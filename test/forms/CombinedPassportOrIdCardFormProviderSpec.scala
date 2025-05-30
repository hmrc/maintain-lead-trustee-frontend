/*
 * Copyright 2024 HM Revenue & Customs
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
import models.CombinedPassportOrIdCard
import play.api.data.{Form, FormError}

class CombinedPassportOrIdCardFormProviderSpec extends StringFieldBehaviours {

  val prefix = "leadtrustee.individual.passportOrIdCardDetails"

  val requiredKey = s"$prefix.country.error.required"
  val lengthKey = s"$prefix.country.error.length"
  val maxLengthCountryField = 100
  val maxLengthNumberField = 30

  val form: Form[CombinedPassportOrIdCard] = new CombinedPassportOrIdCardDetailsFormProvider(frontendAppConfig).withPrefix(prefix)

  ".country" must {

    val fieldName = "country"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLengthCountryField)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLengthCountryField,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLengthCountryField))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }


}
