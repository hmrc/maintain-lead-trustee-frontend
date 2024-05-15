/*
 * Copyright 2024 HM Revenue & Customs
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

package mapping.mappers.trustee

import models._
import pages.QuestionPage
import pages.trustee.individual._
import pages.trustee.individual.add._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class TrusteeIndividualMapper extends TrusteeMapper[TrusteeIndividual] {

  override val reads: Reads[TrusteeIndividual] = (
    NamePage.path.read[Name] and
      DateOfBirthPage.path.readNullable[LocalDate] and
      Reads(_ => JsSuccess(None)) and
      readIdentification and
      readAddress and
      readCountryOfResidence and
      readCountryOfNationality and
      readMentalCapacity and
      WhenAddedPage.path.read[LocalDate] and
      Reads(_ => JsSuccess(true))
    )(TrusteeIndividual.apply _)

  private def readIdentification: Reads[Option[IndividualIdentification]] = {
    NationalInsuranceNumberYesNoPage.path.readNullable[Boolean].flatMap[Option[IndividualIdentification]] {
      case Some(true) => NationalInsuranceNumberPage.path.read[String].map(nino => Some(NationalInsuranceNumber(nino)))
      case _ => readPassportOrIdCard
    }
  }

  private def readPassportOrIdCard: Reads[Option[IndividualIdentification]] = {
    val identification = for {
      hasNino <- NationalInsuranceNumberYesNoPage.path.readWithDefault(false)
      hasAddress <- AddressYesNoPage.path.readWithDefault(false)
      hasPassport <- PassportDetailsYesNoPage.path.readWithDefault(false)
      hasIdCard <- IdCardDetailsYesNoPage.path.readWithDefault(false)
      hasPassportOrIdCard <- PassportOrIdCardDetailsYesNoPage.path.readWithDefault(false)
    } yield (hasNino, hasAddress, hasPassport, hasIdCard, hasPassportOrIdCard)

    identification.flatMap[Option[IndividualIdentification]] {
      case (false, true, true, false, false) => PassportDetailsPage.path.read[Passport].map(_.asCombined).map(Some(_))
      case (false, true, false, true, false) => IdCardDetailsPage.path.read[IdCard].map(_.asCombined).map(Some(_))
      case (false, true, false, false, true) => PassportOrIdCardDetailsPage.path.read[CombinedPassportOrIdCard].map(Some(_))
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  private def readCountryOfNationality: Reads[Option[String]] = {
    readCountryOfResidenceOrNationality(CountryOfNationalityYesNoPage, CountryOfNationalityInTheUkYesNoPage, CountryOfNationalityPage)
  }

  private def readMentalCapacity: Reads[Option[YesNoDontKnow]] = {
    MentalCapacityYesNoPage.path.readNullable[YesNoDontKnow].flatMap[Option[YesNoDontKnow]] {
      case Some(value) => Reads(_ => JsSuccess(Some(value)))
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

}
