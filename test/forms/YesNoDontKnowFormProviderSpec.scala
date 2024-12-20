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

import forms.behaviours.OptionFieldBehaviours
import models.YesNoDontKnow
import play.api.data.{Form, FormError}

class YesNoDontKnowFormProviderSpec extends OptionFieldBehaviours {

  val prefix = "yesNoDontKnow"

  val requiredKey = s"$prefix.error.required"
  val invalidKey = s"error.invalid"

  val form: Form[YesNoDontKnow] = new YesNoDontKnowFormProvider().withPrefix(prefix)

  ".value" must {

    val fieldName = "value"

    behave like optionsField(
      form = form,
      fieldName = fieldName,
      validValues = YesNoDontKnow.values.toSet,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

  }
}
