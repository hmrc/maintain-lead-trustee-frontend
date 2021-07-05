/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.trustee.organisation.add

import base.SpecBase
import connectors.TrustConnector
import mapping.mappers.trustee.TrusteeOrganisationMapper
import models.TrusteeOrganisation
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.trustee.organisation.NamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.checkYourAnswers.TrusteeOrganisationPrintHelper
import viewmodels.AnswerSection
import views.html.trustee.organisation.add.CheckDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class CheckDetailsControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val date: LocalDate = LocalDate.parse("1996-02-03")

  private lazy val onPageLoadRoute = routes.CheckDetailsController.onPageLoad().url
  private lazy val onSubmitRoute = routes.CheckDetailsController.onSubmit().url

  private val name = "Amazon"

  private val trustee = TrusteeOrganisation(
    name = name,
    phoneNumber = None,
    email = None,
    identification = None,
    countryOfResidence = None,
    entityStart = date,
    provisional = true
  )

  private val baseAnswers = emptyUserAnswers
    .set(NamePage, name).success.value

  "CheckDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val printHelper: TrusteeOrganisationPrintHelper = mock[TrusteeOrganisationPrintHelper]

      val answerSection: AnswerSection = AnswerSection(None, Nil)

      when(printHelper.print(any(), any(), any())(any())).thenReturn(answerSection)

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[TrusteeOrganisationPrintHelper].toInstance(printHelper))
        .build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckDetailsView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection)(request, messages).toString
    }

    "redirect to the 'add a trustee' page when submitted" in {

      val connector: TrustConnector = mock[TrustConnector]
      val mapper: TrusteeOrganisationMapper = mock[TrusteeOrganisationMapper]

      val application = applicationBuilder(userAnswers = Some(baseAnswers), affinityGroup = Agent)
        .overrides(
          bind[TrustConnector].toInstance(connector),
          bind[TrusteeOrganisationMapper].toInstance(mapper)
        ).build()

      when(connector.addTrustee(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mapper.map(any())).thenReturn(Some(trustee))

      val request = FakeRequest(POST, onSubmitRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.AddATrusteeController.onPageLoad().url

      application.stop()
    }

    "return InternalServerError for a POST" when {
      "mapper fails" in {

        val mapper: TrusteeOrganisationMapper = mock[TrusteeOrganisationMapper]

        val application = applicationBuilder(userAnswers = Some(baseAnswers), affinityGroup = Agent)
          .overrides(bind[TrusteeOrganisationMapper].toInstance(mapper))
          .build()

        when(mapper.map(any())).thenReturn(None)

        val request = FakeRequest(POST, onSubmitRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
