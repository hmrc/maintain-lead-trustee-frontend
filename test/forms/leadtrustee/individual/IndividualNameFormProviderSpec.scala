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

package forms.leadtrustee.individual

import forms.IndividualNameFormProvider
import forms.behaviours.{OptionalFieldBehaviours, StringFieldBehaviours}
import org.scalacheck.Gen
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class IndividualNameFormProviderSpec extends StringFieldBehaviours with OptionalFieldBehaviours {

  val messageKeyPrefix = "leadtrustee.individual.name"
  val form             = new IndividualNameFormProvider().withPrefix(messageKeyPrefix)

  val maxLength = 35
  val minLength = 1

  /**
   * The following section of the individualNameRegex is removed,
   * as the scalacheck-gen-regexp library does not appear to support it: ^(?=.{1,99}$)
   */
  val testIndividualNameRegex = "([A-Z]([-'. ]{0,1}[A-Za-z ]+)*[A-Za-z]?)$"

  /**
   * This method produces a Gen[String] that conforms to the individualNameRegex,
   * but is at least one character over the max allowed length
   */
  private def validStringButOverMaxLength(maxLength: Int): Gen[String] =
    for {
      firstChar <- Gen.alphaUpperChar
      length    <- Gen.choose(maxLength + 1, maxLength * 2)
      chars     <- Gen.listOfN(length, Gen.alphaChar)
    } yield (firstChar :: chars).mkString

  ".firstName" must {

    val fieldName   = "firstName"
    val requiredKey = s"$messageKeyPrefix.error.firstName.required"
    val lengthKey   = s"$messageKeyPrefix.error.firstName.length"
    val capitalKey  = s"$messageKeyPrefix.error.firstName.capitalLetter"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(testIndividualNameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      stringGenerator = validStringButOverMaxLength(maxLength)
    )

    "bind whitespace no values" in {
      val result = form.bind(Map("firstName" -> "FirstName", "middleName" -> "", "lastName" -> "LastName"))
      result.value.value.middleName mustBe None
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like nonEmptyField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(fieldName))
    )

    behave like fieldStartingWithCapitalLetter(
      form,
      fieldName,
      requiredError = FormError(fieldName, capitalKey, Seq(fieldName))
    )
  }

  ".middleName" must {

    val fieldName  = "middleName"
    val lengthKey  = s"$messageKeyPrefix.error.middleName.length"
    val maxLength  = 35
    val capitalKey = s"$messageKeyPrefix.error.middleName.capitalLetter"

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      stringGenerator = validStringButOverMaxLength(maxLength)
    )

    behave like optionalField(
      form,
      fieldName,
      validDataGenerator = RegexpGen.from(testIndividualNameRegex)
    )

    behave like fieldStartingWithCapitalLetter(
      form,
      fieldName,
      requiredError = FormError(fieldName, capitalKey, Seq(fieldName))
    )

    "bind whitespace trim values" in {
      val result = form.bind(Map("firstName" -> "FirstName", "middleName" -> "  Middle  ", "lastName" -> "LastName"))
      result.value.value.middleName mustBe Some("Middle")
    }

    "bind whitespace blank values" in {
      val result = form.bind(Map("firstName" -> "FirstName", "middleName" -> "  ", "lastName" -> "LastName"))
      result.value.value.middleName mustBe None
    }

    "bind whitespace no values" in {
      val result = form.bind(Map("firstName" -> "FirstName", "middleName" -> "", "lastName" -> "LastName"))
      result.value.value.middleName mustBe None
    }

  }

  ".lastName" must {

    val fieldName   = "lastName"
    val requiredKey = s"$messageKeyPrefix.error.lastName.required"
    val lengthKey   = s"$messageKeyPrefix.error.lastName.length"
    val capitalKey  = s"$messageKeyPrefix.error.lastName.capitalLetter"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(testIndividualNameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      stringGenerator = validStringButOverMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like nonEmptyField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(fieldName))
    )

    behave like fieldStartingWithCapitalLetter(
      form,
      fieldName,
      requiredError = FormError(fieldName, capitalKey, Seq(fieldName))
    )
  }

}
