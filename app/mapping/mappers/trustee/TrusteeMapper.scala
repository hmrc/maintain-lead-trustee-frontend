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

package mapping.mappers.trustee

import mapping.mappers.Mapper
import models.Constants.GB
import models.{Address, NonUkAddress, UkAddress}
import pages.QuestionPage
import play.api.libs.json.{JsSuccess, Reads}

trait TrusteeMapper[T] extends Mapper[T] {

  def ukAddressYesNoPage: QuestionPage[Boolean]

  def ukAddressPage: QuestionPage[UkAddress]

  def nonUkAddressPage: QuestionPage[NonUkAddress]

  def readAddress: Reads[Option[Address]] = {
    ukAddressYesNoPage.path.readNullable[Boolean].flatMap {
      case Some(true) => ukAddressPage.path.readNullable[UkAddress].widen[Option[Address]]
      case Some(false) => nonUkAddressPage.path.readNullable[NonUkAddress].widen[Option[Address]]
      case _ => Reads(_ => JsSuccess(None)).widen[Option[Address]]
    }
  }

  def countryOfResidenceYesNoPage: QuestionPage[Boolean]

  def ukCountryOfResidenceYesNoPage: QuestionPage[Boolean]

  def countryOfResidencePage: QuestionPage[String]

  def readCountryOfResidence: Reads[Option[String]] = {
    readCountryOfResidenceOrNationality(countryOfResidenceYesNoPage, ukCountryOfResidenceYesNoPage, countryOfResidencePage)
  }

  def readCountryOfResidenceOrNationality(yesNoPage: QuestionPage[Boolean],
                                          ukYesNoPage: QuestionPage[Boolean],
                                          page: QuestionPage[String]): Reads[Option[String]] = {
    yesNoPage.path.readNullable[Boolean].flatMap[Option[String]] {
      case Some(true) => ukYesNoPage.path.read[Boolean].flatMap {
        case true => Reads(_ => JsSuccess(Some(GB)))
        case false => page.path.read[String].map(Some(_))
      }
      case _ => Reads(_ => JsSuccess(None))
    }
  }
}
