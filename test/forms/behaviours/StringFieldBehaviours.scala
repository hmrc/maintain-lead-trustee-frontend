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

package forms.behaviours

import forms.mappings.TelephoneNumber
import forms.{NationalInsuranceNumberFormProvider, UtrFormProvider, Validation}
import org.scalacheck.Gen
import play.api.data.{Form, FormError}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.emailaddress.EmailAddress
import wolfendale.scalacheck.regexp.RegexpGen

trait StringFieldBehaviours extends FieldBehaviours with OptionalFieldBehaviours {

  def fieldWithMaxLength(form: Form[_],
                         fieldName: String,
                         maxLength: Int,
                         lengthError: FormError): Unit = {

    s"not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(lengthError)
      }
    }
  }

  def fieldWithMinLength(form : Form[_],
                         fieldName : String,
                         minLength : Int,
                         lengthError : FormError) : Unit = {

    s"not bind strings shorter than $minLength characters" in {

      val length = if (minLength > 0 && minLength < 2) minLength else minLength -1

      forAll(stringsWithMaxLength(length) -> "shortString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(lengthError)
      }
    }
  }

  def nonEmptyField(form: Form[_],
                    fieldName: String,
                    requiredError: FormError): Unit = {

    "not bind spaces" in {

      val result = form.bind(Map(fieldName -> "    ")).apply(fieldName)
      result.errors mustBe Seq(requiredError)
    }
  }

  def fieldWithRegexpWithGenerator(form: Form[_],
                                   fieldName: String,
                                   regexp: String,
                                   generator: Gen[String],
                                   error: FormError): Unit = {

    s"not bind strings which do not match $regexp" in {
      forAll(generator) {
        string =>
          whenever(!string.matches(regexp) && string.nonEmpty) {
            val result = form.bind(Map(fieldName -> string)).apply(fieldName)
            result.errors mustEqual Seq(error)
          }
      }
    }
  }

  def telephoneNumberField(form: Form[_],
                           fieldName: String,
                           invalidError: FormError): Unit = {

    "not bind strings which do not match valid telephone number format" in {
      val generator = RegexpGen.from(Validation.telephoneRegex)
      forAll(generator) {
        string =>
          whenever(!TelephoneNumber.isValid(string)) {
            val result = form.bind(Map(fieldName -> string)).apply(fieldName)
            result.errors mustEqual Seq(invalidError)
          }
      }
    }
  }

  def emailAddressField(form: Form[_],
                        fieldName: String,
                        invalidError: FormError): Unit = {

    s"not bind strings which do not match valid email address format " in {
      forAll(nonEmptyString) {
        string =>
          whenever(!EmailAddress.isValid(string)) {
            val result = form.bind(Map(fieldName -> string)).apply(fieldName)
            result.errors mustEqual Seq(invalidError)
          }
      }
    }
  }

  def ninoField(form: NationalInsuranceNumberFormProvider,
                prefix: String,
                fieldName: String,
                requiredError: FormError,
                notUniqueError: FormError): Unit = {

    val nino = "AA000000A"

    s"not bind strings which do not match valid nino format" in {
      val generator = RegexpGen.from(Validation.validNinoFormat)
      forAll(generator) {
        string =>
          whenever(!Nino.isValid(string)) {
            val result = form.apply(prefix, Nil).bind(Map(fieldName -> string)).apply(fieldName)
            result.errors mustEqual Seq(requiredError)
          }
      }
    }

    "not bind NINos that have been used for other individuals" in {
      val intGenerator = Gen.choose(1, 25)
      forAll(intGenerator) {
        size =>
          val ninos = List.fill(size)(nino)
          val result = form.apply(prefix, ninos).bind(Map(fieldName -> nino)).apply(fieldName)
          result.errors mustEqual Seq(notUniqueError)
      }
    }

    "bind valid NINos when no individuals" in {
      val result = form.apply(prefix, Nil).bind(Map(fieldName -> nino)).apply(fieldName)
      result.errors mustEqual Nil
      result.value.value mustBe nino
    }

    "bind valid NINos when no other individuals have that NINo" in {
      val value: String = "AA111111A"
      val result = form.apply(prefix, List(value)).bind(Map(fieldName -> nino)).apply(fieldName)
      result.errors mustEqual Nil
      result.value.value mustBe nino
    }

    "not bind NINo that has been used for another individual but is case-sensitively different" when {

      "bound value is in lower case" in {
        val otherNino: String = "AA111111A"
        val boundNino: String = "aa111111a"
        val result = form.apply(prefix, List(otherNino)).bind(Map(fieldName -> boundNino)).apply(fieldName)
        result.errors mustEqual Seq(notUniqueError)
      }

      "bound value is in upper case" in {
        val otherNino: String = "aa111111a"
        val boundNino: String = "AA111111A"
        val result = form.apply(prefix, List(otherNino)).bind(Map(fieldName -> boundNino)).apply(fieldName)
        result.errors mustEqual Seq(notUniqueError)
      }

      "bound value is in mixed case" in {
        val otherNino: String = "aA111111a"
        val boundNino: String = "Aa111111A"
        val result = form.apply(prefix, List(otherNino)).bind(Map(fieldName -> boundNino)).apply(fieldName)
        result.errors mustEqual Seq(notUniqueError)
      }
    }
  }

  def utrField(form: UtrFormProvider,
               prefix: String,
               fieldName: String,
               length: Int,
               notUniqueError: FormError,
               sameAsTrustUtrError: FormError): Unit = {

    val regex = Validation.utrRegex.replace("*", s"{$length}")
    val utrGenerator = RegexpGen.from(regex)

    "not bind UTRs that have been used for other businesses" in {
      val intGenerator = Gen.choose(1, 25)
      forAll(utrGenerator, intGenerator) {
        (utr, size) =>
          val utrs = List.fill(size)(utr)
          val result = form.apply(prefix, "utr", utrs).bind(Map(fieldName -> utr)).apply(fieldName)
          result.errors mustEqual Seq(notUniqueError)
      }
    }

    "not bind UTR if it is the same as the trust UTR" in {
      forAll(utrGenerator) {
        utr =>
          val result = form.apply(prefix, utr, Nil).bind(Map(fieldName -> utr)).apply(fieldName)
          result.errors mustEqual Seq(sameAsTrustUtrError)
      }
    }

    "bind valid UTRs when no businesses" in {
      forAll(utrGenerator) {
        utr =>
          val result = form.apply(prefix, "utr", Nil).bind(Map(fieldName -> utr)).apply(fieldName)
          result.errors mustEqual Nil
          result.value.value mustBe utr
      }
    }

    "bind valid UTRs when no other businesses have that UTR" in {
      val value: String = "1234567890"
      forAll(utrGenerator.suchThat(_ != value)) {
        utr =>
          val result = form.apply(prefix, "utr", List(value)).bind(Map(fieldName -> utr)).apply(fieldName)
          result.errors mustEqual Nil
          result.value.value mustBe utr
      }
    }
  }

}
