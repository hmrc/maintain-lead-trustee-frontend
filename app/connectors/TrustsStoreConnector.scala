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

package connectors

import config.FrontendAppConfig
import models.FeatureResponse
import models.TaskStatus.TaskStatus
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustsStoreConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) {

  private val baseUrl: String = s"${config.trustsStoreUrl}/trusts-store"

  def updateTaskStatus(identifier: String, taskStatus: TaskStatus)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val url: String = s"$baseUrl/maintain/tasks/update-trustees/$identifier"
    http.post(url"$url").withBody(Json.toJson(taskStatus)).execute[HttpResponse]
  }

  def getFeature(feature: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FeatureResponse] = {
    val url: String = s"$baseUrl/features/$feature"
    http.get(url"$url").execute[FeatureResponse]
  }

}
