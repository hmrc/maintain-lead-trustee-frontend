/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class DateOfBirthFormProvider @Inject() extends Mappings {

  def withPrefix(prefix: String): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = s"$prefix.individual.dateOfBirth.error.invalid",
        allRequiredKey = s"$prefix.individual.dateOfBirth.error.required.all",
        twoRequiredKey = s"$prefix.individual.dateOfBirth.error.required.two",
        requiredKey    = s"$prefix.individual.dateOfBirth.error.required"
      ).verifying(firstError(
        maxDate(LocalDate.now, s"$prefix.individual.dateOfBirth.error.future", "day", "month", "year"),
        minDate(LocalDate.of(1500,1,1), s"$prefix.individual.dateOfBirth.error.past", "day", "month", "year")
      ))
    )
}
