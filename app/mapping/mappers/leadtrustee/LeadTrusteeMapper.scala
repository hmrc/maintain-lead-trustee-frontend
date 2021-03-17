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

package mapping.mappers.leadtrustee

import mapping.mappers.Mapper
import models.Constants.GB
import models.{Address, NonUkAddress, UkAddress}
import pages.QuestionPage
import play.api.libs.json.{JsSuccess, Reads}

trait LeadTrusteeMapper[T] extends Mapper[T] {

  def ukAddressYesNoPage: QuestionPage[Boolean]

  def ukAddressPage: QuestionPage[UkAddress]

  def nonUkAddressPage: QuestionPage[NonUkAddress]

  def readAddress: Reads[Address] = {
    (ukAddressYesNoPage.path.read[Boolean] orElse ukCountryOfResidenceYesNoPage.path.read[Boolean]).flatMap {
      case true => ukAddressPage.path.read[UkAddress].widen[Address]
      case false => nonUkAddressPage.path.read[Address].widen[Address]
    }
  }

  def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean]

  def countryOfResidencePage: QuestionPage[String]

  def readCountryOfResidence: Reads[Option[String]] = {
    readCountryOfResidenceOrNationality(ukCountryOfResidenceYesNoPage, countryOfResidencePage)
  }

  def readCountryOfResidenceOrNationality(ukYesNoPage: QuestionPage[Boolean],
                                          page: QuestionPage[String]): Reads[Option[String]] = {
    ukYesNoPage.path.readNullable[Boolean].flatMap {
      case Some(true) => Reads(_ => JsSuccess(Some(GB)))
      case Some(false) => page.path.read[String].map(Some(_))
      case _ => Reads(_ => JsSuccess(None))
    }
  }

}
