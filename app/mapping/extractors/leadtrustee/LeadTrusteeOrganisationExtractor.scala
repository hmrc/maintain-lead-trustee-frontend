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

package mapping.extractors.leadtrustee

import models.IndividualOrBusiness.Business
import models._
import pages.QuestionPage
import pages.leadtrustee.organisation.{UtrPage, _}

import scala.util.Try

class LeadTrusteeOrganisationExtractor extends LeadTrusteeExtractor {

  override def ukAddressYesNoPage: QuestionPage[Boolean] = AddressInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

  def extract(answers: UserAnswers, leadOrganisation: LeadTrusteeOrganisation): Try[UserAnswers] = {
    super.extract(answers, Business)
      .flatMap(answers => extractConditionalValue(leadOrganisation.utr, RegisteredInUkYesNoPage, UtrPage, answers))
      .flatMap(_.set(NamePage, leadOrganisation.name))
      .flatMap(answers => extractAddress(leadOrganisation.address, answers))
      .flatMap(answers => extractCountryOfResidence(leadOrganisation.countryOfResidence, answers))
      .flatMap(answers => extractConditionalValue(leadOrganisation.email, EmailAddressYesNoPage, EmailAddressPage, answers))
      .flatMap(_.set(TelephoneNumberPage, leadOrganisation.phoneNumber))
  }

}
