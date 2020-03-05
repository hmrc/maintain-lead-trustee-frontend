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

package controllers.leadtrustee

import controllers.actions.StandardActionSets
import javax.inject.Inject
import models.{LeadTrusteeIndividual, LeadTrusteeOrganisation}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.leadtrustee.UnableToRemoveView

import scala.concurrent.ExecutionContext

class UnableToRemoveController @Inject()(
                                                val controllerComponents: MessagesControllerComponents,
                                                view: UnableToRemoveView,
                                                service: TrustService,
                                                standardActionSets: StandardActionSets
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad: Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      service.getLeadTrustee(request.userAnswers.utr) map {
        case Some(lt) =>
          lt match {
            case LeadTrusteeIndividual(name, _, _, _, _, _) =>
              Ok(view(name.displayName))
            case LeadTrusteeOrganisation(name, _, _, _, _) =>
              Ok(view(name))
          }
        case None =>
        Redirect(controllers.routes.AddATrusteeController.onPageLoad())
      }
  }
}