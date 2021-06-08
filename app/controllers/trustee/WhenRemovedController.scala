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

package controllers.trustee

import controllers.actions.StandardActionSets
import forms.DateRemovedFromTrustFormProvider
import models.{RemoveTrustee, TrusteeIndividual, TrusteeOrganisation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.trustee.WhenRemovedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhenRemovedController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       formProvider: DateRemovedFromTrustFormProvider,
                                       trust: TrustService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WhenRemovedView,
                                       trustService: TrustService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getTrustee(request.userAnswers.identifier, index).map {
        trustee =>
          val (trusteeName, entityStartDate) = trustee match {
            case lti: TrusteeIndividual => (lti.name.displayName, lti.entityStart)
            case lto: TrusteeOrganisation => (lto.name, lto.entityStart)
          }

          val form = formProvider.withPrefixAndEntityStartDate("trustee.whenRemoved", entityStartDate)

          Ok(view(form, index, trusteeName))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getTrustee(request.userAnswers.identifier, index).flatMap {
        trustee =>
          val (trusteeName, entityStartDate) = trustee match {
            case lti: TrusteeIndividual => (lti.name.displayName, lti.entityStart)
            case lto: TrusteeOrganisation => (lto.name, lto.entityStart)
          }

          val form = formProvider.withPrefixAndEntityStartDate("trustee.whenRemoved", entityStartDate)

          form.bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, index, trusteeName)))
            },
            value =>
              for {
                _ <- trustService.removeTrustee(request.userAnswers.identifier, RemoveTrustee(trustee.`type`, index, value))
              } yield Redirect(controllers.routes.AddATrusteeController.onPageLoad())
          )
      }
  }
}
