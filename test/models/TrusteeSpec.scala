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

package models

import base.SpecBase
import models.Constants.GB
import play.api.libs.json.{JsString, Json, __}

import java.time.LocalDate

class TrusteeSpec extends SpecBase {

  private val addressLine: String = "Line"
  private val postcode: String = "AB1 1AB"

  private val tel: String = "tel"

  private val country: String = "FR"

  private val startDate: String = "2018-02-01"

  "Trustee" when {

    "individual" when {

      val firstName = "First"
      val lastName = "Last"
      val name = Name(firstName, None, lastName)
      val dateOfBirth = "2008-02-01"
      val nino = "nino"
      val idDate: String = "2028-02-01"
      val idNumber: String = "1234567890"

      "4mld" when {

        "minimum data" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeInd": {
               |    "name": {
               |      "firstName": "$firstName",
               |      "lastName": "$lastName"
               |    },
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
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeInd").json.pick).get
        }

        "DOB, NINO and phone number" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeInd": {
               |    "name": {
               |      "firstName": "$firstName",
               |      "lastName": "$lastName"
               |    },
               |    "dateOfBirth": "$dateOfBirth",
               |    "identification": {
               |      "nino": "$nino"
               |    },
               |    "phoneNumber": "$tel",
               |    "entityStart": "$startDate",
               |    "provisional": true
               |  }
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[Trustee]

          result mustBe TrusteeIndividual(
            name = name,
            dateOfBirth = Some(LocalDate.parse(dateOfBirth)),
            phoneNumber = Some(tel),
            identification = Some(NationalInsuranceNumber(nino)),
            address = None,
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeInd").json.pick).get
        }

        "UK address and no passport/id card" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeInd": {
               |    "name": {
               |      "firstName": "$firstName",
               |      "lastName": "$lastName"
               |    },
               |    "identification": {
               |      "address": {
               |        "line1": "$addressLine",
               |        "line2": "$addressLine",
               |        "postCode": "$postcode"
               |      }
               |    },
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
            address = Some(UkAddress(addressLine, addressLine, None, None, postcode)),
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform(
            __.json.update((__ \ "trusteeInd" \ "identification" \ "address" \ "country").json.put(JsString("GB"))) andThen
              (__ \ "trusteeInd").json.pick
          ).get
        }

        "non-UK address and passport/id card" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeInd": {
               |    "name": {
               |      "firstName": "$firstName",
               |      "lastName": "$lastName"
               |    },
               |    "identification": {
               |      "address": {
               |        "line1": "$addressLine",
               |        "line2": "$addressLine",
               |        "country": "$country"
               |      },
               |      "passport": {
               |        "countryOfIssue": "$country",
               |        "number": "$idNumber",
               |        "expirationDate": "$idDate"
               |      }
               |    },
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
            identification = Some(CombinedPassportOrIdCard(country, idNumber, LocalDate.parse(idDate))),
            address = Some(NonUkAddress(addressLine, addressLine, None, country)),
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeInd").json.pick).get
        }
      }

      "5mld" when {

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
            mentalCapacityYesNo = Some(true),
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
            mentalCapacityYesNo = Some(false),
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeInd").json.pick).get
        }
      }
    }

    "organisation" when {

      val name = "Amazon"
      val utr = "utr"
      val email = "email@example.com"

      "4mld" when {

        "minimum data" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeOrg": {
               |    "name": "$name",
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
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeOrg").json.pick).get
        }

        "UTR, email and phone number" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeOrg": {
               |    "name": "$name",
               |    "identification": {
               |      "utr": "$utr"
               |    },
               |    "phoneNumber": "$tel",
               |    "email": "$email",
               |    "entityStart": "$startDate",
               |    "provisional": true
               |  }
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[Trustee]

          result mustBe TrusteeOrganisation(
            name = name,
            phoneNumber = Some(tel),
            email = Some(email),
            identification = Some(TrustIdentificationOrgType(None, Some(utr), None)),
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeOrg").json.pick).get
        }

        "UK address" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeOrg": {
               |    "name": "$name",
               |    "identification": {
               |      "address": {
               |        "line1": "$addressLine",
               |        "line2": "$addressLine",
               |        "postCode": "$postcode"
               |      }
               |    },
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
            identification = Some(TrustIdentificationOrgType(None, None, Some(UkAddress(addressLine, addressLine, None, None, postcode)))),
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform(
            __.json.update((__ \ "trusteeOrg" \ "identification" \ "address" \ "country").json.put(JsString("GB"))) andThen
              (__ \ "trusteeOrg").json.pick
          ).get
        }

        "non-UK address" in {

          val jsonStr =
            s"""
               |{
               |  "trusteeOrg": {
               |    "name": "$name",
               |    "identification": {
               |      "address": {
               |        "line1": "$addressLine",
               |        "line2": "$addressLine",
               |        "country": "$country"
               |      }
               |    },
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
            identification = Some(TrustIdentificationOrgType(None, None, Some(NonUkAddress(addressLine, addressLine, None, country)))),
            entityStart = LocalDate.parse(startDate),
            provisional = true
          )

          Json.toJson(result) mustBe json.transform((__ \ "trusteeOrg").json.pick).get
        }
      }

      "5mld" when {

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
  }
}
