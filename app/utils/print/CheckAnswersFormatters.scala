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

package utils.print

import models.{Address, CombinedPassportOrIdCard, IdCard, NonUkAddress, Passport, UkAddress}
import play.api.i18n.Messages
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.escape
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.language.LanguageUtils
import utils.countryOptions.CountryOptions

import java.time.LocalDate
import javax.inject.Inject
import scala.util.Try

class CheckAnswersFormatters @Inject()(languageUtils: LanguageUtils,
                                       countryOptions: CountryOptions) {

  def formatDate(date: LocalDate)(implicit messages: Messages): Html = {
    escape(languageUtils.Dates.formatDate(date))
  }

  def yesOrNo(answer: Boolean)(implicit messages: Messages): Html = {
    if (answer) {
      escape(messages("site.yes"))
    } else {
      escape(messages("site.no"))
    }
  }

  def formatNino(nino: String): Html = {
    val formatted = Try(Nino(nino).formatted).getOrElse(nino)
    escape(formatted)
  }

  def formatAddress(address: Address)(implicit messages: Messages): Html = {
    address match {
      case a: UkAddress => formatUkAddress(a)
      case a: NonUkAddress => formatNonUkAddress(a)
    }
  }

  private def formatUkAddress(address: UkAddress): Html = {
    val lines =
      Seq(
        Some(escape(address.line1)),
        Some(escape(address.line2)),
        address.line3.map(escape),
        address.line4.map(escape),
        Some(escape(address.postcode))
      ).flatten

    breakLines(lines)
  }

  private def formatNonUkAddress(address: NonUkAddress)(implicit messages: Messages): Html = {
    val lines =
      Seq(
        Some(escape(address.line1)),
        Some(escape(address.line2)),
        address.line3.map(escape),
        Some(country(address.country))
      ).flatten

    breakLines(lines)
  }

  def country(code: String)(implicit messages: Messages): Html =
    escape(countryOptions.options.find(_.value.equals(code)).map(_.label).getOrElse(""))

  def formatPassportOrIdCardDetails(id: CombinedPassportOrIdCard)(implicit messages: Messages): Html = {

    def formatNumber(number: String): String = if (id.detailsType.isProvisional) {
      number
    } else {
      messages("site.number-ending", number.takeRight(4))
    }

    val lines =
      Seq(
        Some(country(id.countryOfIssue)),
        Some(escape(formatNumber(id.number))),
        Some(formatDate(id.expirationDate))
      ).flatten

    breakLines(lines)
  }

  def formatPassportDetails(passport: Passport)(implicit messages: Messages): Html = {
    formatPassportOrIdCardDetails(passport.asCombined)
  }

  def formatIdCardDetails(idCard: IdCard)(implicit messages: Messages): Html = {
    formatPassportOrIdCardDetails(idCard.asCombined)
  }

  private def breakLines(lines: Seq[Html]): Html = {
    Html(lines.mkString("<br />"))
  }

}
