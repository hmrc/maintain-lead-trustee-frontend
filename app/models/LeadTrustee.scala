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

package models

import java.time.LocalDate
import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait LeadTrustee

object LeadTrustee {

  implicit val writes: Writes[LeadTrustee] = Writes[LeadTrustee] {
    case lti: LeadTrusteeIndividual   => Json.toJson(lti)(LeadTrusteeIndividual.writes)
    case lto: LeadTrusteeOrganisation => Json.toJson(lto)(LeadTrusteeOrganisation.writes)
  }

  implicit val reads: Reads[LeadTrustee] = Reads { (data: JsValue) =>
    val allErrors: Either[collection.Seq[(JsPath, collection.Seq[JsonValidationError])], LeadTrustee] = for {
      indErrs <- data.validate[LeadTrusteeIndividual].asEither.left
      orgErrs <- data.validate[LeadTrusteeOrganisation].asEither.left
    } yield indErrs.map(pair => (pair._1, JsonValidationError("Failed to read as LeadTrusteeIndividual") +: pair._2)) ++
      orgErrs.map(pair => (pair._1, JsonValidationError("Failed to read as LeadTrusteeOrganisation") +: pair._2))

    allErrors match {
      case Right(lt)  => JsSuccess(lt)
      case Left(errs) => JsError(errs)
    }
  }

}

case class LeadTrusteeIndividual(
  bpMatchStatus: Option[BpMatchStatus],
  name: Name,
  dateOfBirth: LocalDate,
  phoneNumber: String,
  email: Option[String] = None,
  identification: IndividualIdentification,
  address: Address,
  countryOfResidence: Option[String] = None,
  nationality: Option[String] = None
) extends LeadTrustee

object LeadTrusteeIndividual {

  implicit val reads: Reads[LeadTrusteeIndividual] = (
    (__ \ Symbol("bpMatchStatus")).readNullable[BpMatchStatus] and
      (__ \ Symbol("name")).read[Name] and
      (__ \ Symbol("dateOfBirth")).read[LocalDate] and
      (__ \ Symbol("phoneNumber")).read[String] and
      (__ \ Symbol("email")).readNullable[String] and
      (__ \ Symbol("identification")).read[IndividualIdentification] and
      (__ \ Symbol("identification") \ Symbol("address")).read[Address] and
      (__ \ Symbol("countryOfResidence")).readNullable[String] and
      (__ \ Symbol("nationality")).readNullable[String]
  )(LeadTrusteeIndividual.apply _)

  implicit val writes: Writes[LeadTrusteeIndividual] = (
    (__ \ Symbol("bpMatchStatus")).writeNullable[BpMatchStatus] and
      (__ \ Symbol("name")).write[Name] and
      (__ \ Symbol("dateOfBirth")).write[LocalDate] and
      (__ \ Symbol("phoneNumber")).write[String] and
      (__ \ Symbol("email")).writeNullable[String] and
      (__ \ Symbol("identification")).write[IndividualIdentification] and
      (__ \ Symbol("identification") \ Symbol("address")).write[Address] and
      (__ \ Symbol("countryOfResidence")).writeNullable[String] and
      (__ \ Symbol("nationality")).writeNullable[String]
  )(unlift(LeadTrusteeIndividual.unapply))

}

case class LeadTrusteeOrganisation(
  name: String,
  phoneNumber: String,
  email: Option[String] = None,
  utr: Option[String],
  address: Address,
  countryOfResidence: Option[String] = None
) extends LeadTrustee

object LeadTrusteeOrganisation {

  implicit val reads: Reads[LeadTrusteeOrganisation] = (
    (__ \ Symbol("name")).read[String] and
      (__ \ Symbol("phoneNumber")).read[String] and
      (__ \ Symbol("email")).readNullable[String] and
      (__ \ Symbol("identification") \ Symbol("utr")).readNullable[String] and
      (__ \ Symbol("identification") \ Symbol("address")).read[Address] and
      (__ \ Symbol("countryOfResidence")).readNullable[String]
  )(LeadTrusteeOrganisation.apply _)

  implicit val writes: Writes[LeadTrusteeOrganisation] = (
    (__ \ Symbol("name")).write[String] and
      (__ \ Symbol("phoneNumber")).write[String] and
      (__ \ Symbol("email")).writeNullable[String] and
      (__ \ Symbol("identification") \ Symbol("utr")).writeNullable[String] and
      (__ \ Symbol("identification") \ Symbol("address")).write[Address] and
      (__ \ Symbol("countryOfResidence")).writeNullable[String]
  )(unlift(LeadTrusteeOrganisation.unapply))

}
