/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito.{clearInvocations, never, spy, times, verify, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class LogoutControllerSpec extends SpecBase {

  private val mockAuditConnector = Mockito.mock(classOf[AuditConnector])
  private val appConfig          = spy(app.injector.instanceOf[FrontendAppConfig])

  private val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
    .overrides(
      bind[AuditConnector].toInstance(mockAuditConnector),
      bind[FrontendAppConfig].toInstance(appConfig)
    )
    .build()

  private val request = FakeRequest(GET, routes.LogoutController.logout().url)

  "logout should redirect to feedback and not audit, given appConfig.logoutAudit is false" in {

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustBe frontendAppConfig.logoutUrl

    verify(mockAuditConnector, never())
      .sendExplicitAudit(eqTo("trusts"), any[Map[String, String]])(any(), any())

  }

  "logout should redirect to feedback and send audit, given appConfig.logoutAudit is true" in {

    clearInvocations(mockAuditConnector)

    when(appConfig.logoutAudit).thenReturn(true)

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustBe frontendAppConfig.logoutUrl

    verify(mockAuditConnector, times(1))
      .sendExplicitAudit(eqTo("trusts"), any[Map[String, String]])(any(), any())

    application.stop()
  }

}
