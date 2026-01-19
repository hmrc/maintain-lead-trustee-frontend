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

import generators.{Generators, ModelGenerators}
import models.{Name, NonUkAddress, UkAddress}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.leadtrustee.individual._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {
  this : Generators =>

  implicit lazy val arbitraryTelephoneNumberUserAnswersEntry: Arbitrary[(TelephoneNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TelephoneNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmailAddressYesNoUserAnswersEntry: Arbitrary[(EmailAddressYesNoPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EmailAddressYesNoPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmailAddressUserAnswersEntry: Arbitrary[(EmailAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EmailAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUkCitizenUserAnswersEntry: Arbitrary[(UkCitizenPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[UkCitizenPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUkAddressUserAnswersEntry: Arbitrary[(UkAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[UkAddressPage.type]
        value <- arbitrary[UkAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPassportDetailsUserAnswersEntry: Arbitrary[(PassportOrIdCardDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PassportOrIdCardDetailsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNonUkAddressUserAnswersEntry: Arbitrary[(NonUkAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NonUkAddressPage.type]
        value <- arbitrary[NonUkAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNationalInsuranceNumberUserAnswersEntry: Arbitrary[(NationalInsuranceNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NationalInsuranceNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNameUserAnswersEntry: Arbitrary[(NamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NamePage.type]
        value <- arbitrary[Name].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryLiveInTheUkYesNoPageUserAnswersEntry: Arbitrary[(LiveInTheUkYesNoPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[LiveInTheUkYesNoPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateOfBirthUserAnswersEntry: Arbitrary[(DateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

}
