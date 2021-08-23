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

import com.google.inject.Inject
import models._
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import viewmodels.AnswerRow

import java.time.LocalDate

class AnswerRowConverter @Inject()(checkAnswersFormatters: CheckAnswersFormatters) {

  def bind(userAnswers: UserAnswers, trusteeName: String)
          (implicit messages: Messages): Bound = new Bound(userAnswers, trusteeName)

  class Bound(userAnswers: UserAnswers, trusteeName: String)
             (implicit messages: Messages) {

    def nameQuestion(query: Gettable[Name],
                     labelKey: String,
                     changeUrl: String,
                     canEdit: Boolean = true): Option[AnswerRow] = {
      val format = (x: Name) => HtmlFormat.escape(x.displayFullName)
      question(query, labelKey, format, changeUrl, canEdit)
    }

    def stringQuestion(query: Gettable[String],
                       labelKey: String,
                       changeUrl: String): Option[AnswerRow] = {
      val format = (x: String) => HtmlFormat.escape(x)
      question(query, labelKey, format, changeUrl)
    }

    def yesNoQuestion(query: Gettable[Boolean],
                      labelKey: String,
                      changeUrl: String,
                      canEdit: Boolean = true): Option[AnswerRow] = {
      val format = (x: Boolean) => checkAnswersFormatters.yesOrNo(x)
      question(query, labelKey, format, changeUrl, canEdit)
    }

    def yesNoQuestionAllowEmptyAnswer(query: Gettable[Boolean],
                                      labelKey: String,
                                      changeUrl: String,
                                      canEdit: Boolean = true): Option[AnswerRow] = {
      yesNoQuestion(query, labelKey, changeUrl, canEdit) orElse
        Some(answer(labelKey, changeUrl, canEdit))
    }

    def dateQuestion(query: Gettable[LocalDate],
                     labelKey: String,
                     changeUrl: String,
                     canEdit: Boolean = true): Option[AnswerRow] = {
      val format = (x: LocalDate) => checkAnswersFormatters.formatDate(x)
      question(query, labelKey, format, changeUrl, canEdit)
    }

    def ninoQuestion(query: Gettable[String],
                     labelKey: String,
                     changeUrl: String,
                     canEdit: Boolean = true): Option[AnswerRow] = {
      val format = (x: String) => checkAnswersFormatters.formatNino(x)
      question(query, labelKey, format, changeUrl, canEdit)
    }

    def addressQuestion[T <: Address](query: Gettable[T],
                                      labelKey: String,
                                      changeUrl: String)
                                     (implicit reads: Reads[T]): Option[AnswerRow] = {
      val format = (x: T) => checkAnswersFormatters.formatAddress(x)
      question(query, labelKey, format, changeUrl)
    }

    def passportOrIdCardDetailsQuestion(query: Gettable[CombinedPassportOrIdCard],
                                        provisional: Option[Gettable[Boolean]],
                                        labelKey: String,
                                        changeUrl: String): Option[AnswerRow] = {
      val format = (x: CombinedPassportOrIdCard) => checkAnswersFormatters.formatPassportOrIdCardDetails(x, provisional.exists(isProvisional))
      question(query, labelKey, format, changeUrl)
    }

    def passportDetailsQuestion(query: Gettable[Passport],
                                provisional: Gettable[Boolean],
                                labelKey: String,
                                changeUrl: String): Option[AnswerRow] = {
      val format = (x: Passport) => checkAnswersFormatters.formatPassportDetails(x, isProvisional(provisional))
      question(query, labelKey, format, changeUrl)
    }

    def idCardDetailsQuestion(query: Gettable[IdCard],
                              provisional: Gettable[Boolean],
                              labelKey: String,
                              changeUrl: String): Option[AnswerRow] = {
      val format = (x: IdCard) => checkAnswersFormatters.formatIdCardDetails(x, isProvisional(provisional))
      question(query, labelKey, format, changeUrl)
    }

    def countryQuestion(isUkQuery: Gettable[Boolean],
                        query: Gettable[String],
                        labelKey: String,
                        changeUrl: String): Option[AnswerRow] = {
      userAnswers.get(isUkQuery) flatMap {
        case false =>
          val format = (x: String) => checkAnswersFormatters.country(x)
          question(query, labelKey, format, changeUrl)
        case _ =>
          None
      }
    }

    private def question[T](query: Gettable[T],
                            labelKey: String,
                            format: T => Html,
                            changeUrl: String,
                            canEdit: Boolean = true)
                           (implicit rds: Reads[T]): Option[AnswerRow] = {
      userAnswers.get(query) map { x =>
        answer(labelKey, changeUrl, canEdit, format(x))
      }
    }

    private def answer(labelKey: String,
                       changeUrl: String,
                       canEdit: Boolean,
                       format: Html = HtmlFormat.empty): AnswerRow = {
      AnswerRow(
        label = messages(s"$labelKey.checkYourAnswersLabel", trusteeName),
        answer = format,
        changeUrl = Some(changeUrl),
        canEdit = canEdit
      )
    }

    private def isProvisional(query: Gettable[Boolean]): Boolean = {
      !userAnswers.get(query).contains(false)
    }
  }
}
