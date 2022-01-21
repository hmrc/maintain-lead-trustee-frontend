/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import forms.mappings.Mappings
import play.api.data.Form

import java.time.LocalDate
import javax.inject.Inject

class DateOfBirthFormProvider @Inject()(appConfig: FrontendAppConfig) extends Mappings {

  def withConfig(prefix: String, matchingLeadTrustee: Boolean = false): Form[LocalDate] = {

    case class MinDateConfig(date: LocalDate, messageKey: String)

    val minDateConfig: MinDateConfig = if (matchingLeadTrustee) {
      MinDateConfig(appConfig.minLeadTrusteeDob, s"$prefix.matching.error.past")
    } else {
      MinDateConfig(appConfig.minDate, s"$prefix.error.past")
    }

    Form(
      "value" -> localDate(
        invalidKey = s"$prefix.error.invalid",
        allRequiredKey = s"$prefix.error.required.all",
        twoRequiredKey = s"$prefix.error.required.two",
        requiredKey = s"$prefix.error.required"
      ).verifying(firstError(
        maxDate(LocalDate.now, s"$prefix.error.future", "day", "month", "year"),
        minDate(minDateConfig.date, minDateConfig.messageKey, "day", "month", "year")
      ))
    )
  }
}
