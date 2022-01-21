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
import pages.leadtrustee.organisation._
import play.api.libs.functional.syntax._
import play.api.libs.json._

class LeadTrusteeOrganisationMapper extends LeadTrusteeMapper[LeadTrusteeOrganisation] {

  override val reads: Reads[LeadTrusteeOrganisation] = (
    NamePage.path.read[String] and
      TelephoneNumberPage.path.read[String] and
      EmailAddressPage.path.readNullable[String] and
      UtrPage.path.readNullable[String] and
      readAddress and
      readCountryOfResidence
    )(LeadTrusteeOrganisation.apply _)

  override def ukAddressYesNoPage: QuestionPage[Boolean] = AddressInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

}
