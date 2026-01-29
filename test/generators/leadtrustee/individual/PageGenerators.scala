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

import org.scalacheck.Arbitrary
import pages.leadtrustee.individual._

trait PageGenerators {

  implicit lazy val arbitraryTelephoneNumberPage: Arbitrary[TelephoneNumberPage.type] =
    Arbitrary(TelephoneNumberPage)

  implicit lazy val arbitraryEmailAddressYesNoPage: Arbitrary[EmailAddressYesNoPage.type] =
    Arbitrary(EmailAddressYesNoPage)

  implicit lazy val arbitraryEmailAddressPage: Arbitrary[EmailAddressPage.type] =
    Arbitrary(EmailAddressPage)

  implicit lazy val arbitraryUkCitizenPage: Arbitrary[UkCitizenPage.type] =
    Arbitrary(UkCitizenPage)

  implicit lazy val arbitraryUkAddressPage: Arbitrary[UkAddressPage.type] =
    Arbitrary(UkAddressPage)

  implicit lazy val arbitraryPassportDetailsPage: Arbitrary[PassportOrIdCardDetailsPage.type] =
    Arbitrary(PassportOrIdCardDetailsPage)

  implicit lazy val arbitraryNonUkAddressPage: Arbitrary[NonUkAddressPage.type] =
    Arbitrary(NonUkAddressPage)

  implicit lazy val arbitraryNationalInsuranceNumberPage: Arbitrary[NationalInsuranceNumberPage.type] =
    Arbitrary(NationalInsuranceNumberPage)

  implicit lazy val arbitraryNamePage: Arbitrary[NamePage.type] =
    Arbitrary(NamePage)

  implicit lazy val arbitraryLiveInTheUkYesNoPagePage: Arbitrary[LiveInTheUkYesNoPage.type] =
    Arbitrary(LiveInTheUkYesNoPage)

  implicit lazy val arbitraryDateOfBirthPage: Arbitrary[DateOfBirthPage.type] =
    Arbitrary(DateOfBirthPage)

}
