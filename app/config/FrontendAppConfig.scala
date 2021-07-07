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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call
import java.time.LocalDate

import uk.gov.hmrc.hmrcfrontend.config.ContactFrontendConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration,
                                   contactFrontendConfig: ContactFrontendConfig) {

  final val ENGLISH = "en"
  final val WELSH = "cy"
  final val UK_COUNTRY_CODE = "GB"

  val maintainATrustOverview: String = configuration.get[String]("urls.maintainATrustOverview")

  private def loadConfig(key: String) = configuration.get[String](key)

  val analyticsToken: String = configuration.get[String](s"google-analytics.token")

  val betaFeedbackUrl = s"${contactFrontendConfig.baseUrl}/contact/beta-feedback?service=${contactFrontendConfig.serviceId}"
  val betaFeedbackUnauthenticatedUrl = s"${contactFrontendConfig.baseUrl}/contact/beta-feedback-unauthenticated?service=${contactFrontendConfig.serviceId}"

  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")
  lazy val locationCanonicalListCY: String = loadConfig("location.canonical.list.allCY")

  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val trustsUrl: String = configuration.get[Service]("microservice.services.trusts").baseUrl
  lazy val trustAuthUrl: String = configuration.get[Service]("microservice.services.trusts-auth").baseUrl

  lazy val trustsStoreUrl: String = configuration.get[Service]("microservice.services.trusts-store").baseUrl

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val countdownLength: Int = configuration.get[Int]("timeout.countdown")
  lazy val timeoutLength: Int = configuration.get[Int]("timeout.length")

  private def getInt(path: String): Int = configuration.get[Int](path)

  private def getDate(entry: String): LocalDate =
    LocalDate.of(
      getInt(s"dates.$entry.year"),
      getInt(s"dates.$entry.month"),
      getInt(s"dates.$entry.day")
    )

  lazy val minDate: LocalDate = getDate("minimum")
  lazy val minLeadTrusteeDob: LocalDate = getDate("minLeadTrusteeDob")

  lazy val trustsIndividualCheckUrl: String = configuration.get[Service]("microservice.services.trusts-individual-check").baseUrl

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  val maxMatchingAttempts: Int = getInt("individual-match.max-attempts")

}
