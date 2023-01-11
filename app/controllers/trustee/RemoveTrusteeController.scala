/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.trustee

import controllers.actions.StandardActionSets
import forms.RemoveIndexFormProvider
import handlers.ErrorHandler
import models.{RemoveTrustee, TrusteeIndividual, TrusteeOrganisation}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveIndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveTrusteeController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         standardActionSets: StandardActionSets,
                                         trust: TrustService,
                                         formProvider: RemoveIndexFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemoveIndexView,
                                         errorHandler: ErrorHandler
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private def formRoute(index: Int): Call =
    controllers.trustee.routes.RemoveTrusteeController.onSubmit(index)

  private val messagesPrefix: String = "removeATrusteeYesNo"

  private val form = formProvider.apply(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trust.getTrustee(request.userAnswers.identifier, index).map {
        trustee =>
          val trusteeName = trustee match {
            case lti: TrusteeIndividual => lti.name.displayName
            case lto: TrusteeOrganisation => lto.name
          }
        Ok(view(messagesPrefix, form, index, trusteeName, formRoute(index)))
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" user cannot remove trustee as trustee was not found ${iobe.getMessage}: IndexOutOfBoundsException")

          Future.successful(Redirect(controllers.routes.AddATrusteeController.onPageLoad()))
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
            s" user cannot remove trustee as trustee was not found")
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trust.getTrustee(request.userAnswers.identifier, index).map {
            trustee =>
              val trusteeName = trustee match {
                case lti: TrusteeIndividual => lti.name.displayName
                case lto: TrusteeOrganisation => lto.name
              }
              BadRequest(view(messagesPrefix, formWithErrors, index, trusteeName, formRoute(index)))
          }
        },
        value => {
          if (value) {

            trust.getTrustee(request.userAnswers.identifier, index).flatMap {
              trustee =>
                if (trustee.isNewlyAdded) {
                  for {
                    _ <- trust.removeTrustee(request.userAnswers.identifier, RemoveTrustee(trustee.`type`, index))
                  } yield Redirect(controllers.routes.AddATrusteeController.onPageLoad())
                } else {
                  Future.successful(Redirect(controllers.trustee.routes.WhenRemovedController.onPageLoad(index).url))
                }
            } recoverWith {
              case _ =>
                logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
                  s" user cannot remove trustee as trustee was not found")
                Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
            }
          } else {
            Future.successful(Redirect(controllers.routes.AddATrusteeController.onPageLoad.url))
          }
        }
      )
  }
}
