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

package forms.mappings

import forms.mappings.Formatters.formatNino
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }


  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptyString(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey, value)
    }

  protected def isNinoValid(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if Nino.isValid(str)=>
        Valid
      case _ =>
        Invalid(errorKey, value)
    }

  protected def isTelephoneNumberValid(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if TelephoneNumber.isValid(str)=>
        Valid
      case _ =>
        Invalid(errorKey, value)
    }

  protected def uniqueUtr(trustIdentifier: String, utrs: List[String], notUniqueKey: String, sameAsTrustUtrKey: String): Constraint[String] =
    Constraint {
      utr =>
        if (utr == trustIdentifier) {
          Invalid(sameAsTrustUtrKey)
        } else {
          if (utrs.contains(utr)) Invalid(notUniqueKey) else Valid
        }
    }

  protected def uniqueNino(ninos: List[String], notUniqueKey: String): Constraint[String] =
    Constraint {
      nino =>
        if (ninos.map(formatNino).contains(formatNino(nino))) Invalid(notUniqueKey) else Valid
    }

  protected def startsWithCapitalLetter(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.nonEmpty && str.head.isUpper =>
        Valid
      case _ =>
        Invalid(errorKey, value)
    }

}
