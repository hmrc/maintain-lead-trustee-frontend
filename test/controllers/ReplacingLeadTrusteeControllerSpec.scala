/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.TrusteeTypeFormProvider
import models.BpMatchStatus.FullyMatched
import models.YesNoDontKnow.{No, Yes}
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import viewmodels.RadioOption
import views.html.ReplacingLeadTrusteeView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ReplacingLeadTrusteeControllerSpec extends SpecBase with MockitoSugar {

  private val messageKeyPrefix: String = "replacingLeadTrustee"
  private val form: Form[TrusteeType] = new TrusteeTypeFormProvider().withPrefix(messageKeyPrefix)

  private lazy val replacingLeadTrusteeRoute: String = routes.ReplacingLeadTrusteeController.onPageLoad.url

  private val date: LocalDate = LocalDate.parse("2019-02-28")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")

  private val leadTrusteeIndividual = LeadTrusteeIndividual(
    bpMatchStatus = Some(FullyMatched),
    name = Name(firstName = "John", middleName = Some("Jonathan"), lastName = "Smith"),
    dateOfBirth = date,
    phoneNumber = "+446565657",
    email = None,
    identification = NationalInsuranceNumber("JP121212A"),
    address = ukAddress
  )

  private class FakeService(data: Trustees, leadTrustee: Option[LeadTrustee] = Some(leadTrusteeIndividual)) extends TrustService {

    override def getLeadTrustee(identifier: String)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[LeadTrustee]] =
      ???

    override def getAllTrustees(identifier: String)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AllTrustees] =
      Future.successful(AllTrustees(leadTrustee, data.trustees))

    override def getTrustees(identifier: String)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Trustees] =
      ???

    override def getTrustee(identifier: String, index: Int)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Trustee] =
      ???

    override def removeTrustee(identifier: String, trustee: RemoveTrustee)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      ???

    override def getBusinessUtrs(identifier: String, index: Option[Int], adding: Boolean)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] =
      ???

    override def getIndividualNinos(identifier: String, index: Option[Int], adding: Boolean)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] =
      ???

  }

  "ReplacingLeadTrustee controller" must {

    "return OK and the correct view for a GET" when {

      "all trustees mentally capable" in {

        val trustee1 = TrusteeIndividual(
          name = Name(firstName = "Joe", middleName = Some("Joseph"), lastName = "Bloggs"),
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = Some(Yes),
          entityStart = date,
          provisional = true
        )

        val trustee2 = TrusteeIndividual(
          name = Name(firstName = "John", middleName = Some("Joe"), lastName = "Doe"),
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = Some(Yes),
          entityStart = date,
          provisional = true
        )

        val trustees = Trustees(List(trustee1, trustee2))

        val expectedRadioOptions = List(
          RadioOption(s"$messageKeyPrefix.0", "0", "Joe Bloggs"),
          RadioOption(s"$messageKeyPrefix.1", "1", "John Doe")
        )

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, replacingLeadTrusteeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReplacingLeadTrusteeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, "John Smith", expectedRadioOptions)(request, messages).toString

        application.stop()
      }

      "trustees contains a mentally incapable trustee" in {

        val indTrustee1 = TrusteeIndividual(
          name = Name(firstName = "Joe", middleName = Some("Joseph"), lastName = "Bloggs"),
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = Some(No),
          entityStart = date,
          provisional = true
        )

        val indTrustee2 = TrusteeIndividual(
          name = Name(firstName = "John", middleName = Some("Joe"), lastName = "Doe"),
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = Some(Yes),
          entityStart = date,
          provisional = true
        )

        val trustees = Trustees(List(indTrustee1, indTrustee2))

        val expectedRadioOptions = List(
          RadioOption(s"$messageKeyPrefix.1", "1", "John Doe")
        )

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, replacingLeadTrusteeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReplacingLeadTrusteeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, "John Smith", expectedRadioOptions)(request, messages).toString

        application.stop()
      }
    }

    "redirect to the next page when valid data is submitted" when {

      "individual" in {

        val trustee = TrusteeIndividual(
          name = Name(firstName = "Joe", middleName = Some("Joseph"), lastName = "Bloggs"),
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          entityStart = LocalDate.parse("2019-02-28"),
          provisional = true
        )

        val fakeService = new FakeService(Trustees(List(trustee)))

        val mockPlaybackRepository = mock[PlaybackRepository]

        when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, replacingLeadTrusteeRoute)
          .withFormUrlEncodedBody(("value", "0"))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.leadtrustee.individual.routes.NeedToAnswerQuestionsController.onPageLoad.url

        application.stop()
      }

      "organisation" in {

        val trustee = TrusteeOrganisation(
          name = "Amazon",
          phoneNumber = None,
          email = None,
          identification = None,
          entityStart = date,
          provisional = true
        )

        val fakeService = new FakeService(Trustees(List(trustee)))

        val mockPlaybackRepository = mock[PlaybackRepository]

        when(mockPlaybackRepository.set(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, replacingLeadTrusteeRoute)
          .withFormUrlEncodedBody(("value", "0"))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustBe controllers.leadtrustee.organisation.routes.NeedToAnswerQuestionsController.onPageLoad.url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" when {

      "individual lead trustee" in {

        val fakeService = new FakeService(Trustees(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, replacingLeadTrusteeRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReplacingLeadTrusteeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, "John Smith", Nil)(request, messages).toString

        application.stop()
      }

      "organisation lead trustee" in {

        val leadTrustee = LeadTrusteeOrganisation(
          name = "Amazon",
          phoneNumber = "tel",
          email = None,
          utr = None,
          address = ukAddress
        )

        val fakeService = new FakeService(Trustees(Nil), Some(leadTrustee))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, replacingLeadTrusteeRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReplacingLeadTrusteeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, "Amazon", Nil)(request, messages).toString

        application.stop()
      }

      //should never happen
      "no lead trustee" in {

        val fakeService = new FakeService(Trustees(Nil), None)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, replacingLeadTrusteeRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReplacingLeadTrusteeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, "the lead trustee", Nil)(request, messages).toString

        application.stop()
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, replacingLeadTrusteeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, replacingLeadTrusteeRoute)
        .withFormUrlEncodedBody(("value", "0"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
