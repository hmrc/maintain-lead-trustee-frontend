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

package mapping.extractors.trustee

import models.IndividualOrBusiness.Individual
import models._
import pages.QuestionPage
import pages.trustee.individual._
import pages.trustee.individual.add.WhenAddedPage
import pages.trustee.individual.amend._

import scala.util.{Success, Try}

class TrusteeIndividualExtractor extends TrusteeExtractor {

  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def countryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityYesNoPage
  override def ukCountryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityInTheUkYesNoPage
  override def countryOfNationalityPage: QuestionPage[String] = CountryOfNationalityPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

  def extract(answers: UserAnswers, trustee: TrusteeIndividual, index: Int): Try[UserAnswers] = {
    super.extract(answers, Individual)
      .flatMap(_.set(IndexPage, index))
      .flatMap(_.set(NamePage, trustee.name))
      .flatMap(answers => extractConditionalValue(trustee.dateOfBirth, DateOfBirthYesNoPage, DateOfBirthPage, answers))
      .flatMap(answers => extractOptionalAddress(trustee.address, answers))
      .flatMap(answers => extractCountryOfResidence(trustee.countryOfResidence, answers))
      .flatMap(answers => extractCountryOfNationality(trustee.nationality, answers))
      .flatMap(answers => extractMentalCapacity(trustee.mentalCapacityYesNo, answers))
      .flatMap(answers => extractIdentification(trustee.identification, answers))
      .flatMap(_.set(WhenAddedPage, trustee.entityStart))
  }

  private def extractMentalCapacity(mentalCapacityYesNo: Option[Boolean], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      extractValue(mentalCapacityYesNo, MentalCapacityYesNoPage, answers)
    } else {
      Success(answers)
    }
  }

  private def extractIdentification(identification: Option[IndividualIdentification], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable || !answers.is5mldEnabled) {
      identification match {
        case Some(NationalInsuranceNumber(nino)) =>
          answers.set(NationalInsuranceNumberYesNoPage, true)
            .flatMap(_.set(NationalInsuranceNumberPage, nino))
        case Some(p: Passport) =>
          answers.set(NationalInsuranceNumberYesNoPage, false)
            .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
            .flatMap(_.set(PassportOrIdCardDetailsPage, p.asCombined))
        case Some(id: IdCard) =>
          answers.set(NationalInsuranceNumberYesNoPage, false)
            .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
            .flatMap(_.set(PassportOrIdCardDetailsPage, id.asCombined))
        case Some(c: CombinedPassportOrIdCard) =>
          answers.set(NationalInsuranceNumberYesNoPage, false)
            .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
            .flatMap(_.set(PassportOrIdCardDetailsPage, c))
        case _ =>
          answers.set(NationalInsuranceNumberYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

}
