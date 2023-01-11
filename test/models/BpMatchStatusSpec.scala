/*
 * Copyright 2023 HM Revenue & Customs
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
import models.BpMatchStatus._
import play.api.libs.json.{JsString, Json}

class BpMatchStatusSpec extends SpecBase {

  "BpMatchStatus" must {

    "deserialise and serialise" when {

      "FullyMatched" in {
        val json = JsString("01")
        val bpMatchStatus = json.as[BpMatchStatus]
        bpMatchStatus mustBe FullyMatched
        Json.toJson(bpMatchStatus) mustBe json
      }

      "Unmatched" in {
        val json = JsString("02")
        val bpMatchStatus = json.as[BpMatchStatus]
        bpMatchStatus mustBe Unmatched
        Json.toJson(bpMatchStatus) mustBe json
      }

      "NoMatchAttempted" in {
        val json = JsString("98")
        val bpMatchStatus = json.as[BpMatchStatus]
        bpMatchStatus mustBe NoMatchAttempted
        Json.toJson(bpMatchStatus) mustBe json
      }

      "FailedToMatch" in {
        val json = JsString("99")
        val bpMatchStatus = json.as[BpMatchStatus]
        bpMatchStatus mustBe FailedToMatch
        Json.toJson(bpMatchStatus) mustBe json
      }
    }
  }
}
