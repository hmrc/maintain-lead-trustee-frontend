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
import models.{AllTrustees, Name, NationalInsuranceNumber, TrusteeIndividual, TrusteeOrganisation, YesNoDontKnow}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import views.html.ChangeLeadTrusteeView

import java.time.LocalDate
import scala.concurrent.Future

class ChangeLeadTrusteeControllerSpec extends SpecBase {

  lazy val changeLeadTrusteeRoute       = routes.ChangeLeadTrusteeController.onPageLoad().url
  lazy val changeLeadTrusteeSubmitRoute = routes.ChangeLeadTrusteeController.onSubmit().url

  val eligibleIndividual = TrusteeIndividual(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    phoneNumber = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true,
    mentalCapacityYesNo = Some(YesNoDontKnow.Yes)
  )

  val ineligibleIndividual = TrusteeIndividual(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    phoneNumber = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true,
    mentalCapacityYesNo = Some(YesNoDontKnow.No)
  )

  val eligibleOrganisation = TrusteeOrganisation(
    name = "Test Organisation",
    phoneNumber = None,
    email = None,
    identification = None,
    countryOfResidence = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = false
  )

  "ChangeLeadTrustee controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, changeLeadTrusteeRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ChangeLeadTrusteeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(request, messages).toString

      application.stop()
    }

    "onSubmit" when {

      "there are eligible trustees to promote" must {

        "redirect to ReplacingLeadTrusteeController" in {

          val mockTrustService = Mockito.mock(classOf[TrustService])

          val allTrustees = AllTrustees(None, List(eligibleIndividual, eligibleOrganisation))

          when(mockTrustService.getAllTrustees(any())(any(), any()))
            .thenReturn(Future.successful(allTrustees))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          val request = FakeRequest(POST, changeLeadTrusteeSubmitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.ReplacingLeadTrusteeController.onPageLoad().url

          application.stop()
        }
      }

      "there are no eligible trustees to promote" must {

        "redirect to IndividualOrBusinessController when no trustees have mental capacity" in {

          val mockTrustService = Mockito.mock(classOf[TrustService])

          val allTrustees = AllTrustees(None, List(ineligibleIndividual))

          when(mockTrustService.getAllTrustees(any())(any(), any()))
            .thenReturn(Future.successful(allTrustees))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          val request = FakeRequest(POST, changeLeadTrusteeSubmitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.leadtrustee.routes.IndividualOrBusinessController
            .onPageLoad()
            .url

          application.stop()
        }

        "redirect to IndividualOrBusinessController when no trustees exist" in {

          val mockTrustService = Mockito.mock(classOf[TrustService])

          val allTrustees = AllTrustees(None, List.empty)

          when(mockTrustService.getAllTrustees(any())(any(), any()))
            .thenReturn(Future.successful(allTrustees))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          val request = FakeRequest(POST, changeLeadTrusteeSubmitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.leadtrustee.routes.IndividualOrBusinessController
            .onPageLoad()
            .url

          application.stop()
        }
      }

      "mixed eligible and ineligible trustees exist" must {

        "redirect to ReplacingLeadTrusteeController when at least one is eligible" in {

          val mockTrustService = Mockito.mock(classOf[TrustService])

          val allTrustees = AllTrustees(None, List(eligibleIndividual, ineligibleIndividual))

          when(mockTrustService.getAllTrustees(any())(any(), any()))
            .thenReturn(Future.successful(allTrustees))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          val request = FakeRequest(POST, changeLeadTrusteeSubmitRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.ReplacingLeadTrusteeController.onPageLoad().url

          application.stop()
        }
      }
    }
  }

}
