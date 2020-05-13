/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.{LeadTrusteeIndividual, LeadTrusteeOrganisation, Name, NationalInsuranceNumber, RemoveTrustee, TrustIdentificationOrgType, TrustStartDate, TrusteeIndividual, TrusteeOrganisation, Trustees, UkAddress}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class TrustConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private def getLeadTrusteeUrl(utr: String): String = s"/trusts/$utr/transformed/lead-trustee"

  private def getTrustStartDateUrl(utr: String): String = s"/trusts/$utr/trust-details"

  private def getTrusteesUrl(utr: String) = s"/trusts/$utr/transformed/trustees"

  private def removeTrusteeUrl(utr: String) = s"/trusts/trustees/$utr/remove"

  private def promoteTrusteeUrl(utr: String, index: Int) = s"/trusts/trustees/promote/$utr/$index"

  private def amendTrusteeUrl(utr: String, index: Int) = s"/trusts/trustees/amend/$utr/$index"

  private def amendLeadTrusteeUrl(utr: String) = s"/trusts/trustees/amend-lead/$utr"

  private def addTrusteeUrl(utr: String) = s"/trusts/trustees/add/$utr"

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  "TrustConnector amendLeadTrustee" must {
    "Be fine when request is successful" in {
      val utr = "1000000007"
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(amendLeadTrusteeUrl(utr)))
          .willReturn(ok)
      )

      val result = connector.amendLeadTrustee(utr, arbitraryLeadTrusteeIndividual.arbitrary.sample.get)
      result.futureValue.status mustBe (OK)

      application.stop()
    }
  }

  "TrustConnector addTrustee" must {
    val utr = "1000000007"
    "Return Ok when the request is successful" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(addTrusteeUrl(utr)))
          .willReturn(ok)
      )

      val result = connector.addTrustee(utr, arbitraryTrusteeIndividual.arbitrary.sample.get)
      result.futureValue.status mustBe (OK)

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(addTrusteeUrl(utr)))
          .willReturn(badRequest)
      )

      val result = connector.addTrustee(utr, arbitraryTrusteeIndividual.arbitrary.sample.get)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }
  }

  "TrustConnector getLeadTrustee" must {

    "must return playback data inside a Processed trust" in {
      val utr = "1000000007"
      val json = Json.obj(
        "lineNo" -> "lineNo",
        "name" -> "name",
        "dateOfBirth" -> "1956-01-01",
        "phoneNumber" -> "phoneNumber",
        "identification" -> Json.obj(
          "utr" -> "anUtr",
          "address" -> Json.obj(
            "line1" -> "address1",
            "line2" -> "address2",
            "postCode" -> "Postcode"
          )
        ),
        "entityStart" -> "now"
      )

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(getLeadTrusteeUrl(utr)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getLeadTrustee(utr)

      whenReady(processed) { leadTrustee =>
        leadTrustee mustBe LeadTrusteeOrganisation(
          "name",
          "phoneNumber",
          None,
          Some("anUtr"),
          UkAddress("address1", "address2", None, None, "Postcode")
        )
      }
      application.stop()
    }
  }

  "TrustConnector getTrustStartDate" must {

    "must return trust start date" in {
      val utr = "1000000007"

      val json = Json.parse(
        """
          |{
          | "startDate": "1920-03-28",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(getTrustStartDateUrl(utr)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustStartDate(utr)

      whenReady(processed) { startDate =>
        startDate mustBe TrustStartDate(
          "1920-03-28"
        )
      }
      application.stop()
    }
  }

  "TrustConnector removeTrustee" must {

    "return Ok when the request is successful" in {

      val utr = "1000000008"

      val trustee = RemoveTrustee(
        index = 0,
        endDate = LocalDate.now()
      )

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        put(urlEqualTo(removeTrusteeUrl(utr)))
          .willReturn(ok)
      )

      val result = connector.removeTrustee(utr, trustee)

      result.futureValue.status mustBe (OK)

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {

      val utr = "1000000008"

      val trustee = RemoveTrustee(
        index = 0,
        endDate = LocalDate.now()
      )

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        put(urlEqualTo(removeTrusteeUrl(utr)))
          .willReturn(badRequest)
      )

      val result = connector.removeTrustee(utr, trustee)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }

  }

  "TrustConnector getTrustees" must {

    "must return playback data inside a Processed trust" in {

      val utr = "1000000008"

      val json = Json.parse(
        """
          |{
          | "trustees": [
          |   {
          |     "trusteeInd": {
          |       "name": {
          |         "firstName": "1234567890 QwErTyUiOp ,.(/)&'- name",
          |         "lastName": "1234567890 QwErTyUiOp ,.(/)&'- name"
          |       },
          |       "dateOfBirth": "1983-09-24",
          |       "identification": {
          |         "nino": "JS123456A"
          |       },
          |       "entityStart": "2019-02-28",
          |       "provisional": true
          |     }
          |   },
          |   {
          |     "trusteeOrg": {
          |       "name": "Trustee Org",
          |       "identification": {
          |         "utr": "1234567890"
          |       },
          |       "entityStart": "2019-02-28",
          |       "provisional": false
          |     }
          |   }
          | ]
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(getTrusteesUrl(utr)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustees(utr)

      whenReady(processed) { trustees =>
        trustees mustBe Trustees(
          List(
            TrusteeIndividual(
              name = Name(firstName = "1234567890 QwErTyUiOp ,.(/)&'- name", middleName = None, lastName = "1234567890 QwErTyUiOp ,.(/)&'- name"),
              dateOfBirth = Some(LocalDate.parse("1983-09-24")),
              phoneNumber = None,
              identification = Some(NationalInsuranceNumber("JS123456A")),
              None,
              entityStart = LocalDate.parse("2019-02-28"),
              provisional = true
            ),
            TrusteeOrganisation(
              name = "Trustee Org",
              phoneNumber = None,
              email = None,
              identification = Some(TrustIdentificationOrgType(
                utr = Some("1234567890"),
                safeId = None,
                address = None
              )),
              entityStart = LocalDate.parse("2019-02-28"),
              provisional = false
            )
          )
        )
      }

      application.stop()
    }
  }

  "TrustConnector promoteTrustee" must {

    "return Ok when the request is successful" in {

      val utr = "1000000008"
      val index = 0

      val newLeadTrustee = LeadTrusteeIndividual(
        name = Name(
          firstName = "Lead First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfBirth = LocalDate.parse("2010-10-10"),
        phoneNumber = "+446565657",
        email = None,
        identification = NationalInsuranceNumber("JP121212A"),
        address = None
      )

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(promoteTrusteeUrl(utr, index)))
          .willReturn(ok)
      )

      val result = connector.promoteTrustee(utr, index, newLeadTrustee)

      result.futureValue.status mustBe (OK)

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {

      val utr = "1000000008"
      val index = 0

      val newLeadTrustee = LeadTrusteeIndividual(
        name = Name(
          firstName = "Lead First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfBirth = LocalDate.parse("2010-10-10"),
        phoneNumber = "+446565657",
        email = None,
        identification = NationalInsuranceNumber("JP121212A"),
        address = None
      )

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(promoteTrusteeUrl(utr, index)))
          .willReturn(badRequest)
      )

      val result = connector.promoteTrustee(utr, index, newLeadTrustee)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }

  }

  "TrustConnector amendTrustee" must {

    val index = 0
    val utr = "UTRUTRUTR"

    "Return Ok when the request is successful" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(amendTrusteeUrl(utr, index)))
          .willReturn(ok)
      )

      val result = connector.amendTrustee(utr, index, arbitraryTrusteeIndividual.arbitrary.sample.get)
      result.futureValue.status mustBe (OK)

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(amendTrusteeUrl(utr, index)))
          .willReturn(badRequest)
      )

      val result = connector.amendTrustee(utr, index, arbitraryTrusteeIndividual.arbitrary.sample.get)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }
  }

}
