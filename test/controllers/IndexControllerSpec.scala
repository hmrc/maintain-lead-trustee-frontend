/*
 * Copyright 2026 HM Revenue & Customs
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
import connectors.TrustConnector
import models.TaskStatus.InProgress
import models.{TrustDetails, UserAnswers}
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "Index Controller" must {
    val identifier = "1234567890"
    val startDate = "2019-06-01"
    val isTaxable = false
    val isUnderlyingData5mld = false

    "return OK and the correct view for a GET" in {

      val mockTrustConnector = Mockito.mock(classOf[TrustConnector])
      val mockTrustsStoreService = Mockito.mock(classOf[TrustsStoreService])

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = LocalDate.parse(startDate), trustTaxable = Some(isTaxable))))

      when(mockTrustsStoreService.updateTaskStatus(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mockTrustConnector.isTrust5mld(any())(any(), any()))
        .thenReturn(Future.successful(isUnderlyingData5mld))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.AddATrusteeController.onPageLoad().url)

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)

      uaCaptor.getValue.internalId mustBe "id"
      uaCaptor.getValue.identifier mustBe identifier
      uaCaptor.getValue.whenTrustSetup mustBe LocalDate.parse(startDate)
      uaCaptor.getValue.isTaxable mustBe isTaxable

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }
  }
}
