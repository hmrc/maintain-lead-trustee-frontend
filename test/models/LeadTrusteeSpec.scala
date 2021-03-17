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

class LeadTrusteeSpec extends SpecBase {

  private val addressLine: String = "Line"
  private val postcode: String = "AB1 1AB"

  private val tel: String = "tel"
  private val email: String = "email@example.com"

  private val country: String = "FR"

  "LeadTrustee" when {

    "individual" when {

      val firstName = "First"
      val lastName = "Last"
      val name = Name(firstName, None, lastName)
      val dateOfBirth = "2008-02-01"
      val nino = "nino"
      val idDate: String = "2028-02-01"
      val idNumber: String = "1234567890"

      "4mld" when {

        "NINO and UK address" in {

          val jsonStr =
            s"""
               |{
               |  "name": {
               |    "firstName": "$firstName",
               |    "lastName": "$lastName"
               |  },
               |  "dateOfBirth": "$dateOfBirth",
               |  "phoneNumber": "$tel",
               |  "identification": {
               |    "nino": "$nino",
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "postCode": "$postcode"
               |    }
               |  }
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = LocalDate.parse(dateOfBirth),
            phoneNumber = tel,
            email = None,
            identification = NationalInsuranceNumber(nino),
            address = UkAddress(addressLine, addressLine, None, None, postcode)
          )

          Json.toJson(result) mustBe json.transform(
            __.json.update((__ \ "identification" \ "address" \ "country").json.put(JsString("GB"))) andThen
              __.json.pick
          ).get
        }

        "passport/id card and UK address" in {

          val jsonStr =
            s"""
               |{
               |  "name": {
               |    "firstName": "$firstName",
               |    "lastName": "$lastName"
               |  },
               |  "dateOfBirth": "$dateOfBirth",
               |  "phoneNumber": "$tel",
               |  "email": "$email",
               |  "identification": {
               |    "passport": {
               |      "countryOfIssue": "$country",
               |      "number": "$idNumber",
               |      "expirationDate": "$idDate"
               |    },
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "country": "$country"
               |    }
               |  }
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = LocalDate.parse(dateOfBirth),
            phoneNumber = tel,
            email = Some(email),
            identification = CombinedPassportOrIdCard(country, idNumber, LocalDate.parse(idDate)),
            address = NonUkAddress(addressLine, addressLine, None, country)
          )

          Json.toJson(result) mustBe json
        }
      }

      "5mld" when {

        "UK nationality/residency" in {

          val jsonStr =
            s"""
               |{
               |  "name": {
               |    "firstName": "$firstName",
               |    "lastName": "$lastName"
               |  },
               |  "dateOfBirth": "$dateOfBirth",
               |  "phoneNumber": "$tel",
               |  "identification": {
               |    "nino": "$nino",
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "postCode": "$postcode"
               |    }
               |  },
               |  "nationality": "$GB",
               |  "countryOfResidence": "$GB"
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = LocalDate.parse(dateOfBirth),
            phoneNumber = tel,
            email = None,
            identification = NationalInsuranceNumber(nino),
            address = UkAddress(addressLine, addressLine, None, None, postcode),
            countryOfResidence = Some(GB),
            nationality = Some(GB)
          )

          Json.toJson(result) mustBe json.transform(
            __.json.update((__ \ "identification" \ "address" \ "country").json.put(JsString("GB"))) andThen
              __.json.pick
          ).get
        }

        "non-UK nationality/residency and legally incapable" in {

          val jsonStr =
            s"""
               |{
               |  "name": {
               |    "firstName": "$firstName",
               |    "lastName": "$lastName"
               |  },
               |  "dateOfBirth": "$dateOfBirth",
               |  "phoneNumber": "$tel",
               |  "email": "$email",
               |  "identification": {
               |    "passport": {
               |      "countryOfIssue": "$country",
               |      "number": "$idNumber",
               |      "expirationDate": "$idDate"
               |    },
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "country": "$country"
               |    }
               |  },
               |  "nationality": "$country",
               |  "countryOfResidence": "$country"
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeIndividual(
            name = name,
            dateOfBirth = LocalDate.parse(dateOfBirth),
            phoneNumber = tel,
            email = Some(email),
            identification = CombinedPassportOrIdCard(country, idNumber, LocalDate.parse(idDate)),
            address = NonUkAddress(addressLine, addressLine, None, country),
            countryOfResidence = Some(country),
            nationality = Some(country)
          )

          Json.toJson(result) mustBe json
        }
      }
    }

    "organisation" when {

      val name = "Amazon"
      val utr = "utr"

      "4mld" when {

        "UTR and UK address" in {

          val jsonStr =
            s"""
               |{
               |  "name": "$name",
               |  "phoneNumber": "$tel",
               |  "identification": {
               |    "utr": "$utr",
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "postCode": "$postcode"
               |    }
               |  }
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = tel,
            email = None,
            utr = Some(utr),
            address = UkAddress(addressLine, addressLine, None, None, postcode)
          )

          Json.toJson(result) mustBe json.transform(
            __.json.update((__ \ "identification" \ "address" \ "country").json.put(JsString("GB"))) andThen
              __.json.pick
          ).get
        }

        "non-UK address and email" in {

          val jsonStr =
            s"""
               |{
               |  "name": "$name",
               |  "phoneNumber": "$tel",
               |  "identification": {
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "country": "$country"
               |    }
               |  },
               |  "email": "$email"
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = tel,
            email = Some(email),
            utr = None,
            address = NonUkAddress(addressLine, addressLine, None, country)
          )

          Json.toJson(result) mustBe json
        }
      }

      "5mld" when {

        "UK country of residence" in {

          val jsonStr =
            s"""
               |{
               |  "name": "$name",
               |  "phoneNumber": "$tel",
               |  "identification": {
               |    "utr": "$utr",
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "postCode": "$postcode"
               |    }
               |  },
               |  "countryOfResidence": "$GB"
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = tel,
            email = None,
            utr = Some(utr),
            address = UkAddress(addressLine, addressLine, None, None, postcode),
            countryOfResidence = Some(GB)
          )

          Json.toJson(result) mustBe json.transform(
            __.json.update((__ \ "identification" \ "address" \ "country").json.put(JsString("GB"))) andThen
              __.json.pick
          ).get
        }

        "non-UK country of residence" in {

          val jsonStr =
            s"""
               |{
               |  "name": "$name",
               |  "phoneNumber": "$tel",
               |  "identification": {
               |    "address": {
               |      "line1": "$addressLine",
               |      "line2": "$addressLine",
               |      "country": "$country"
               |    }
               |  },
               |  "countryOfResidence": "$country",
               |  "email": "$email"
               |}""".stripMargin

          val json = Json.parse(jsonStr)

          val result = json.as[LeadTrustee]

          result mustBe LeadTrusteeOrganisation(
            name = name,
            phoneNumber = tel,
            email = Some(email),
            utr = None,
            address = NonUkAddress(addressLine, addressLine, None, country),
            countryOfResidence = Some(country)
          )

          Json.toJson(result) mustBe json
        }
      }
    }
  }
}
