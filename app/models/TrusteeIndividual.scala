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

package models

import java.time.LocalDate

import play.api.libs.json.{Format, Json, Reads, Writes}

case class TrusteeIndividual(lineNo: String,
                             bpMatchStatus: Option[String],
                             name: Name,
                             dateOfBirth: Option[LocalDate],
                             phoneNumber: Option[String],
                             identification: Option[TrustIdentification],
                             entityStart: LocalDate)

object TrusteeIndividual {

  implicit val dateFormat: Format[LocalDate] = Format[LocalDate](Reads.DefaultLocalDateReads, Writes.DefaultLocalDateWrites)

  implicit val trusteeIndividualTypeFormat: Format[TrusteeIndividual] = Json.format[TrusteeIndividual]
}

case class TrustIdentification(safeId: Option[String],
                               nino: Option[String],
                               passport: Option[Passport],
                               address: Option[AddressType])

object TrustIdentification {
  implicit val identificationTypeFormat: Format[TrustIdentification] = Json.format[TrustIdentification]
}

case class AddressType(line1: String,
                       line2: String,
                       line3: Option[String],
                       line4: Option[String],
                       postCode: Option[String],
                       country: String)

object AddressType {
  implicit val addressTypeFormat: Format[AddressType] = Json.format[AddressType]
}