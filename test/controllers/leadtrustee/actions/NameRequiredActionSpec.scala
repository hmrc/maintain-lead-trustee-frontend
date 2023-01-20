/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.leadtrustee.actions

import java.time.LocalDate

import models.UserAnswers
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.Future

class NameRequiredActionSpec extends AnyWordSpec with MockitoSugar with ScalaFutures with Matchers {
  class Harness(messagesApi: MessagesApi) extends NameRequiredAction(scala.concurrent.ExecutionContext.global, messagesApi) {
    def callTransform[A](request: DataRequest[A]): Future[LeadTrusteeNameRequest[A]] = transform(request)
  }

  "Pulls the individual name from userAnswers into the Request" in {
    val OUT = new Harness(mock[MessagesApi])
    val sourceRequest = mock[DataRequest[AnyContent]]

    val ua = UserAnswers("id",
      "UTRUTRUTR",
      "sessionId", "newId",
      LocalDate.now(),
      Json.obj().transform(pages.leadtrustee.individual.NamePage.path.json.put(Json.obj(
      "firstName" -> "testFirstName",
      "middleName" -> "testMiddleName",
      "lastName" -> "testLastName"
    ))).get)

    when(sourceRequest.userAnswers).thenReturn(ua)
    whenReady(OUT.callTransform(sourceRequest)) { transformedRequest =>
      transformedRequest.leadTrusteeName mustBe "testFirstName testLastName"
    }
  }

  "Pulls the org name from userAnswers into the Request" in {
    val OUT = new Harness(mock[MessagesApi])
    val sourceRequest = mock[DataRequest[AnyContent]]

    val ua = UserAnswers("id",
      "UTRUTRUTR",
      "sessionId", "newId",
      LocalDate.now(),
      Json.obj().transform(pages.leadtrustee.organisation.NamePage.path.json.put(JsString("org name"))).get)

    when(sourceRequest.userAnswers).thenReturn(ua)
    whenReady(OUT.callTransform(sourceRequest)) { transformedRequest =>
      transformedRequest.leadTrusteeName mustBe "org name"
    }
  }

  "Provides default text when the name isn't in userAnswers" in {
    val sourceRequest = mock[DataRequest[AnyContent]]
    val messagesApi = mock[MessagesApi]
    val messages = mock[Messages]
    when(messagesApi.preferred(any[Request[AnyContent]])).thenReturn(messages)
    when(messages("leadTrusteeName.defaultText")).thenReturn("defaultValue")

    val OUT = new Harness(messagesApi)

    val ua = UserAnswers("id", "UTRUTRUTR", "sessionId", "newId", LocalDate.now(), Json.obj())

    when(sourceRequest.userAnswers).thenReturn(ua)
    whenReady(OUT.callTransform(sourceRequest)) { transformedRequest =>
      transformedRequest.leadTrusteeName mustBe "defaultValue"
    }
  }
}
