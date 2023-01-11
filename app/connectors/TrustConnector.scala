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

package connectors

import config.FrontendAppConfig
import models.{LeadTrustee, RemoveTrustee, TrustDetails, Trustee, Trustees}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readRaw}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {

  def getLeadTrustee(identifier: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LeadTrustee] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/$identifier/transformed/lead-trustee"
    http.GET[LeadTrustee](url)
  }

  def getTrustDetails(identifier: String)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"${config.trustsUrl}/trusts/trust-details/$identifier/transformed"
    http.GET[TrustDetails](url)
  }

  def addTrustee(identifier: String, trustee: Trustee)
                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/add/$identifier"
    http.POST[Trustee, HttpResponse](url, trustee)
  }

  def amendLeadTrustee(identifier: String, leadTrustee: LeadTrustee)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/amend-lead/$identifier"
    http.POST[LeadTrustee, HttpResponse](url, leadTrustee)
  }

  def amendTrustee(identifier: String, index: Int, trustee: Trustee)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/amend/$identifier/$index"
    http.POST[Trustee, HttpResponse](url, trustee)
  }

  def getTrustees(identifier: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Trustees] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/$identifier/transformed/trustee"
    http.GET[Trustees](url)
  }

  def removeTrustee(identifier: String, trustee: RemoveTrustee)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/$identifier/remove"
    http.PUT[JsValue, HttpResponse](url, Json.toJson(trustee))
  }

  def promoteTrustee(identifier: String, index: Int, newLeadTrustee: LeadTrustee)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"${config.trustsUrl}/trusts/trustees/promote/$identifier/$index"
    http.POST[LeadTrustee, HttpResponse](url, newLeadTrustee)
  }

  def isTrust5mld(identifier: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"${config.trustsUrl}/trusts/$identifier/is-trust-5mld"
    http.GET[Boolean](url)
  }

}
