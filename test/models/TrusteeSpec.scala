/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import base.SpecBase
import models.Constants.GB
import models.YesNoDontKnow.{DontKnow, No, Yes}
import play.api.libs.json.{Json, __}

import java.time.LocalDate

class TrusteeSpec extends SpecBase {

  private val country: String = "FR"

  private val startDate: String = "2018-02-01"

  "Trustee" when {

    "individual" when {

      val firstName = "First"
      val lastName  = "Last"
      val name      = Name(firstName, None, lastName)

      "UK nationality/residency and legally capable" in {

        val jsonStr =
          s"""
             |{
             |  "trusteeInd": {
             |    "name": {
             |      "firstName": "$firstName",
             |      "lastName": "$lastName"
             |    },
             |    "countryOfResidence": "$GB",
             |    "nationality": "$GB",
             |    "legallyIncapable": false,
             |    "entityStart": "$startDate",
             |    "provisional": true
             |  }
             |}""".stripMargin

        val json = Json.parse(jsonStr)

        val result = json.as[Trustee]

        result mustBe TrusteeIndividual(
          name = name,
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          countryOfResidence = Some(GB),
          nationality = Some(GB),
          mentalCapacityYesNo = Some(Yes),
          entityStart = LocalDate.parse(startDate),
          provisional = true
        )

        Json.toJson(result) mustBe json.transform((__ \ "trusteeInd").json.pick).get
      }

      "non-UK nationality/residency and legally incapable" in {

        val jsonStr =
          s"""
             |{
             |  "trusteeInd": {
             |    "name": {
             |      "firstName": "$firstName",
             |      "lastName": "$lastName"
             |    },
             |    "countryOfResidence": "$country",
             |    "nationality": "$country",
             |    "legallyIncapable": true,
             |    "entityStart": "$startDate",
             |    "provisional": true
             |  }
             |}""".stripMargin

        val json = Json.parse(jsonStr)

        val result = json.as[Trustee]

        result mustBe TrusteeIndividual(
          name = name,
          dateOfBirth = None,
          phoneNumber = None,
          identification = None,
          address = None,
          countryOfResidence = Some(country),
          nationality = Some(country),
          mentalCapacityYesNo = Some(No),
          entityStart = LocalDate.parse(startDate),
          provisional = true
        )

        Json.toJson(result) mustBe json.transform((__ \ "trusteeInd").json.pick).get
      }
    }

    "organisation" when {

      val name = "Amazon"

      "UK country of residence" in {

        val jsonStr =
          s"""
             |{
             |  "trusteeOrg": {
             |    "name": "$name",
             |    "countryOfResidence": "$GB",
             |    "entityStart": "$startDate",
             |    "provisional": true
             |  }
             |}""".stripMargin

        val json = Json.parse(jsonStr)

        val result = json.as[Trustee]

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = None,
          countryOfResidence = Some(GB),
          entityStart = LocalDate.parse(startDate),
          provisional = true
        )

        Json.toJson(result) mustBe json.transform((__ \ "trusteeOrg").json.pick).get
      }

      "non-UK country of residence" in {

        val jsonStr =
          s"""
             |{
             |  "trusteeOrg": {
             |    "name": "$name",
             |    "countryOfResidence": "$country",
             |    "entityStart": "$startDate",
             |    "provisional": true
             |  }
             |}""".stripMargin

        val json = Json.parse(jsonStr)

        val result = json.as[Trustee]

        result mustBe TrusteeOrganisation(
          name = name,
          phoneNumber = None,
          email = None,
          identification = None,
          countryOfResidence = Some(country),
          entityStart = LocalDate.parse(startDate),
          provisional = true
        )

        Json.toJson(result) mustBe json.transform((__ \ "trusteeOrg").json.pick).get
      }
    }
  }

  "Trustee mental capacity eligibility" when {

    def individual(answer: Option[YesNoDontKnow]): TrusteeIndividual = TrusteeIndividual(
      name = Name("First", None, "Last"),
      dateOfBirth = None,
      phoneNumber = None,
      identification = None,
      address = None,
      mentalCapacityYesNo = answer,
      entityStart = LocalDate.parse(startDate),
      provisional = false
    )

    "individual" when {

      "mental capacity answer is Yes" must {
        val trusteeIndividual = individual(Some(Yes))

        "not lack mental capacity, and be eligible to be lead trustee" in {
          trusteeIndividual.lacksMentalCapacity       mustBe false
          trusteeIndividual.isEligibleToBeLeadTrustee mustBe true
        }
      }

      "mental capacity answer is I don't know" must {
        val trusteeIndividual = individual(Some(DontKnow))

        "not lack mental capacity, and be eligible to be lead trustee" in {
          trusteeIndividual.lacksMentalCapacity       mustBe false
          trusteeIndividual.isEligibleToBeLeadTrustee mustBe true
        }
      }

      "mental capacity answer is No" must {
        val trusteeIndividual = individual(Some(No))

        "lack mental capacity, and not be eligible to be lead trustee" in {
          trusteeIndividual.lacksMentalCapacity       mustBe true
          trusteeIndividual.isEligibleToBeLeadTrustee mustBe false
        }
      }

      "mental capacity question has not been answered" must {
        val trusteeIndividual = individual(None)

        "not lack mental capacity, and be eligible to be lead trustee" in {
          trusteeIndividual.lacksMentalCapacity       mustBe false
          trusteeIndividual.isEligibleToBeLeadTrustee mustBe true
        }
      }
    }

    "organisation" must {
      "never lack mental capacity and always be eligible to be lead trustee" in {
        val org = TrusteeOrganisation(
          name = "Big Company",
          identification = None,
          entityStart = LocalDate.parse(startDate),
          provisional = false
        )

        org.lacksMentalCapacity       mustBe false
        org.isEligibleToBeLeadTrustee mustBe true
      }
    }

    "read from the backend" when {

      "legallyIncapable is absent" must {
        "be treated as having mental capacity and be eligible" in {
          val json = Json.parse(
            s"""
               |{
               |  "trusteeInd": {
               |    "name": { "firstName": "First", "lastName": "Last" },
               |    "entityStart": "$startDate",
               |    "provisional": false
               |  }
               |}""".stripMargin
          )

          val result = json.as[Trustee]

          result.asInstanceOf[TrusteeIndividual].mentalCapacityYesNo mustBe Some(DontKnow)
          result.lacksMentalCapacity                                 mustBe false
          result.isEligibleToBeLeadTrustee                           mustBe true
        }
      }

      "legallyIncapable is true" must {
        "be treated as lacking mental capacity and not be eligible" in {
          val json = Json.parse(
            s"""
               |{
               |  "trusteeInd": {
               |    "name": { "firstName": "First", "lastName": "Last" },
               |    "legallyIncapable": true,
               |    "entityStart": "$startDate",
               |    "provisional": false
               |  }
               |}""".stripMargin
          )

          val result = json.as[Trustee]

          result.lacksMentalCapacity       mustBe true
          result.isEligibleToBeLeadTrustee mustBe false
        }
      }
    }
  }

}
