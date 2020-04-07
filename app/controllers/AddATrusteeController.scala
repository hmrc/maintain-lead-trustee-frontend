/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.FrontendAppConfig
import connectors.TrustStoreConnector
import controllers.actions.StandardActionSets
import forms.YesNoFormProvider
import forms.trustee.AddATrusteeFormProvider
import javax.inject.Inject
import models.{AddATrustee, AllTrustees, Enumerable}
import navigation.Navigator
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddATrusteeViewHelper
import views.html.trustee.{AddATrusteeView, AddATrusteeYesNoView, MaxedOutTrusteesView}

import scala.concurrent.{ExecutionContext, Future}

class AddATrusteeController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       repository: PlaybackRepository,
                                       navigator: Navigator,
                                       trust: TrustService,
                                       standardActionSets: StandardActionSets,
                                       addAnotherFormProvider: AddATrusteeFormProvider,
                                       yesNoFormProvider: YesNoFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       addAnotherView: AddATrusteeView,
                                       yesNoView: AddATrusteeYesNoView,
                                       completeView: MaxedOutTrusteesView,
                                       val appConfig: FrontendAppConfig,
                                       trustStoreConnector: TrustStoreConnector
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport
  with Enumerable.Implicits {

  val addAnotherForm : Form[AddATrustee] = addAnotherFormProvider()

  val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addATrusteeYesNo")

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.utr) map {
        case AllTrustees(None, Nil) =>
          Ok(yesNoView(yesNoForm))
        case all: AllTrustees =>

          val trustees = new AddATrusteeViewHelper(all).rows

          if (all.trustees.size < 25) {
            Ok(addAnotherView(
              form = addAnotherForm,
              inProgressTrustees = trustees.inProgress,
              completeTrustees = trustees.complete,
              isLeadTrusteeDefined = all.lead.isDefined,
              heading = all.addToHeading
            ))
          } else {
            Ok(completeView(
              inProgressTrustees = trustees.inProgress,
              completeTrustees = trustees.complete,
              isLeadTrusteeDefined = all.lead.isDefined,
              heading = all.addToHeading
            ))
          }
      }
  }

  def submitOne(): Action[AnyContent] = standardActionSets.identifiedUserWithData {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          BadRequest(yesNoView(formWithErrors))
        },
        addNow => {
            if (addNow) {
              Redirect(controllers.routes.LeadTrusteeOrTrusteeController.onPageLoad())
            } else {
              Redirect(appConfig.maintainATrustOverview)
            }
        }
      )
  }

  def submitAnother(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.utr).flatMap { trustees =>
        addAnotherForm.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {

            val rows = new AddATrusteeViewHelper(trustees).rows

            Future.successful(BadRequest(
              addAnotherView(
                formWithErrors,
                rows.inProgress,
                rows.complete,
                isLeadTrusteeDefined = trustees.lead.isDefined,
                trustees.addToHeading
              )
            ))
          },
          {
            case AddATrustee.YesNow =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.deleteAtPath(pages.trustee.basePath))
                _ <- repository.set(updatedAnswers)
              } yield Redirect(controllers.trustee.routes.IndividualOrBusinessController.onPageLoad())
            case AddATrustee.YesLater =>
              Future.successful(Redirect(appConfig.maintainATrustOverview))
            case AddATrustee.NoComplete =>
              for {
                _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
              } yield {
                Redirect(appConfig.maintainATrustOverview)
              }
          }
        )
      }
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      for {
        _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
      } yield {
        Redirect(appConfig.maintainATrustOverview)
      }
  }
}
