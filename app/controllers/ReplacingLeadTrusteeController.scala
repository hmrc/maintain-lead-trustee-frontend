/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.StandardActionSets
import forms.ReplaceLeadTrusteeFormProvider
import handlers.ErrorHandler
import mapping.extractors.{IndividualTrusteeToLeadTrusteeExtractor, OrganisationTrusteeToLeadTrusteeExtractor}
import models.requests.DataRequest
import models.{AllTrustees, LeadTrustee, LeadTrusteeIndividual, LeadTrusteeOrganisation, Trustee, TrusteeIndividual, TrusteeOrganisation, UserAnswers}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RadioOption
import views.html.ReplacingLeadTrusteeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReplacingLeadTrusteeController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                playbackRepository: PlaybackRepository,
                                                trust: TrustService,
                                                standardActionSets: StandardActionSets,
                                                formProvider: ReplaceLeadTrusteeFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: ReplacingLeadTrusteeView,
                                                errorHandler: ErrorHandler,
                                                individualTrusteeToLeadTrusteeExtractor: IndividualTrusteeToLeadTrusteeExtractor,
                                                organisationTrusteeToLeadTrusteeExtractor: OrganisationTrusteeToLeadTrusteeExtractor
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messageKeyPrefix: String = "replacingLeadTrustee"

  private val form: Form[String] = formProvider.withPrefix(messageKeyPrefix)

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.identifier) map {
        case AllTrustees(leadTrustee, trustees) =>
          val radioOptions = generateRadioOptions(trustees)
          Ok(view(form, getLeadTrusteeName(leadTrustee), radioOptions))
      } recoverWith {
        recovery
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.identifier) flatMap {
        case AllTrustees(leadTrustee, trustees) =>
          form.bindFromRequest().fold(
            formWithErrors => {
              val radioOptions = generateRadioOptions(trustees)
              Future.successful(BadRequest(view(formWithErrors, getLeadTrusteeName(leadTrustee), radioOptions)))
            },
            value => {
              value.toInt match {
                case index =>
                  trustees(index) match {
                    case ind: TrusteeIndividual => populateUserAnswersAndRedirect(request.userAnswers, ind, index)
                    case org: TrusteeOrganisation => populateUserAnswersAndRedirect(request.userAnswers, org, index)
                  }
              }
            }
          )
      } recoverWith {
        recovery
      }
  }

  private def generateRadioOptions(trustees: List[Trustee]): List[RadioOption] = {
    trustees
      .zipWithIndex
      .filter(_._1 match {
        case ind: TrusteeIndividual => !ind.mentalCapacityYesNo.contains(false)
        case _: TrusteeOrganisation => true
      })
      .map { x =>
        val name = x._1 match {
          case ind: TrusteeIndividual => ind.name.displayName
          case org: TrusteeOrganisation => org.name
        }
        RadioOption(s"$messageKeyPrefix.${x._2}", s"${x._2}", name)
      }
  }

  private def getLeadTrusteeName(leadTrustee: Option[LeadTrustee])(implicit request: DataRequest[AnyContent]): String = {
    leadTrustee match {
      case Some(ltInd: LeadTrusteeIndividual) => ltInd.name.displayName
      case Some(ltOrg: LeadTrusteeOrganisation) => ltOrg.name
      case None => request.messages(messagesApi)("leadTrusteeName.defaultText")
    }
  }

  private def populateUserAnswersAndRedirect(userAnswers: UserAnswers, trustee: TrusteeIndividual, index: Int): Future[Result] = {
    for {
      updatedAnswers <- Future.fromTry(individualTrusteeToLeadTrusteeExtractor.extract(userAnswers, trustee, index))
      _ <- playbackRepository.set(updatedAnswers)
    } yield Redirect(controllers.leadtrustee.individual.routes.NeedToAnswerQuestionsController.onPageLoad())
  }

  private def populateUserAnswersAndRedirect(userAnswers: UserAnswers, trustee: TrusteeOrganisation, index: Int): Future[Result] = {
    for {
      updatedAnswers <- Future.fromTry(organisationTrusteeToLeadTrusteeExtractor.extract(userAnswers, trustee, index))
      _ <- playbackRepository.set(updatedAnswers)
    } yield Redirect(controllers.leadtrustee.organisation.routes.NeedToAnswerQuestionsController.onPageLoad())
  }

  private def recovery(implicit request: DataRequest[AnyContent]): PartialFunction[Throwable, Future[Result]] = {
    case e =>
      logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
        s" Problem getting trustees: ${e.getMessage}")

      Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
  }

}
