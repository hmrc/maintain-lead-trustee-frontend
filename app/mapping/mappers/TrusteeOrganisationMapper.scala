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

package mapping.mappers

import models._
import pages.trustee.WhenAddedPage
import pages.trustee.organisation._
import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads}

import java.time.LocalDate

class TrusteeOrganisationMapper extends Logging {

  def map(userAnswers: UserAnswers): Option[TrusteeOrganisation] = {
    val reads: Reads[TrusteeOrganisation] =
      (
        NamePage.path.read[String] and
          Reads(_ => JsSuccess(None)) and
          Reads(_ => JsSuccess(None)) and
          readIdentification and
          WhenAddedPage.path.read[LocalDate] and
          Reads(_ => JsSuccess(true))
        ).apply(TrusteeOrganisation.apply _ )

    userAnswers.data.validate[TrusteeOrganisation](reads) match {
      case JsError(errors) =>
        logger.error(s"[UTR: ${userAnswers.identifier}] Failed to rehydrate TrusteeOrganisation from UserAnswers due to $errors")
        None
      case JsSuccess(value, _) =>
        Some(value)
    }
  }

  private def readIdentification: Reads[Option[TrustIdentificationOrgType]] = {
    (
      Reads(_ => JsSuccess(None)) and
        UtrPage.path.readNullable[String] and
        readAddress
      ).tupled.map {
      case (None, None, None) => None
      case (safeId, utr, address) => Some(TrustIdentificationOrgType(safeId, utr, address))
    }
  }

  private def readAddress: Reads[Option[Address]] = {
    AddressInTheUkYesNoPage.path.readNullable[Boolean].flatMap {
      case Some(true) => UkAddressPage.path.readNullable[UkAddress].widen[Option[Address]]
      case Some(false) => NonUkAddressPage.path.readNullable[NonUkAddress].widen[Option[Address]]
      case _ => Reads(_ => JsSuccess(None)).widen[Option[Address]]
    }
  }

}
