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

package models

import base.SpecBase
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class TrusteeIndividualSpec extends SpecBase with Matchers with OptionValues {

  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "TrusteeIndividual reads" must {

    "parse the mental capacity question when trustee has mental capacity" in {
      val json = Json.parse(
        s"""
          |{
          | "name": {
          |   "firstName": "John",
          |   "lastName": "Smith"
          | },
          | "legallyIncapable": false,
          | "entityStart": "$date",
          | "provisional": false
          |}
          |""".stripMargin)

      json.as[TrusteeIndividual] mustBe TrusteeIndividual(
        name = Name("John", None, "Smith"),
        dateOfBirth = None,
        phoneNumber = None,
        identification = None,
        address = None,
        countryOfResidence = None,
        nationality = None,
        mentalCapacityYesNo = Some(YesNoDontKnow.Yes),
        entityStart = date,
        provisional = false
      )
    }

    "parse the mental capacity question when trustee does not have mental capacity" in {
      val json = Json.parse(
        s"""
           |{
           | "name": {
           |   "firstName": "John",
           |   "lastName": "Smith"
           | },
           | "legallyIncapable": true,
           | "entityStart": "$date",
           | "provisional": false
           |}
           |""".stripMargin)

      json.as[TrusteeIndividual] mustBe TrusteeIndividual(
        name = Name("John", None, "Smith"),
        dateOfBirth = None,
        phoneNumber = None,
        identification = None,
        address = None,
        countryOfResidence = None,
        nationality = None,
        mentalCapacityYesNo = Some(YesNoDontKnow.No),
        entityStart = date,
        provisional = false
      )
    }

    "parse the mental capacity question when mental capacity is not known" in {
      val json = Json.parse(
        s"""
         |{
         | "name": {
         |   "firstName": "John",
         |   "lastName": "Smith"
         | },
         | "entityStart": "$date",
         | "provisional": false
         |}
         |""".stripMargin)

      json.as[TrusteeIndividual] mustBe TrusteeIndividual(
        name = Name("John", None, "Smith"),
        dateOfBirth = None,
        phoneNumber = None,
        identification = None,
        address = None,
        countryOfResidence = None,
        nationality = None,
        mentalCapacityYesNo = Some(YesNoDontKnow.DontKnow),
        entityStart = date,
        provisional = false
      )
    }

  }

}
