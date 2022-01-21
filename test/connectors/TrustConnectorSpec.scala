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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.Constants.INDIVIDUAL_TRUSTEE
import models.YesNoDontKnow.DontKnow
import models._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._

import java.time.LocalDate

class TrustConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {

  private def getLeadTrusteeUrl(identifier: String): String = s"/trusts/trustees/$identifier/transformed/lead-trustee"
  private def getTrustDetailsUrl(identifier: String) = s"/trusts/trust-details/$identifier/transformed"
  private def getTrusteesUrl(identifier: String) = s"/trusts/trustees/$identifier/transformed/trustee"
  private def removeTrusteeUrl(identifier: String) = s"/trusts/trustees/$identifier/remove"
  private def promoteTrusteeUrl(identifier: String, index: Int) = s"/trusts/trustees/promote/$identifier/$index"
  private def isTrust5mldUrl(identifier: String) = s"/trusts/$identifier/is-trust-5mld"

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
      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo("/trusts/trustees/amend-lead/UTRUTRUTR"))
          .willReturn(ok)
      )

      val result = connector.amendLeadTrustee("UTRUTRUTR", arbitraryLeadTrusteeIndividual.arbitrary.sample.get)
      result.futureValue.status mustBe OK

      application.stop()
    }
  }

  "TrustConnector addTrustee" must {
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
        post(urlEqualTo("/trusts/trustees/add/UTRUTRUTR"))
          .willReturn(ok)
      )

      val result = connector.addTrustee("UTRUTRUTR", arbitraryTrusteeIndividual.arbitrary.sample.get)
      result.futureValue.status mustBe OK

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
        post(urlEqualTo("/trusts/trustees/add/UTRUTRUTR"))
          .willReturn(badRequest)
      )

      val result = connector.addTrustee("UTRUTRUTR", arbitraryTrusteeIndividual.arbitrary.sample.get)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }
  }

  "TrustConnector getLeadTrustee" must {

    "must return playback data inside a Processed trust" in {
      val identifier = "1000000007"
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
        get(urlEqualTo(getLeadTrusteeUrl(identifier)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getLeadTrustee(identifier)

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

  "TrustConnector getTrustsDetails" in {
    val identifier = "1000000007"
    val date: LocalDate = LocalDate.parse("2019-02-03")
    val json = Json.parse(
      """
        |{
        | "startDate": "2019-02-03",
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
      get(urlEqualTo(getTrustDetailsUrl(identifier)))
        .willReturn(okJson(json.toString))
    )

    val processed = connector.getTrustDetails(identifier)

    whenReady(processed) {
      r =>
        r mustBe TrustDetails(startDate = date, None)
    }
  }

  "TrustConnector removeTrustee" must {

    "return Ok when the request is successful" in {

      val identifier = "1000000008"

      val trustee = RemoveTrustee(
        `type` = INDIVIDUAL_TRUSTEE,
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
        put(urlEqualTo(removeTrusteeUrl(identifier)))
          .willReturn(ok)
      )

      val result = connector.removeTrustee(identifier, trustee)

      result.futureValue.status mustBe OK

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {

      val identifier = "1000000008"

      val trustee = RemoveTrustee(
        `type` = INDIVIDUAL_TRUSTEE,
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
        put(urlEqualTo(removeTrusteeUrl(identifier)))
          .willReturn(badRequest)
      )

      val result = connector.removeTrustee(identifier, trustee)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }

  }

  "TrustConnector getTrustees" must {

    "must return playback data inside a Processed trust" in {

      val identifier = "1000000008"

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
        get(urlEqualTo(getTrusteesUrl(identifier)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustees(identifier)

      whenReady(processed) { trustees =>
        trustees mustBe Trustees(
          List(
            TrusteeIndividual(
              name = Name(firstName = "1234567890 QwErTyUiOp ,.(/)&'- name", middleName = None, lastName = "1234567890 QwErTyUiOp ,.(/)&'- name"),
              dateOfBirth = Some(LocalDate.parse("1983-09-24")),
              phoneNumber = None,
              identification = Some(NationalInsuranceNumber("JS123456A")),
              None,
              None,
              entityStart = LocalDate.parse("2019-02-28"),
              provisional = true,
              mentalCapacityYesNo = Some(DontKnow)
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
              countryOfResidence = None,
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

      val identifier = "1000000008"
      val index = 0

      val newLeadTrustee = LeadTrusteeIndividual(
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
        post(urlEqualTo(promoteTrusteeUrl(identifier, index)))
          .willReturn(ok)
      )

      val result = connector.promoteTrustee(identifier, index, newLeadTrustee)

      result.futureValue.status mustBe OK

      application.stop()
    }

    "return Bad Request when the request is unsuccessful" in {

      val identifier = "1000000008"
      val index = 0

      val newLeadTrustee = LeadTrusteeIndividual(
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
        post(urlEqualTo(promoteTrusteeUrl(identifier, index)))
          .willReturn(badRequest)
      )

      val result = connector.promoteTrustee(identifier, index, newLeadTrustee)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }

  }

  "TrustConnector amendTrustee" must {

    val index = 0
    val identifier = "UTRUTRUTR"

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
        post(urlEqualTo(s"/trusts/trustees/amend/$identifier/$index"))
          .willReturn(ok)
      )

      val result = connector.amendTrustee(identifier, index, arbitraryTrusteeIndividual.arbitrary.sample.get)
      result.futureValue.status mustBe OK

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
        post(urlEqualTo(s"/trusts/trustees/amend/$identifier/$index"))
          .willReturn(badRequest)
      )

      val result = connector.amendTrustee(identifier, index, arbitraryTrusteeIndividual.arbitrary.sample.get)

      result.map(response => response.status mustBe BAD_REQUEST)

      application.stop()
    }
  }

  "TrustConnector isTrust5mld" must {

    "return true" when {
      "untransformed data is 5mld" in {
        val identifier = "1000000007"
        val json = JsBoolean(true)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(isTrust5mldUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.isTrust5mld(identifier)

        whenReady(processed) {
          r =>
            r mustBe true
        }
      }
    }

    "return false" when {
      "untransformed data is 4mld" in {
        val identifier = "1000000007"
        val json = JsBoolean(false)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(isTrust5mldUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.isTrust5mld(identifier)

        whenReady(processed) {
          r =>
            r mustBe false
        }
      }
    }
  }
}
