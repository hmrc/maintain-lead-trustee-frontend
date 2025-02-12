/*
 * Copyright 2025 HM Revenue & Customs
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

package forms

import forms.Validation.individualNameRegex
import forms.helpers.WhitespaceHelper.{emptyToNone, trimWhitespace}
import forms.mappings.Mappings

import javax.inject.Inject
import models.Name
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Valid}

class IndividualNameFormProvider @Inject() extends Mappings {

  private val maxFieldCharacters = 35

  def withPrefix(prefix: String): Form[Name] = Form(
    mapping(
      "firstName" -> text(s"$prefix.error.firstName.required")
        .verifying(
          firstError(
            nonEmptyString("firstName", s"$prefix.error.firstName.required"),
            regexp(individualNameRegex, s"$prefix.error.firstName.invalid"),
            maxLength(maxFieldCharacters, s"$prefix.error.firstName.length"),
            startsWithCapitalLetter("firstName", s"$prefix.error.firstName.capitalLetter")
          )
        ),
      "middleName" -> optional(text()
        .transform(trimWhitespace, identity[String])
        .verifying(
          Constraint[String] { value: String =>
            if (value.nonEmpty) {
              firstError(
                regexp(individualNameRegex, s"$prefix.error.middleName.invalid"),
                maxLength(maxFieldCharacters, s"$prefix.error.middleName.length"),
                startsWithCapitalLetter("middleName", s"$prefix.error.middleName.capitalLetter")
              )(value)
            } else {
              Valid
            }
          }
        )
      ).transform(emptyToNone, identity[Option[String]]),
      "lastName" -> text(s"$prefix.error.lastName.required")
        .verifying(
          firstError(
            nonEmptyString("lastName", s"$prefix.error.lastName.required"),
            regexp(individualNameRegex, s"$prefix.error.lastName.invalid"),
            maxLength(maxFieldCharacters, s"$prefix.error.lastName.length"),
            startsWithCapitalLetter("lastName", s"$prefix.error.lastName.capitalLetter")
          )
        )
    )(Name.apply)(Name.unapply)
  )
}
