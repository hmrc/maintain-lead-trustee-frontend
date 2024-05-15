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

package models

import play.api.i18n.{Messages, MessagesProvider}

case class AllTrustees(lead: Option[LeadTrustee], trustees: List[Trustee]) {

  val size: Int = lead.size + trustees.size

  def addToHeading(implicit mp: MessagesProvider): String = size match {
    case x if x > 1 => Messages("addATrustee.count.heading", x)
    case _ => Messages("addATrustee.heading")
  }
}
