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

package services

import models.requests.DataRequest
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class FakeAuthenticationService extends AuthenticationService {

  override def authenticateAgent()(implicit hc: HeaderCarrier): Future[Either[Result, String]]
    = Future.successful(Right("SomeARN"))

  override def authenticateForUtr[A](utr: String)
                              (implicit request: DataRequest[A], hc: HeaderCarrier): Future[Either[Result, DataRequest[A]]]
    = Future.successful(Right(request))

}

class FakeFailingAuthenticationService extends AuthenticationService {

  override def authenticateAgent()(implicit hc: HeaderCarrier): Future[Either[Result, String]]
    = Future.successful(Left(Results.Unauthorized))

  override def authenticateForUtr[A](utr: String)
                              (implicit request: DataRequest[A], hc: HeaderCarrier): Future[Either[Result, DataRequest[A]]]
    = Future.successful(Left(Results.Unauthorized))

}
