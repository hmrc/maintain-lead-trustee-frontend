/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logging}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Session

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SessionTimeoutController @Inject()(val appConfig: FrontendAppConfig,
                                         val config: Configuration,
                                         mcc: MessagesControllerComponents) extends FrontendController(mcc) with Logging {

  val keepAlive: Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"[Session ID: ${Session.id(hc)}] user requested to extend the time remaining to maintain trustees, user has not been signed out")
    Future.successful(Ok.withSession(request.session))
  }

  val timeout: Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"[Session ID: ${Session.id(hc)}] user remained inactive on the service, user has been signed out")
    Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad.url).withNewSession)
  }

}
