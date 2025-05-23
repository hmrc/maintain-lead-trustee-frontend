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

package forms.mappings

import java.time.LocalDate

import play.api.data.FieldMapping
import play.api.data.Forms.of
import models.Enumerable

trait Mappings extends Formatters with Constraints {

  protected def nino(errorKey: String = "error.required"): FieldMapping[String] =
    of(ninoFormatter(errorKey))

  protected def utr(requiredKey: String, invalidKey: String, lengthKey: String): FieldMapping[String] =
    of(utrFormatter(requiredKey, invalidKey, lengthKey))

  protected def postcode(requiredKey : String = "error.required",
                         invalidKey : String = "error.postcodeInvalid") : FieldMapping[String] =
    of(postcodeFormatter(requiredKey, invalidKey))

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.integer",
                    nonNumericKey: String = "error.non_numeric"): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))


  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid")(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey))

  protected def localDate(
                           invalidKey: String,
                           allRequiredKey: String,
                           twoRequiredKey: String,
                           requiredKey: String): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, allRequiredKey, twoRequiredKey, requiredKey))
}
