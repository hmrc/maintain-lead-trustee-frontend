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

package services

import connectors.TrustConnector
import models.{AllTrustees, LeadTrustee, LeadTrusteeOrganisation, RemoveTrustee, Trustee, TrusteeOrganisation, Trustees}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait TrustService {

  def getAllTrustees(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AllTrustees]

  def getLeadTrustee(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[LeadTrustee]]

  def getTrustees(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Trustees]

  def removeTrustee(identifier: String, trustee: RemoveTrustee)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[HttpResponse]

  def getTrustee(identifier: String, index: Int)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Trustee]

  def getBusinessUtrs(identifier: String, index: Option[Int], adding: Boolean)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]]

}

class TrustServiceImpl @Inject()(connector: TrustConnector) extends TrustService {

  override def getAllTrustees(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AllTrustees] = {
    for {
      lead <- getLeadTrustee(identifier)
      trustees <- getTrustees(identifier)
    } yield {
      AllTrustees(lead, trustees.trustees)
    }
  }

  override def getLeadTrustee(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[LeadTrustee]] =
    connector.getLeadTrustee(identifier).map(Some(_))

  override def getTrustees(identifier: String)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Trustees] = {
    connector.getTrustees(identifier)
  }

  override def removeTrustee(identifier: String, trustee: RemoveTrustee)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[HttpResponse] = {
    connector.removeTrustee(identifier, trustee)
  }

  override def getTrustee(identifier: String, index: Int)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Trustee] = {
    getTrustees(identifier).map(_.trustees(index))
  }

  override def getBusinessUtrs(identifier: String, index: Option[Int], adding: Boolean)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[String]] = {
    getAllTrustees(identifier).map { all =>

      val leadTrusteeUtr: List[String] = all.lead.fold[List[String]](Nil) {
        case x: LeadTrusteeOrganisation if index.isDefined || adding => x.utr.map(List(_)).getOrElse(Nil)
        case _ => Nil
      }

      val trusteeUtrs: List[String] = all.trustees
        .zipWithIndex
        .filterNot(x => index.contains(x._2))
        .collect { case (x: TrusteeOrganisation, _) => x.identification.flatMap(_.utr) }
        .flatten

      leadTrusteeUtr ++ trusteeUtrs
    }
  }

}
