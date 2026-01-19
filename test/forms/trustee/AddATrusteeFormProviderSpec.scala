/*
 * Copyright 2026 HM Revenue & Customs
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

package forms.trustee

import forms.behaviours.OptionFieldBehaviours
import models.AddATrustee
import play.api.data.FormError

class AddATrusteeFormProviderSpec extends OptionFieldBehaviours {

  val form = new AddATrusteeFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "addATrustee.error.required"

    behave like optionsField[AddATrustee](
      form,
      fieldName,
      validValues  = AddATrustee.values.toSet,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
