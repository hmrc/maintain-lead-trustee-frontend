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

package controllers.leadtrustee.individual

import controllers.actions.StandardActionSets
import controllers.leadtrustee.actions.NameRequiredAction
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.leadtrustee.individual.NeedToAnswerQuestionsView

class NeedToAnswerQuestionsController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 nameAction: NameRequiredAction,
                                                 standardActionSets: StandardActionSets,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: NeedToAnswerQuestionsView
                                               ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      Ok(view(request.leadTrusteeName))
  }

  def onSubmit: Action[AnyContent] = Action {
    Redirect(controllers.leadtrustee.individual.routes.NameController.onPageLoad.url)
  }

}
