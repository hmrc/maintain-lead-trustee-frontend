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

package mapping.mappers.trustee

import models._
import pages.QuestionPage
import pages.trustee.organisation._
import pages.trustee.organisation.add._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class TrusteeOrganisationMapper extends TrusteeMapper[TrusteeOrganisation] {

  override val reads: Reads[TrusteeOrganisation] = (
    NamePage.path.read[String] and
      Reads(_ => JsSuccess(None)) and
      Reads(_ => JsSuccess(None)) and
      readIdentification and
      readCountryOfResidence and
      WhenAddedPage.path.read[LocalDate] and
      Reads(_ => JsSuccess(true))
    )(TrusteeOrganisation.apply _)

  private def readIdentification: Reads[Option[TrustIdentificationOrgType]] = (
    Reads(_ => JsSuccess(None)) and
      UtrPage.path.readNullable[String] and
      readAddress
    ).tupled
    .map {
      case (None, None, None) => None
      case (safeId, utr, address) => Some(TrustIdentificationOrgType(safeId, utr, address))
    }

  override def ukAddressYesNoPage: QuestionPage[Boolean] = AddressInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceInTheUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage

}
