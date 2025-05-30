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

package controllers.trustee.individual

import java.time.LocalDate
import base.SpecBase
import models.{CombinedPassportOrIdCard, Mode, Name, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import pages.trustee.individual.amend.IndexPage
import pages.trustee.individual.{NamePage, PassportOrIdCardDetailsPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class PassportOrIdCardDetailsControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val name: Name = Name("FirstName", None, "LastName")
  private val mode: Mode = NormalMode
  private val index = 0

  val userAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(IndexPage, index).success.value

  private lazy val passportOrIdCardDetailsRoute: String =
    routes.PassportOrIdCardDetailsController.onPageLoad(mode).url

  private lazy val checkDetailsRoute =
    controllers.trustee.individual.amend.routes.CheckDetailsController.onPageLoadUpdated(index).url

  val validData: CombinedPassportOrIdCard = CombinedPassportOrIdCard("country", "number", LocalDate.parse("2020-02-03"))

  override def beforeEach(): Unit = {
    reset(playbackRepository)
    when(playbackRepository.set(any())).thenReturn(Future.successful(true))
  }

  "PassportOrIdCardDetails Controller" must {

    "redirect to check details for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to check details when previously answered" in {

      val newUserAnswers = userAnswers.set(PassportOrIdCardDetailsPage, validData).success.value

      val application = applicationBuilder(userAnswers = Some(newUserAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to check details when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      val request = FakeRequest(POST, passportOrIdCardDetailsRoute)
        .withFormUrlEncodedBody(
          "country" -> validData.countryOfIssue,
          "number" -> validData.number,
          "expiryDate.day" -> validData.expirationDate.getDayOfMonth.toString,
          "expiryDate.month" -> validData.expirationDate.getMonthValue.toString,
          "expiryDate.year" -> validData.expirationDate.getYear.toString,
          "detailsType" -> validData.detailsType.toString
        )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()

    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
