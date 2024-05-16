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

package controllers.leadtrustee.individual

import base.SpecBase
import forms.CombinedPassportOrIdCardDetailsFormProvider
import models.{CombinedPassportOrIdCard, DetailsType, Name, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.leadtrustee.individual.{NamePage, PassportOrIdCardDetailsPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import views.html.leadtrustee.individual.PassportOrIdCardDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class PassportOrIdCardControllerSpec extends SpecBase with BeforeAndAfterEach {

  val formProvider = new CombinedPassportOrIdCardDetailsFormProvider(frontendAppConfig)
  val form: Form[CombinedPassportOrIdCard] = formProvider.withPrefix("leadtrustee.individual.passportOrIdCardDetails")

  lazy val passportDetailsRoute: String = routes.PassportOrIdCardController.onPageLoad().url

  val countryOptions: CountryOptions = injector.instanceOf[CountryOptions]

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .set(NamePage, Name("Lead", None, "Trustee")).success.value

  private val validData: CombinedPassportOrIdCard = CombinedPassportOrIdCard("country", "number", LocalDate.parse("2020-02-03"))

  override def beforeEach(): Unit = {
    reset(playbackRepository)
    when(playbackRepository.set(any())).thenReturn(Future.successful(true))
  }

  "PassportDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, passportDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PassportOrIdCardDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, "Lead Trustee", countryOptions.options())(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsPage, validData).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, passportDetailsRoute)

      val view = application.injector.instanceOf[PassportOrIdCardDetailsView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validData), "Lead Trustee", countryOptions.options())(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" when {

      "number has changed" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[Navigator].toInstance(fakeNavigator))
            .build()

        val request = FakeRequest(POST, passportDetailsRoute)
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
        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(playbackRepository).set(uaCaptor.capture)
        uaCaptor.getValue.get(PassportOrIdCardDetailsPage).get.detailsType mustBe DetailsType.CombinedProvisional

        application.stop()
      }

      "number has not changed" when {

        "previously Combined" in {

          val vd = validData.copy(detailsType = DetailsType.Combined)

          val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsPage, vd).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[Navigator].toInstance(fakeNavigator))
            .build()

          val request = FakeRequest(POST, passportDetailsRoute)
            .withFormUrlEncodedBody(
              "country" -> vd.countryOfIssue,
              "number" -> vd.number,
              "expiryDate.day" -> vd.expirationDate.getDayOfMonth.toString,
              "expiryDate.month" -> vd.expirationDate.getMonthValue.toString,
              "expiryDate.year" -> vd.expirationDate.getYear.toString,
              "detailsType" -> vd.detailsType.toString
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(PassportOrIdCardDetailsPage).get.detailsType mustBe DetailsType.Combined

          application.stop()
        }

        "previously CombinedProvisional" in {

          val vd = validData.copy(detailsType = DetailsType.CombinedProvisional)

          val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsPage, vd).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[Navigator].toInstance(fakeNavigator))
            .build()

          val request = FakeRequest(POST, passportDetailsRoute)
            .withFormUrlEncodedBody(
              "country" -> vd.countryOfIssue,
              "number" -> vd.number,
              "expiryDate.day" -> vd.expirationDate.getDayOfMonth.toString,
              "expiryDate.month" -> vd.expirationDate.getMonthValue.toString,
              "expiryDate.year" -> vd.expirationDate.getYear.toString,
              "detailsType" -> vd.detailsType.toString
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(PassportOrIdCardDetailsPage).get.detailsType mustBe DetailsType.CombinedProvisional

          application.stop()
        }

        "country or expiry date have changed" in {

          val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsPage, validData).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[Navigator].toInstance(fakeNavigator))
            .build()

          val request = FakeRequest(POST, passportDetailsRoute)
            .withFormUrlEncodedBody(
              "country" -> "changed country",
              "number" -> validData.number,
              "expiryDate.day" -> validData.expirationDate.plusDays(1).getDayOfMonth.toString,
              "expiryDate.month" -> validData.expirationDate.getMonthValue.toString,
              "expiryDate.year" -> validData.expirationDate.getYear.toString,
              "detailsType" -> validData.detailsType.toString
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(PassportOrIdCardDetailsPage).get.detailsType mustBe DetailsType.Combined

          application.stop()
        }
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, passportDetailsRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[PassportOrIdCardDetailsView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, "Lead Trustee", countryOptions.options())(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, passportDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, passportDetailsRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
