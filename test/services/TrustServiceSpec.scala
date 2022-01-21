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

package services

import connectors.TrustConnector
import models.BpMatchStatus.FullyMatched
import models.Constants.INDIVIDUAL_TRUSTEE
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TrustServiceSpec extends FreeSpec with MockitoSugar with MustMatchers with ScalaFutures {

  val mockConnector: TrustConnector = mock[TrustConnector]

  val service = new TrustServiceImpl(mockConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val trusteeInd: TrusteeIndividual = TrusteeIndividual(
    name = Name(firstName = "1234567890 QwErTyUiOp ,.(/)&'- name", middleName = None, lastName = "1234567890 QwErTyUiOp ,.(/)&'- name"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    phoneNumber = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  val trusteeOrg: TrusteeOrganisation = TrusteeOrganisation(
    name = "Business",
    phoneNumber = None,
    email = None,
    identification = None,
    countryOfResidence = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  val leadTrusteeInd: LeadTrusteeIndividual = LeadTrusteeIndividual(
    bpMatchStatus = None,
    name = Name("Joe", None, "Bloggs"),
    dateOfBirth = LocalDate.parse("1996-02-03"),
    phoneNumber = "tel",
    email = None,
    identification = NationalInsuranceNumber("nino"),
    address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
    countryOfResidence = None,
    nationality = None
  )

  val leadTrusteeOrg: LeadTrusteeOrganisation = LeadTrusteeOrganisation(
    name = "Business",
    phoneNumber = "tel",
    email = None,
    utr = None,
    address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
    countryOfResidence = None
  )

  val identifier: String = "1234567890"

  "Trust service" - {

    "get all trustees" in {

      val trustees = List(trusteeInd)

      val leadTrusteeIndividual = LeadTrusteeIndividual(
        bpMatchStatus = Some(FullyMatched),
        name = Name(
          firstName = "First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfBirth = LocalDate.parse("2010-10-10"),
        phoneNumber = "+446565657",
        email = None,
        identification = NationalInsuranceNumber("JP121212A"),
        address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")
      )

      when(mockConnector.getTrustees(any())(any(), any()))
        .thenReturn(Future.successful(Trustees(trustees)))

      when(mockConnector.getLeadTrustee(any())(any(), any()))
        .thenReturn(Future.successful(leadTrusteeIndividual))

      val result = service.getAllTrustees("1234567890")

      whenReady(result) { r =>
        r mustBe AllTrustees(
          lead = Some(leadTrusteeIndividual),
          trustees = trustees
        )
      }

    }

    "get trustees" in {

      when(mockConnector.getTrustees(any())(any(), any()))
        .thenReturn(Future.successful(Trustees(List(trusteeInd))))

      val result = service.getTrustees("1234567890")

      whenReady(result) { r =>
        r mustBe Trustees(List(trusteeInd))
      }

    }

    "remove a trustee" in {

      when(mockConnector.removeTrustee(any(),any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val service = new TrustServiceImpl(mockConnector)

      val trustee : RemoveTrustee = RemoveTrustee(
        `type` = INDIVIDUAL_TRUSTEE,
        index = 0,
        endDate = LocalDate.now()
      )

      implicit val hc : HeaderCarrier = HeaderCarrier()

      val result = service.removeTrustee("1234567890", trustee)

      whenReady(result) { r =>
        r.status mustBe 200
      }

    }


    "get trustee" in {

      def trusteeInd(id: Int) = TrusteeIndividual(
        name = Name(firstName = s"First $id", middleName = None, lastName = s"Last $id"),
        dateOfBirth = Some(LocalDate.parse("1983-09-24")),
        phoneNumber = None,
        identification = Some(NationalInsuranceNumber("JS123456A")),
        address = None,
        entityStart = LocalDate.parse("2019-02-28"),
        provisional = true
      )

      val expectedResult = trusteeInd(2)

      val trustees = List(trusteeInd(1), trusteeInd(2), trusteeInd(3))

      when(mockConnector.getTrustees(any())(any(), any()))
        .thenReturn(Future.successful(Trustees(trustees)))

      val result = service.getTrustee("1234567890", 1)

      whenReady(result) { r =>
        r mustBe expectedResult
      }

    }

    ".getBusinessUtrs" - {

      "must return empty list" - {

        "when no businesses" in {

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(Nil)))

          val result = Await.result(service.getBusinessUtrs(identifier, None, adding = true), Duration.Inf)

          result mustBe Nil
        }

        "when there are businesses but they don't have a UTR" in {

          val trustees = List(
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, None, None)))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg.copy(utr = None)))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getBusinessUtrs(identifier, None, adding = true), Duration.Inf)

          result mustBe Nil
        }

        "when there is a business with a UTR but it's the same index as the one we're amending" in {

          val trustees = List(
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr"), None)))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getBusinessUtrs(identifier, Some(0), adding = false), Duration.Inf)

          result mustBe Nil
        }

        "when there is a lead business with a UTR but it's what we're amending" in {

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg.copy(utr = Some("utr"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(Nil)))

          val result = Await.result(service.getBusinessUtrs(identifier, None, adding = false), Duration.Inf)

          result mustBe Nil
        }
      }

      "must return UTRs" - {

        "when businesses have UTRs and we're adding a new trustee (i.e. no index)" in {

          val trustees = List(
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr2"), None))),
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr3"), None)))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg.copy(utr = Some("utr1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getBusinessUtrs(identifier, None, adding = true), Duration.Inf)

          result mustBe List("utr1", "utr2", "utr3")
        }

        "when businesses have UTRs and we're amending the lead trustee (i.e. no index)" in {

          val trustees = List(
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr2"), None))),
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr3"), None)))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg.copy(utr = Some("utr1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getBusinessUtrs(identifier, None, adding = false), Duration.Inf)

          result mustBe List("utr2", "utr3")
        }

        "when businesses have UTRs and we're amending or promoting a trustee" in {

          val trustees = List(
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr2"), None))),
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr3"), None)))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg.copy(utr = Some("utr1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getBusinessUtrs(identifier, Some(0), adding = false), Duration.Inf)

          result mustBe List("utr1", "utr3")
        }

        "when businesses have UTRs and we're amending a different index" in {

          val trustees = List(
            trusteeInd,
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr2"), None))),
            trusteeOrg.copy(identification = Some(TrustIdentificationOrgType(None, Some("utr3"), None)))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg.copy(utr = Some("utr1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getBusinessUtrs(identifier, Some(0), adding = false), Duration.Inf)

          result mustBe List("utr1", "utr2", "utr3")
        }
      }
    }

    ".getIndividualNinos" - {

      val passport = CombinedPassportOrIdCard("FR", "num", LocalDate.parse("2000-01-01"))

      "must return empty list" - {

        "when no individuals" in {

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(Nil)))

          val result = Await.result(service.getIndividualNinos(identifier, None, adding = true), Duration.Inf)

          result mustBe Nil
        }

        "when there are individuals but they don't have a NINo" in {

          val trustees = List(
            trusteeInd.copy(identification = None),
            trusteeInd.copy(identification = Some(passport))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd.copy(identification = passport)))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getIndividualNinos(identifier, None, adding = true), Duration.Inf)

          result mustBe Nil
        }

        "when there is an individual with a NINo but it's the same index as the one we're amending" in {

          val trustees = List(
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino")))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeOrg))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0), adding = false), Duration.Inf)

          result mustBe Nil
        }

        "when there is a lead individual with a NINo but it's what we're amending" in {

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd.copy(identification = NationalInsuranceNumber("nino"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(Nil)))

          val result = Await.result(service.getIndividualNinos(identifier, None, adding = false), Duration.Inf)

          result mustBe Nil
        }
      }

      "must return NINos" - {

        "when individuals have NINos and we're adding a new trustee (i.e. no index)" in {

          val trustees = List(
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino2"))),
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino3")))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd.copy(identification = NationalInsuranceNumber("nino1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getIndividualNinos(identifier, None, adding = true), Duration.Inf)

          result mustBe List("nino1", "nino2", "nino3")
        }

        "when individuals have NINos and we're amending the lead trustee (i.e. no index)" in {

          val trustees = List(
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino2"))),
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino3")))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd.copy(identification = NationalInsuranceNumber("nino1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getIndividualNinos(identifier, None, adding = false), Duration.Inf)

          result mustBe List("nino2", "nino3")
        }

        "when individuals have NINos and we're amending or promoting a trustee" in {

          val trustees = List(
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino2"))),
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino3")))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd.copy(identification = NationalInsuranceNumber("nino1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0), adding = false), Duration.Inf)

          result mustBe List("nino1", "nino3")
        }

        "when individuals have NINos and we're amending a different index" in {

          val trustees = List(
            trusteeOrg,
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino2"))),
            trusteeInd.copy(identification = Some(NationalInsuranceNumber("nino3")))
          )

          when(mockConnector.getLeadTrustee(any())(any(), any()))
            .thenReturn(Future.successful(leadTrusteeInd.copy(identification = NationalInsuranceNumber("nino1"))))

          when(mockConnector.getTrustees(any())(any(), any()))
            .thenReturn(Future.successful(Trustees(trustees)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0), adding = false), Duration.Inf)

          result mustBe List("nino1", "nino2", "nino3")
        }
      }
    }

  }

}
