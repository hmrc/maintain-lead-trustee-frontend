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

package views.behaviours

import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat
import views.ViewUtils

trait QuestionViewBehaviours[A] extends ViewBehaviours {

  val errorKey = "value"
  val errorPrefix = "site.error"
  val errorMessage = "error.number"
  val error: FormError = FormError(errorKey, errorMessage)

  val form: Form[A]

  def pageWithTextFields(form: Form[A],
                         createView: Form[A] => HtmlFormat.Appendable,
                         messageKeyPrefix: String,
                         messageKeyParam: Option[String],
                         expectedFormAction: String,
                         fields: String*) = {

    "behave like a question page" when {

      "rendered" must {

        for (field <- fields) {

          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field)
          }
        }

        "not render an error summary" in {

          val doc = asDocument(createView(form))
          assertNotRenderedByClass(doc,  "govuk-error-summary__title")
        }
      }

      "rendered with any error" must {

        "show an error prefix in the browser title" in {

          val doc = asDocument(createView(form.withError(error)))
          assertEqualsValue(doc, "title", ViewUtils.breadcrumbTitle(s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title")}"""))
        }
      }

      for (field <- fields) {

        s"rendered with an error with field '$field'" must {

          "show an error summary" in {

            val doc = asDocument(createView(form.withError(FormError(field, "error"))))
            assertRenderedByClass(doc, "govuk-error-summary__title")
          }

          s"show an error in the label for field '$field'" in {

            val doc = asDocument(createView(form.withError(FormError(field, "error"))))
            val errorSpan = doc.getElementsByClass("govuk-error-message").first
            errorSpan.parent.getElementsByClass("govuk-label").attr("for") mustBe field
          }
        }
      }
    }
  }

  def pageWithHiddenInput(form: Form[A],
                          createView: Form[A] => HtmlFormat.Appendable,
                          field: String): Unit = {

    s"contain hidden input for $field" in {
      val doc = asDocument(createView(form))
      doc.getElementById(field).attr("type") mustBe "hidden"
    }
  }

  def pageWithPassportOrIDCardDetailsFields(form: Form[A],
                                            createView: Form[A] => HtmlFormat.Appendable,
                                            messageKeyPrefix: String,
                                            expectedFormAction: String,
                                            textFields: Seq[(String, Option[String])],
                                            dateKey : String,
                                            args: String*) = {

    val dateFields = Seq(s"$dateKey.day", s"$dateKey.month", s"$dateKey.year")

    "behave like a passportOrIDCard page" when {

      "rendered" must {

        for (field <- textFields) {

          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field._1)
          }
        }

        for (field <- dateFields) {

          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field)
          }
        }

        "not render an error summary" in {

          val doc = asDocument(createView(form))
          assertNotRenderedByClass(doc,  "govuk-error-summary__title")
        }
      }

      "rendered with any error" must {

        "show an error prefix in the browser title" in {

          val doc = asDocument(createView(form.withError(error)))
          assertEqualsValue(doc, "title", ViewUtils.breadcrumbTitle(s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title", args: _*)}"""))
        }
      }

      for (field <- textFields) {

        s"rendered with an error with field '$field'" must {

          "show an error summary" in {

            val doc = asDocument(createView(form.withError(FormError(field._1, "error"))))
            assertRenderedByClass(doc, "govuk-error-summary__title")
          }

          s"show an error in the label for field '$field'" in {

            val doc = asDocument(createView(form.withError(FormError(field._1, "error"))))
            val errorSpan = doc.getElementsByClass("govuk-error-message").first
            errorSpan.parent.getElementsByClass("govuk-label").attr("for") mustBe field._1
          }

          s"contains a label and optional hint text for the field '$field'" in {
            val doc = asDocument(createView(form))
            val fieldName = field._1
            val fieldHint = field._2 map (k => messages(k))
            assertContainsLabel(doc, fieldName, messages(s"$messageKeyPrefix.$fieldName"), fieldHint)
          }
        }
      }

      "rendered with any date field error" must {

        "show an error in the legend" in {

          val doc = asDocument(createView(form.withError(FormError(dateKey, "error"))))
          assertRenderedById(doc, s"$dateKey-error")
        }

        "show an error prefix in the browser title" in {

          val doc = asDocument(createView(form.withError(error)))
          assertEqualsValue(doc, "title", ViewUtils.breadcrumbTitle(s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title", args: _*)}"""))
        }
      }

    }
  }

  def pageWithDateFields(form: Form[A],
                         createView: Form[A] => HtmlFormat.Appendable,
                         messageKeyPrefix: String,
                         key: String,
                         args: String*) = {

    val fields = Seq(s"$key.day", s"$key.month", s"$key.year")

    "behave like a question page" when {

      "rendered" must {

        for (field <- fields) {

          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field)
          }
        }

        "not render an error summary" in {

          val doc = asDocument(createView(form))
          assertNotRenderedByClass(doc,  "govuk-error-summary__title")
        }
      }

      "rendered with any error" must {

        "show an error prefix in the browser title" in {

          val doc = asDocument(createView(form.withError(error)))
          assertEqualsValue(doc, "title", ViewUtils.breadcrumbTitle(s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title", args: _*)}"""))
        }
      }

      "rendered with an error" must {

        "show an error summary" in {

          val doc = asDocument(createView(form.withError(FormError(key, "error"))))
          assertRenderedByClass(doc,  "govuk-error-summary__title")
        }

        "show an error in the legend" in {

          val doc = asDocument(createView(form.withError(FormError(key, "error"))))
          assertRenderedById(doc, s"value-error")
        }
      }
    }
  }
}
