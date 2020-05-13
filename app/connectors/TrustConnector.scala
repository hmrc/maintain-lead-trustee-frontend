/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import models.{LeadTrustee, RemoveTrustee, TrustStartDate, Trustee, Trustees}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private def getTrustStartDateUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/trust-details"

  private def getLeadTrusteeUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/transformed/lead-trustee"
  private def amendLeadTrusteeUrl(utr: String) = s"${config.trustsUrl}/trusts/trustees/amend-lead/$utr"

  private def getTrusteesUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/transformed/trustees"
  private def addTrusteeUrl(utr: String) = s"${config.trustsUrl}/trusts/trustees/add/$utr"
  private def amendTrusteeUrl(utr: String, index: Int) = s"${config.trustsUrl}/trusts/trustees/amend/$utr/$index"
  private def removeTrusteeUrl(utr: String) = s"${config.trustsUrl}/trusts/trustees/$utr/remove"
  private def promoteTrusteeUrl(utr: String, index: Int) = s"${config.trustsUrl}/trusts/trustees/promote/$utr/$index"

  def getLeadTrustee(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[LeadTrustee] = {
    http.GET[LeadTrustee](getLeadTrusteeUrl(utr))
  }

  def getTrustStartDate(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[TrustStartDate] = {
    http.GET[TrustStartDate](getTrustStartDateUrl(utr))
  }

  def addTrustee(utr: String, trustee: Trustee)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[HttpResponse] = {
    http.POST[Trustee, HttpResponse](addTrusteeUrl(utr), trustee)(Trustee.writes, HttpReads.readRaw, hc, ec)
  }

  def amendLeadTrustee(utr: String, leadTrustee: LeadTrustee)(implicit hc: HeaderCarrier, ec : ExecutionContext) : Future[HttpResponse] = {
    http.POST[LeadTrustee, HttpResponse](amendLeadTrusteeUrl(utr), leadTrustee)(LeadTrustee.writes, HttpReads.readRaw, hc, ec)
  }

  def amendTrustee(utr: String, index: Int, trustee: Trustee)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.POST[Trustee, HttpResponse](amendTrusteeUrl(utr, index), trustee)(Trustee.writes, HttpReads.readRaw, hc, ec)
  }

  def getTrustees(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[Trustees] = {
    http.GET[Trustees](getTrusteesUrl(utr))
  }

  def removeTrustee(utr: String, trustee: RemoveTrustee)(implicit hc: HeaderCarrier, ec : ExecutionContext)= {
    http.PUT[JsValue, HttpResponse](removeTrusteeUrl(utr), Json.toJson(trustee))
  }

  def promoteTrustee(utr: String, index: Int, newLeadTrustee: LeadTrustee)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[HttpResponse] = {
    http.POST[LeadTrustee, HttpResponse](promoteTrusteeUrl(utr, index), newLeadTrustee)(LeadTrustee.writes, HttpReads.readRaw, hc, ec)
  }

}


