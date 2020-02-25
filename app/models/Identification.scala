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

import play.api.libs.json.{Format, Json, Reads, Writes, __}

sealed trait IndividualIdentification
object IndividualIdentification {
  implicit val reads: Reads[IndividualIdentification] =
    (__ \ 'passport \ 'countryOfIssue).read[String] flatMap {
      case "GB" => (__ \ 'passport).read[Passport].widen[IndividualIdentification]
      case _ => (__ \ 'passport).read[IdCard].widen[IndividualIdentification]
    } orElse
    __.read[NationalInsuranceNumber].widen[IndividualIdentification]

  implicit val writes: Writes[IndividualIdentification] = Writes {
    case ni:NationalInsuranceNumber =>Json.toJson(ni)(NationalInsuranceNumber.format)
    case p:Passport => Json.obj("passport" -> Json.toJson(p)(Passport.format))
    case i:IdCard=> Json.obj("passport" -> Json.toJson(i)(IdCard.format))
  }
}

case class NationalInsuranceNumber(nino: String) extends IndividualIdentification
object NationalInsuranceNumber{
  implicit val format: Format[NationalInsuranceNumber] = Json.format[NationalInsuranceNumber]
}

case class Passport(number: String,
                    expirationDate: LocalDate,
                    countryOfIssue: String) extends IndividualIdentification
object Passport {
  implicit val format: Format[Passport] = Json.format[Passport]
}

case class IdCard(number: String,
                    expirationDate: LocalDate,
                    countryOfIssue: String) extends IndividualIdentification
object IdCard {
  implicit val format: Format[IdCard] = Json.format[IdCard]
}

