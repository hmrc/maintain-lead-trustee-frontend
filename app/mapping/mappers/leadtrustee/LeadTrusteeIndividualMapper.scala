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

package mapping.mappers.leadtrustee

import models._
import pages.QuestionPage
import pages.leadtrustee.individual._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class LeadTrusteeIndividualMapper extends LeadTrusteeMapper[LeadTrusteeIndividual] {

  override val reads: Reads[LeadTrusteeIndividual] = (
    BpMatchStatusPage.path.readNullable[BpMatchStatus] and
      NamePage.path.read[Name] and
      DateOfBirthPage.path.read[LocalDate] and
      TelephoneNumberPage.path.read[String] and
      EmailAddressYesNoPage.path.read[Boolean].flatMap[Option[String]] {
        case true => EmailAddressPage.path.read[String].map(Some(_))
        case false => Reads( _=> JsSuccess(None))
      } and
      UkCitizenPage.path.read[Boolean].flatMap {
        case true => NationalInsuranceNumberPage.path.read[String].map(NationalInsuranceNumber(_)).widen[IndividualIdentification]
        case false => PassportOrIdCardDetailsPage.path.read[CombinedPassportOrIdCard].widen[IndividualIdentification]
      } and
      readAddress and
      readCountryOfResidence and
      readCountryOfResidenceOrNationality(CountryOfNationalityInTheUkYesNoPage, CountryOfNationalityPage)
    )(LeadTrusteeIndividual.apply _)

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

}
