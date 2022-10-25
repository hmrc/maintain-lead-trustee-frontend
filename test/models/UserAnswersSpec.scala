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
import generators.ModelGenerators
import models.BpMatchStatus.FullyMatched
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.leadtrustee.individual.{BpMatchStatusPage, NationalInsuranceNumberPage}
import play.api.libs.json.{JsPath, Json}

import java.time.LocalDate
import scala.util.Success

class UserAnswersSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "UserAnswers" when {

    ".deleteAtPath" must {
      "delete data removes data from the Json Object" in {
        val json = Json.obj(
          "field" -> Json.obj(
            "innerfield" -> "value"
          )
        )

        val ua = new UserAnswers(
          "ID",
          "UTRUTRUTR",
          "sessionId",
           newId = s"ID-UTRUTRUTR-sessionId",
          LocalDate.of(1999, 10, 20),
          json
        )

        ua.deleteAtPath(JsPath \ "field" \ "innerfield") mustBe Success(ua.copy(data = Json.obj(
          "field" -> Json.obj()
        )))
      }
    }

    ".isLeadTrusteeMatched" must {

      "return true" when {
        "lead trustee fully matched with a NINO" in {

          val userAnswers = emptyUserAnswers
            .set(BpMatchStatusPage, FullyMatched).success.value
            .set(NationalInsuranceNumberPage, "nino").success.value

          userAnswers.isLeadTrusteeMatched mustEqual true
        }
      }

      "return false" when {
        "any of these false: fully matched; has nino" in {

          val gen = arbitrary[(BpMatchStatus, Option[String])]

          forAll(gen.suchThat(x => !(x._1 == FullyMatched && x._2.isDefined))) {
            x =>
              val userAnswers = emptyUserAnswers
                .set(BpMatchStatusPage, x._1).success.value
                .set(NationalInsuranceNumberPage, x._2).success.value

              userAnswers.isLeadTrusteeMatched mustEqual false
          }
        }
      }
    }
  }
}
