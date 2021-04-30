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

package models

import java.time.LocalDate
import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait LeadTrustee

object LeadTrustee {

  implicit val writes: Writes[LeadTrustee] = Writes[LeadTrustee] {
    case lti: LeadTrusteeIndividual => Json.toJson(lti)(LeadTrusteeIndividual.writes)
    case lto: LeadTrusteeOrganisation => Json.toJson(lto)(LeadTrusteeOrganisation.writes)
  }

  implicit val reads: Reads[LeadTrustee] = Reads { (data:JsValue) =>
    val allErrors: Either[Seq[(JsPath, Seq[JsonValidationError])], LeadTrustee] = for {
      indErrs <- data.validate[LeadTrusteeIndividual].asEither.left
      orgErrs <- data.validate[LeadTrusteeOrganisation].asEither.left
    } yield indErrs.map(pair => (pair._1, JsonValidationError("Failed to read as LeadTrusteeIndividual") +: pair._2)) ++
      orgErrs.map(pair => (pair._1, JsonValidationError("Failed to read as LeadTrusteeOrganisation") +: pair._2))

    allErrors match {
      case Right(lt) => JsSuccess(lt)
      case Left(errs) => JsError(errs)
    }
  }
}

case class LeadTrusteeIndividual(bpMatchStatus: Option[BpMatchStatus],
                                 name: Name,
                                 dateOfBirth: LocalDate,
                                 phoneNumber: String,
                                 email: Option[String] = None,
                                 identification: IndividualIdentification,
                                 address: Address,
                                 countryOfResidence: Option[String] = None,
                                 nationality: Option[String] = None) extends LeadTrustee

object LeadTrusteeIndividual {
  implicit val reads: Reads[LeadTrusteeIndividual] = (
    (__ \ 'bpMatchStatus).readNullable[BpMatchStatus] and
      (__ \ 'name).read[Name] and
      (__ \ 'dateOfBirth).read[LocalDate] and
      (__ \ 'phoneNumber).read[String] and
      (__ \ 'email).readNullable[String] and
      (__ \ 'identification).read[IndividualIdentification] and
      (__ \ 'identification \ 'address).read[Address] and
      (__ \ 'countryOfResidence).readNullable[String] and
      (__ \ 'nationality).readNullable[String]
    )(LeadTrusteeIndividual.apply _)

  implicit val writes: Writes[LeadTrusteeIndividual] = (
    (__ \ 'bpMatchStatus).writeNullable[BpMatchStatus] and
      (__ \ 'name).write[Name] and
      (__ \ 'dateOfBirth).write[LocalDate] and
      (__ \ 'phoneNumber).write[String] and
      (__ \ 'email).writeNullable[String] and
      (__ \ 'identification).write[IndividualIdentification] and
      (__ \ 'identification \ 'address).write[Address] and
      (__ \ 'countryOfResidence).writeNullable[String] and
      (__ \ 'nationality).writeNullable[String]
    )(unlift(LeadTrusteeIndividual.unapply))
}

case class LeadTrusteeOrganisation(name: String,
                                   phoneNumber: String,
                                   email: Option[String] = None,
                                   utr: Option[String],
                                   address: Address,
                                   countryOfResidence: Option[String] = None) extends LeadTrustee

object LeadTrusteeOrganisation {
  implicit val reads: Reads[LeadTrusteeOrganisation] = (
    (__ \ 'name).read[String] and
      (__ \ 'phoneNumber).read[String] and
      (__ \ 'email).readNullable[String] and
      (__ \ 'identification \ 'utr).readNullable[String] and
      (__ \ 'identification \ 'address).read[Address] and
      (__ \ 'countryOfResidence).readNullable[String]
    )(LeadTrusteeOrganisation.apply _)

  implicit val writes: Writes[LeadTrusteeOrganisation] = (
    (__ \ 'name).write[String] and
      (__ \ 'phoneNumber).write[String] and
      (__ \ 'email).writeNullable[String] and
      (__ \ 'identification \ 'utr).writeNullable[String] and
      (__ \ 'identification \ 'address).write[Address] and
      (__ \ 'countryOfResidence).writeNullable[String]
    )(unlift(LeadTrusteeOrganisation.unapply))
}
