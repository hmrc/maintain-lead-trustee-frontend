/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.behaviours.StringFieldBehaviours
import play.api.data.{Form, FormError}

class UtrFormProviderSpec extends StringFieldBehaviours {

  val prefix = "trustee.organisation.utr"

  val requiredKey = s"$prefix.error.required"
  val lengthKey = s"$prefix.error.length"
  val notUniqueKey = s"$prefix.error.notUnique"
  val sameAsTrustUtrKey = s"$prefix.error.sameAsTrustUtr"
  val utrLength = 10

  val form: Form[String] = new UtrFormProvider().apply(prefix, "utr", Nil)

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(utrLength)
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = utrLength,
      lengthError = FormError(fieldName, lengthKey, Seq(utrLength))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = utrLength,
      lengthError = FormError(fieldName, lengthKey, Seq(utrLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like utrField(
      form = new UtrFormProvider(),
      prefix = prefix,
      fieldName = fieldName,
      length = utrLength,
      notUniqueError = FormError(fieldName, notUniqueKey),
      sameAsTrustUtrError = FormError(fieldName, sameAsTrustUtrKey)
    )

  }
}
