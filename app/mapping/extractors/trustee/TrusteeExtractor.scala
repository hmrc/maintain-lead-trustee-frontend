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

import mapping.extractors.Extractor
import models.Constants.GB
import models.{Address, IndividualOrBusiness, NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.trustee.IndividualOrBusinessPage
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

trait TrusteeExtractor extends Extractor {

  override def basePath: JsPath = pages.trustee.basePath
  override def individualOrBusinessPage: QuestionPage[IndividualOrBusiness] = IndividualOrBusinessPage

  def addressYesNoPage: QuestionPage[Boolean]

  override def extractOptionalAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable || !answers.is5mldEnabled) {
      address match {
        case Some(value) => extractAddress(value, answers)
        case _ => answers.set(addressYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  override def extractAddress(address: Address, answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable || !answers.is5mldEnabled) {
      address match {
        case uk: UkAddress => answers
          .set(addressYesNoPage, true)
          .flatMap(_.set(ukAddressYesNoPage, true))
          .flatMap(_.set(ukAddressPage, uk))
        case nonUk: NonUkAddress => answers
          .set(addressYesNoPage, true)
          .flatMap(_.set(ukAddressYesNoPage, false))
          .flatMap(_.set(nonUkAddressPage, nonUk))
      }
    } else {
      Success(answers)
    }
  }

  override def extractCountryOfResidenceOrNationality(country: Option[String],
                                                      answers: UserAnswers,
                                                      yesNoPage: QuestionPage[Boolean],
                                                      ukYesNoPage: QuestionPage[Boolean],
                                                      page: QuestionPage[String]): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      country match {
        case Some(GB) => answers
          .set(yesNoPage, true)
          .flatMap(_.set(ukYesNoPage, true))
          .flatMap(_.set(page, GB))
        case Some(country) => answers
          .set(yesNoPage, true)
          .flatMap(_.set(ukYesNoPage, false))
          .flatMap(_.set(page, country))
        case None => answers
          .set(yesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

}
