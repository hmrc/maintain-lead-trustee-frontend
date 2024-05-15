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

import base.SpecBase
import connectors.TrustsStoreConnector
import forms.YesNoFormProvider
import forms.trustee.AddATrusteeFormProvider
import models.IndividualOrBusiness.Individual
import models.TaskStatus.Completed
import models._
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.trustee.IndividualOrBusinessPage
import pages.trustee.individual.NamePage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddATrusteeViewHelper
import viewmodels.addAnother.AddRow
import views.html.trustee.{AddATrusteeView, AddATrusteeYesNoView, MaxedOutTrusteesView}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AddATrusteeControllerSpec extends SpecBase with BeforeAndAfterEach {

  lazy val getRoute: String = controllers.routes.AddATrusteeController.onPageLoad().url
  lazy val submitAnotherRoute: String = controllers.routes.AddATrusteeController.submitAnother().url
  lazy val submitYesNoRoute: String = controllers.routes.AddATrusteeController.submitOne().url
  lazy val submitCompleteRoute: String = controllers.routes.AddATrusteeController.submitComplete().url

  val mockStoreConnector: TrustsStoreConnector = Mockito.mock(classOf[TrustsStoreConnector])

  val addTrusteeForm = new AddATrusteeFormProvider()()
  val yesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addATrusteeYesNo")

  val trusteeRows: List[AddRow] = List(
    AddRow(
      name = "First Last",
      typeLabel = "Trustee Individual",
      changeLabel = "Change details",
      changeUrl = "/maintain-a-trust/trustees/trustee/individual/0/check-details",
      removeLabel = Some("Remove"),
      removeUrl = Some("/maintain-a-trust/trustees/trustee/0/remove")
    ),
    AddRow(
      name = "First Last",
      typeLabel = "Trustee Individual",
      changeLabel = "Change details",
      changeUrl = "/maintain-a-trust/trustees/trustee/individual/1/check-details",
      removeLabel = Some("Remove"),
      removeUrl = Some("/maintain-a-trust/trustees/trustee/1/remove")
    )
  )

  val leadAndTrusteeRows: List[AddRow] = AddRow(
    name = "Lead First Last",
    typeLabel = "Lead Trustee Individual",
    changeLabel = "Change details",
    changeUrl = "/maintain-a-trust/trustees/lead-trustee/individual/check-details",
    removeLabel =  None,
    removeUrl = None
  ) +: trusteeRows

  private val leadTrusteeIndividual = Some(LeadTrusteeIndividual(
    bpMatchStatus = None,
    name = Name(
      firstName = "Lead First",
      middleName = None,
      lastName = "Last"
    ),
    dateOfBirth = LocalDate.parse("2010-10-10"),
    phoneNumber = "+446565657",
    email = None,
    identification = NationalInsuranceNumber("JP121212A"),
    address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  ))

  private val trustee = TrusteeIndividual(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    phoneNumber = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  val trustees: Trustees = Trustees(List.fill(2)(trustee))

  class FakeService(data: Trustees, leadTrustee: Option[LeadTrustee] = leadTrusteeIndividual) extends TrustService {

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
                           (implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Trustee] =
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

  override def beforeEach(): Unit = {
    reset(mockStoreConnector)

    when(mockStoreConnector.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse.apply(OK, "")))
  }

  "AddATrustee Controller" when {

    "no data" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(Trustees(Nil))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind(classOf[TrustService])
            .toInstance(fakeService)).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddATrustee.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }

    "no trustees" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(Trustees(Nil), None)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddATrusteeYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(yesNoForm)(request, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val fakeService = new FakeService(Trustees(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.LeadTrusteeOrTrusteeController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitYesNoRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = yesNoForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddATrusteeYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm)(request, messages).toString

        application.stop()
      }
    }

    "there are trustees" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(trustees, None)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddATrusteeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addTrusteeForm ,Nil, trusteeRows, isLeadTrusteeDefined = false, heading = "The trust has 2 trustees")(request, messages).toString

        application.stop()
      }

      "redirect to the 'add trustee' journey when the user elects to add a trustee" in {

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddATrustee.YesNow.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.trustee.routes.IndividualOrBusinessController.onPageLoad().url

        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          ).build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddATrustee.NoComplete.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "redirect to the maintain task list when the user says they want to add later" ignore {

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddATrustee.YesLater.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/individual-declaration"

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val fakeService = new FakeService(trustees, None)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addTrusteeForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddATrusteeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            Nil,
            trusteeRows,
            isLeadTrusteeDefined = false,
            heading = "The trust has 2 trustees"
          )(request, messages).toString

        application.stop()
      }

    }

    "there is a lead trustee and trustees" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddATrusteeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addTrusteeForm ,Nil, leadAndTrusteeRows, isLeadTrusteeDefined = true, heading = "The trust has 3 trustees")(request, messages).toString

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val fakeService = new FakeService(trustees)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addTrusteeForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddATrusteeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            Nil,
            leadAndTrusteeRows,
            isLeadTrusteeDefined = true,
            heading = "The trust has 3 trustees"
          )(request, messages).toString

        application.stop()
      }

    }

    "Clear out the user answers when starting the add trustee journey and redirect to individual or business page" in {

      val mockTrustService = Mockito.mock(classOf[TrustService])

      val userAnswers = emptyUserAnswers
        .set(IndividualOrBusinessPage, Individual).success.value
        .set(NamePage, Name("First", None, "Last")).success.value

      reset(playbackRepository)

      val application = applicationBuilder(userAnswers = Some(userAnswers), affinityGroup = Agent)
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .build()

      when(mockTrustService.getAllTrustees(any())(any(), any())).thenReturn(Future.successful(AllTrustees(None, Nil)))
      when(playbackRepository.set(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, submitAnotherRoute)
        .withFormUrlEncodedBody(("value", AddATrustee.YesNow.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.trustee.routes.IndividualOrBusinessController.onPageLoad().url

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.data mustBe Json.obj()
    }

  }

  "maxed out trustees" must {

    val trustees = Trustees(List.fill(25)(trustee))

    val fakeService = new FakeService(trustees)

    val trusteeRows = new AddATrusteeViewHelper(AllTrustees(leadTrusteeIndividual, trustees.trustees)).rows

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind(classOf[TrustService]).toInstance(fakeService))
        .build()

      val request = FakeRequest(GET, getRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MaxedOutTrusteesView]

      status(result) mustEqual OK

      val content = contentAsString(result)

      content mustEqual
        view(trusteeRows.inProgress, trusteeRows.complete, isLeadTrusteeDefined = true, "The trust has 26 trustees")(request, messages).toString
      content must include("You cannot add another trustee as you have entered a maximum of 26.")
      content must include("You can add another trustee by removing an existing one, or write to HMRC with details of any additional trustees.")

      application.stop()

    }

    "redirect to add to page and set beneficiaries to complete when user clicks continue" in {

      val fakeService = new FakeService(trustees)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
        ).build()

      val request = FakeRequest(POST, submitCompleteRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

      verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

      application.stop()

    }

  }
}
