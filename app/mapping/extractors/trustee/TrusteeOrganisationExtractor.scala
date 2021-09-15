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

import models.IndividualOrBusiness.Business
import models.{NonUkAddress, TrustIdentificationOrgType, TrusteeOrganisation, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.trustee.organisation._
import pages.trustee.organisation.add.WhenAddedPage
import pages.trustee.organisation.amend._

import scala.util.{Success, Try}

class TrusteeOrganisationExtractor extends TrusteeExtractor {

  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = AddressInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

  def extract(answers: UserAnswers, trustee: TrusteeOrganisation, index: Int): Try[UserAnswers] = {
    super.extract(answers, Business)
      .flatMap(_.set(IndexPage, index))
      .flatMap(_.set(NamePage, trustee.name))
      .flatMap(answers => extractIdentification(trustee.identification, answers))
      .flatMap(answers => extractCountryOfResidence(trustee.countryOfResidence, answers))
      .flatMap(_.set(WhenAddedPage, trustee.entityStart))
  }

  private def extractIdentification(identification: Option[TrustIdentificationOrgType], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      identification match {
        case Some(TrustIdentificationOrgType(_, Some(utr), None)) => answers
          .set(UtrYesNoPage, true)
          .flatMap(_.set(UtrPage, utr))
        case Some(TrustIdentificationOrgType(_, None, Some(address))) => answers
          .set(UtrYesNoPage, false)
          .flatMap(answers => extractAddress(address, answers))
        case _ => answers
          .set(UtrYesNoPage, false)
          .flatMap(_.set(AddressYesNoPage, false))
      }
    } else {
      Success(answers)
    }
  }

}
