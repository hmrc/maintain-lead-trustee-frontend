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

package controllers.trustee.individual.add

import base.SpecBase
import connectors.TrustConnector
import mapping.mappers.trustee.TrusteeIndividualMapper
import models.{Name, TrusteeIndividual, Trustees}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import pages.trustee.individual.NamePage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.checkYourAnswers.TrusteeIndividualPrintHelper
import viewmodels.AnswerSection
import views.html.trustee.individual.add.CheckDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase with ScalaFutures with BeforeAndAfterEach {

  private val date: LocalDate = LocalDate.parse("1996-02-03")

  private lazy val onPageLoadRoute = routes.CheckDetailsController.onPageLoad().url
  private lazy val onSubmitRoute = routes.CheckDetailsController.onSubmit().url

  private val name = Name("Joe", None, "Bloggs")

  private val trustee = TrusteeIndividual(
    name = name,
    dateOfBirth = None,
    phoneNumber = None,
    identification = None,
    address = None,
    countryOfResidence = None,
    nationality = None,
    mentalCapacityYesNo = None,
    entityStart = date,
    provisional = true
  )

  val mockConnector: TrustConnector = Mockito.mock(classOf[TrustConnector])
  val mapper: TrusteeIndividualMapper = Mockito.mock(classOf[TrusteeIndividualMapper])
  val printHelper: TrusteeIndividualPrintHelper = Mockito.mock(classOf[TrusteeIndividualPrintHelper])

  def createApplication(): Application = {
    applicationBuilder(userAnswers = Some(baseAnswers), affinityGroup = Agent)
      .overrides(
        bind[TrustConnector].toInstance(mockConnector),
        bind[TrusteeIndividualPrintHelper].toInstance(printHelper),
        bind[TrusteeIndividualMapper].toInstance(mapper))
      .build()
  }

  override def beforeEach(): Unit = {
    reset(mockConnector)

    when(mockConnector.getTrustees(any())(any(), any()))
      .thenReturn(Future.successful(Trustees(List(trustee))))

    when(mockConnector.addTrustee(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  private val baseAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  "CheckDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val answerSection: AnswerSection = AnswerSection(None, Nil)
      when(printHelper.print(any(), any(), any())(any())).thenReturn(answerSection)
      val application = createApplication()
      val request = FakeRequest(GET, onPageLoadRoute)
      val result = route(application, request).value
      val view = application.injector.instanceOf[CheckDetailsView]
      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(answerSection)(request, messages).toString
    }

    "redirect to the 'add a trustee' page when submitted" in {
      val application = createApplication()
      when(mapper.map(any())).thenReturn(Some(trustee))
      val request = FakeRequest(POST, onSubmitRoute)
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url
      application.stop()
    }

    "return InternalServerError for a POST" when {
      "mapper fails" in {
        val application = createApplication()
        when(mapper.map(any())).thenReturn(None)
        val request = FakeRequest(POST, onSubmitRoute)
        val result = route(application, request).value
        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "redirect to the AddATrusteePage when trustee details are not in the list" in {
      val application = createApplication()
      when(mapper.map(any())).thenReturn(Some(trustee))
      val request = FakeRequest(POST, onSubmitRoute)
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER

      redirectLocation(result).get mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

    }
  }
}