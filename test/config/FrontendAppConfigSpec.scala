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

package config

import base.SpecBase
import play.api.i18n.{Lang, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate

class FrontendAppConfigSpec extends SpecBase {

  val config: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  "FrontendAppConfig" when {

    ".logoutUrl" must {
      "append the useServiceNavigation parameter to the configured feedback URL" in {
        config.logoutUrl mustBe "http://localhost:9514/feedback/trusts?useServiceNavigation"
      }
    }

    ".loginUrl" must {
      "return the configured login URL" in {
        config.loginUrl mustBe "http://localhost:9949/auth-login-stub/gg-sign-in"
      }
    }

    ".loginContinueUrl" must {
      "return the configured login continue URL" in {
        config.loginContinueUrl mustBe "http://localhost:9781/trusts-registration"
      }
    }

    ".appName" must {
      "return the configured application name" in {
        config.appName mustBe "maintain-lead-trustee-frontend"
      }
    }

    ".logoutAudit" must {
      "return the configured value" in {
        config.logoutAudit mustBe false
      }

      "return true when enabled via config" in {
        val application = new GuiceApplicationBuilder()
          .configure(defaultAppConfigurations ++ Map("microservice.services.features.auditing.logout" -> true))
          .build()

        application.injector.instanceOf[FrontendAppConfig].logoutAudit mustBe true
        application.stop()
      }
    }

    ".languageMap" must {
      "have the correct languageMap" in {
        config.languageMap mustBe Map(
          "english" -> Lang("en"),
          "cymraeg" -> Lang("cy")
        )
      }
    }

    ".trustsUrl" must {
      "return the configured trusts service base URL" in {
        config.trustsUrl mustBe "http://localhost:9782"
      }
    }

    ".trustsStoreUrl" must {
      "return the configured trusts-store service base URL" in {
        config.trustsStoreUrl mustBe "http://localhost:9783"
      }
    }

    ".trustsIndividualCheckUrl" must {
      "return the configured trusts-individual-check service base URL" in {
        config.trustsIndividualCheckUrl mustBe "http://localhost:9846"
      }
    }

    ".locationCanonicalList" must {
      "return the configured file name" in {
        config.locationCanonicalList mustBe "location-autocomplete-canonical-list.json"
      }
    }

    ".locationCanonicalListCY" must {
      "return the configured file name" in {
        config.locationCanonicalListCY mustBe "location-autocomplete-canonical-list-cy.json"
      }
    }

    ".countdownLength" must {
      "return the configured value" in {
        config.countdownLength mustBe 120
      }
    }

    ".timeoutLength" must {
      "return the configured value" in {
        config.timeoutLength mustBe 900
      }
    }

    ".minDate" must {
      "return the configured date" in {
        config.minDate mustBe LocalDate.of(1500, 1, 1)
      }
    }

    ".minLeadTrusteeDob" must {
      "return the configured date" in {
        config.minLeadTrusteeDob mustBe LocalDate.of(1900, 1, 1)
      }
    }

    ".maxMatchingAttempts" must {
      "return the configured value" in {
        config.maxMatchingAttempts mustBe 3
      }
    }

    ".helplineUrl" when {
      "in English mode" must {
        "return trusts helpline URL" in {
          val messages = MessagesImpl(Lang("en"), messagesApi)
          config.helplineUrl(messages) mustBe
            "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/trusts"
        }
      }

      "in Welsh mode" must {
        "return Welsh language helpline URL" in {
          val messages = MessagesImpl(Lang("cy"), messagesApi)
          config.helplineUrl(messages) mustBe
            "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/welsh-language-helplines"
        }
      }
    }
  }

}
