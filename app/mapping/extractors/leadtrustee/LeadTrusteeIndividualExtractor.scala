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

package mapping.extractors.leadtrustee

import models.IndividualOrBusiness.Individual
import models._
import pages.QuestionPage
import pages.leadtrustee.individual._

import scala.util.Try

class LeadTrusteeIndividualExtractor extends LeadTrusteeExtractor {

  override def ukCountryOfNationalityYesNoPage: QuestionPage[Boolean] = CountryOfNationalityInTheUkYesNoPage
  override def countryOfNationalityPage: QuestionPage[String] = CountryOfNationalityPage

  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def ninoYesNoPage: QuestionPage[Boolean] = UkCitizenPage
  override def ninoPage: QuestionPage[String] = NationalInsuranceNumberPage
  override def passportOrIdCardDetailsPage: QuestionPage[CombinedPassportOrIdCard] = PassportOrIdCardDetailsPage

  def extract(answers: UserAnswers, leadIndividual: LeadTrusteeIndividual): Try[UserAnswers] = {
    super.extract(answers, Individual)
      .flatMap(_.set(NamePage, leadIndividual.name))
      .flatMap(_.set(DateOfBirthPage, leadIndividual.dateOfBirth))
      .flatMap(answers => extractCountryOfNationality(leadIndividual.nationality, answers))
      .flatMap(answers => extractIndIdentification(Some(leadIndividual.identification), answers))
      .flatMap(answers => extractAddress(leadIndividual.address, answers))
      .flatMap(answers => extractCountryOfResidence(leadIndividual.countryOfResidence, answers))
      .flatMap(answers => extractConditionalValue(leadIndividual.email, EmailAddressYesNoPage, EmailAddressPage, answers))
      .flatMap(_.set(TelephoneNumberPage, leadIndividual.phoneNumber))
  }

}
