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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import models.{TrustAuthAgentAllowed, TrustAuthAllowed, TrustAuthDenied, TrustAuthInternalServerError}
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import utils.WireMockHelper

class TrustAuthConnectorSpec extends SpecBase with WireMockHelper {

  private val authorisedUrl: String = s"/trusts-auth/agent-authorised"
  private def authorisedUrlFor(utr: String): String = s"/trusts-auth/authorised/$utr"

  private def responseFromJson(json: JsValue) = {
    aResponse().withStatus(Status.OK).withBody(json.toString())
  }

  private def allowedResponse = responseFromJson(Json.obj("authorised" -> true))
  private def allowedAgentResponse = responseFromJson(Json.obj("arn" -> "SomeArn"))

  private def redirectResponse(redirectUrl: String) = responseFromJson(Json.obj("redirectUrl" -> redirectUrl))

  private def wiremock(url: String, response: ResponseDefinitionBuilder) = {
    server.stubFor(get(urlEqualTo(url)).willReturn(response))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(defaultAppConfigurations ++ Map("microservice.services.trusts-auth.port" -> server.port()))
    .build()

  private lazy val connector = app.injector.instanceOf[TrustAuthConnector]

  private val utr = "0123456789"

  "TrustAuthConnector" should {
    "authorisedForUtr" when {

      "returns 'Allowed' when" when {
        "service returns with no redirect url" in {

          wiremock(authorisedUrlFor(utr), allowedResponse)

          connector.authorisedForUtr(utr) map { result =>
            result mustEqual TrustAuthAllowed()
          }
        }
      }
      "returns 'Denied' when" when {
        "service returns a redirect url" in {

          wiremock(authorisedUrlFor(utr), redirectResponse("redirect-url"))

          connector.authorisedForUtr(utr) map { result =>
            result mustEqual TrustAuthDenied("redirect-url")
          }
        }
      }
      "returns 'Internal server error' when" when {
        "service returns something not OK" in {

          wiremock(authorisedUrlFor(utr), aResponse().withStatus(Status.INTERNAL_SERVER_ERROR))

          connector.authorisedForUtr(utr) map { result =>
            result mustEqual TrustAuthInternalServerError
          }
        }
      }
    }
    "authorised" when {

      "returns 'Agent Allowed' when" when {
        "service returns with agent authorised response" in {

          wiremock(authorisedUrl, allowedAgentResponse)

          connector.agentIsAuthorised() map { result =>
            result mustEqual TrustAuthAgentAllowed("SomeArn")
          }
        }
      }

      "returns 'Denied' when" when {
        "service returns a redirect url" in {

          wiremock(authorisedUrl, redirectResponse("redirect-url"))

          connector.agentIsAuthorised() map { result =>
            result mustEqual TrustAuthDenied("redirect-url")
          }
        }
      }
      "returns 'Internal server error' when" when {
        "service returns something not OK" in {

          wiremock(authorisedUrl, aResponse().withStatus(Status.INTERNAL_SERVER_ERROR))

          connector.agentIsAuthorised() map { result =>
            result mustEqual TrustAuthInternalServerError
          }
        }
      }
    }

  }
}
