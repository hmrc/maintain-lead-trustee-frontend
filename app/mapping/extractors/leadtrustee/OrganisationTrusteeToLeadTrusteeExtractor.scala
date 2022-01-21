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

package mapping.extractors.leadtrustee

import models.IndividualOrBusiness.Business
import models._
import pages.QuestionPage
import pages.leadtrustee.organisation._

import scala.util.{Success, Try}

class OrganisationTrusteeToLeadTrusteeExtractor extends LeadTrusteeExtractor {

  override def ukAddressYesNoPage: QuestionPage[Boolean] = AddressInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

  def extract(userAnswers: UserAnswers, trustee: TrusteeOrganisation, index: Int): Try[UserAnswers] = {
    super.extract(userAnswers, Business)
      .flatMap(_.set(IndexPage, index))
      .flatMap(answers => extractOrgIdentification(trustee.identification, answers))
      .flatMap(_.set(NamePage, trustee.name))
      .flatMap(answers => extractCountryOfResidence(trustee.countryOfResidence, answers))
      .flatMap(answers => extractEmail(trustee.email, answers))
      .flatMap(answers => extractValue(trustee.phoneNumber, TelephoneNumberPage, answers))
  }

  private def extractOrgIdentification(identification: Option[TrustIdentificationOrgType], answers: UserAnswers): Try[UserAnswers] = {
    identification match {
      case Some(TrustIdentificationOrgType(_, Some(utr), None)) => answers
        .set(RegisteredInUkYesNoPage, true)
        .flatMap(_.set(UtrPage, utr))
      case Some(TrustIdentificationOrgType(_, None, Some(address))) => answers
        .set(RegisteredInUkYesNoPage, false)
        .flatMap(answers => extractAddress(address, answers))
      case _ => Success(answers)
    }
  }

  private def extractEmail(emailAddress: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    emailAddress match {
      case Some(email) => answers
        .set(EmailAddressYesNoPage, true)
        .flatMap(_.set(EmailAddressPage, email))
      case _ => Success(answers)
    }
  }

}
