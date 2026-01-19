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

package generators.leadtrustee.individual

import generators.Generators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages.QuestionPage
import pages.leadtrustee.individual._
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(TelephoneNumberPage.type, JsValue)] ::
    arbitrary[(EmailAddressYesNoPage.type, JsValue)] ::
    arbitrary[(EmailAddressPage.type, JsValue)] ::
    arbitrary[(UkCitizenPage.type, JsValue)] ::
    arbitrary[(UkAddressPage.type, JsValue)] ::
    arbitrary[(PassportOrIdCardDetailsPage.type, JsValue)] ::
    arbitrary[(NonUkAddressPage.type, JsValue)] ::
    arbitrary[(NationalInsuranceNumberPage.type, JsValue)] ::
    arbitrary[(NamePage.type, JsValue)] ::
    arbitrary[(LiveInTheUkYesNoPage.type, JsValue)] ::
    arbitrary[(DateOfBirthPage.type, JsValue)] ::
    Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id <- nonEmptyString
        utr <- nonEmptyString
        sessionId <- nonEmptyString
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _ => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        internalId = id,
        identifier = utr,
        sessionId = sessionId,
        newId = s"$id-$utr-$sessionId",
        whenTrustSetup = LocalDate.now(),
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
