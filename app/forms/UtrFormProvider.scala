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

package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class UtrFormProvider @Inject() extends Mappings {

  def apply(messagePrefix: String, trustIdentifier: String, utrs: List[String]): Form[String] =
    Form(
      "value" -> text(s"$messagePrefix.error.required")
        .verifying(
          firstError(
            maxLength(10, s"$messagePrefix.error.length"),
            minLength(10, s"$messagePrefix.error.length"),
            regexp(Validation.utrRegex, s"$messagePrefix.error.invalidCharacters"),
            nonEmptyString("value", s"$messagePrefix.error.required"),
            uniqueUtr(trustIdentifier, utrs, s"$messagePrefix.error.notUnique", s"$messagePrefix.error.sameAsTrustUtr")
          )
        )
    )
}
