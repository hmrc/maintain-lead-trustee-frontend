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

import mapping.extractors.Extractor
import models.Constants.GB
import models.{Address, CombinedPassportOrIdCard, IdCard, IndividualIdentification, IndividualOrBusiness, NationalInsuranceNumber, NonUkAddress, Passport, UkAddress, UserAnswers}
import pages.leadtrustee.IndividualOrBusinessPage
import pages.{EmptyPage, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

trait LeadTrusteeExtractor extends Extractor {

  override def basePath: JsPath = pages.leadtrustee.basePath
  override def individualOrBusinessPage: QuestionPage[IndividualOrBusiness] = IndividualOrBusinessPage

  override def extractOptionalAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    address match {
      case Some(value) => extractAddress(value, answers)
      case None => Success(answers)
    }
  }

  override def extractAddress(address: Address, answers: UserAnswers): Try[UserAnswers] = {
    address match {
      case uk: UkAddress => answers
        .set(ukAddressYesNoPage, true)
        .flatMap(_.set(ukAddressPage, uk))
      case nonUk: NonUkAddress => answers
        .set(ukAddressYesNoPage, false)
        .flatMap(_.set(nonUkAddressPage, nonUk))
    }
  }

  override def extractCountryOfResidenceOrNationality(country: Option[String],
                                                      answers: UserAnswers,
                                                      yesNoPage: QuestionPage[Boolean],
                                                      ukYesNoPage: QuestionPage[Boolean],
                                                      page: QuestionPage[String]): Try[UserAnswers] = {
    if (answers.is5mldEnabled) {
      country match {
        case Some(GB) => answers
          .set(ukYesNoPage, true)
          .flatMap(_.set(page, GB))
        case Some(country) => answers
          .set(ukYesNoPage, false)
          .flatMap(_.set(page, country))
        case None => Success(answers)
      }
    } else {
      Success(answers)
    }
  }

  def ninoYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ninoPage: QuestionPage[String] = new EmptyPage[String]
  def passportOrIdCardDetailsPage: QuestionPage[CombinedPassportOrIdCard] = new EmptyPage[CombinedPassportOrIdCard]

  def extractIndIdentification(identification: Option[IndividualIdentification], answers: UserAnswers): Try[UserAnswers] = {
    identification map {
      case NationalInsuranceNumber(nino) => answers
        .set(ninoYesNoPage, true)
        .flatMap(_.set(ninoPage, nino))
      case p: Passport => answers
        .set(ninoYesNoPage, false)
        .flatMap(_.set(passportOrIdCardDetailsPage, p.asCombined))
      case id: IdCard => answers
        .set(ninoYesNoPage, false)
        .flatMap(_.set(passportOrIdCardDetailsPage, id.asCombined))
      case c: CombinedPassportOrIdCard => answers
        .set(ninoYesNoPage, false)
        .flatMap(_.set(passportOrIdCardDetailsPage, c))
    } getOrElse {
      Success(answers)
    }
  }

}
