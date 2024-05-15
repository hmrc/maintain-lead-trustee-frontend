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

import models.Constants._
import models.Trustee.{legallyIncapableWrites, readMentalCapacity}
import models.YesNoDontKnow.{No, Yes}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

sealed trait Trustee {
  val provisional: Boolean
  def isNewlyAdded: Boolean = provisional
  val `type`: String
}

object Trustee {

  implicit val writes: Writes[Trustee] = Writes[Trustee] {
    case trustee: TrusteeIndividual => Json.toJson(trustee)(TrusteeIndividual.writes)
    case trustee: TrusteeOrganisation => Json.toJson(trustee)(TrusteeOrganisation.formats)
  }

  implicit val reads : Reads[Trustee] = Reads { data : JsValue =>
    val allErrors: Either[collection.Seq[(JsPath, collection.Seq[JsonValidationError])], Trustee] = for {
      indErrs <- (data \ INDIVIDUAL_TRUSTEE).validate[TrusteeIndividual].asEither.left
      orgErrs <- (data \ BUSINESS_TRUSTEE).validate[TrusteeOrganisation].asEither.left
    } yield indErrs.map(pair => (pair._1, JsonValidationError("Failed to read as TrusteeIndividual") +: pair._2)) ++
      orgErrs.map(pair => (pair._1, JsonValidationError("Failed to read as TrusteeOrganisation") +: pair._2))

    allErrors match {
      case Right(t) => JsSuccess(t)
      case Left(errs) => JsError(errs)
    }
  }

  def readMentalCapacity: Reads[Option[YesNoDontKnow]] =
    (__ \ Symbol("legallyIncapable")).readNullable[Boolean].flatMap[Option[YesNoDontKnow]] { x: Option[Boolean] =>
      Reads(_ => JsSuccess(YesNoDontKnow.fromBoolean(x)))
    }

  def legallyIncapableWrites: Writes[YesNoDontKnow] = new Writes[YesNoDontKnow] {
    override def writes(o: YesNoDontKnow): JsValue = o match {
      case Yes => JsBoolean(false)
      case No => JsBoolean(true)
      case _ => JsNull
    }
  }
}

case class TrusteeIndividual(name: Name,
                             dateOfBirth: Option[LocalDate],
                             phoneNumber: Option[String],
                             identification: Option[IndividualIdentification],
                             address: Option[Address],
                             countryOfResidence: Option[String] = None,
                             nationality: Option[String] = None,
                             mentalCapacityYesNo: Option[YesNoDontKnow] = None,
                             entityStart: LocalDate,
                             provisional: Boolean) extends Trustee {

  override val `type`: String = INDIVIDUAL_TRUSTEE
}

object TrusteeIndividual {

  def readNullableAtSubPath[T:Reads](subPath : JsPath) : Reads[Option[T]] = Reads (
    _.transform(subPath.json.pick)
      .flatMap(_.validate[T])
      .map(Some(_))
      .recoverWith(_ => JsSuccess(None))
  )

  implicit val reads: Reads[TrusteeIndividual] = (
    (__ \ Symbol("name")).read[Name] and
      (__ \ Symbol("dateOfBirth")).readNullable[LocalDate] and
      (__ \ Symbol("phoneNumber")).readNullable[String] and
      __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ Symbol("identification"))) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ Symbol("identification") \ Symbol("address"))) and
      (__ \ Symbol("countryOfResidence")).readNullable[String] and
      (__ \ Symbol("nationality")).readNullable[String] and
      readMentalCapacity and
      (__ \ "entityStart").read[LocalDate] and
      (__ \ "provisional").read[Boolean]
    )(TrusteeIndividual.apply _)

  implicit val writes: Writes[TrusteeIndividual] = (
    (__ \ Symbol("name")).write[Name] and
      (__ \ Symbol("dateOfBirth")).writeNullable[LocalDate] and
      (__ \ Symbol("phoneNumber")).writeNullable[String] and
      (__ \ Symbol("identification")).writeNullable[IndividualIdentification] and
      (__ \ Symbol("identification") \ Symbol("address")).writeNullable[Address] and
      (__ \ Symbol("countryOfResidence")).writeNullable[String] and
      (__ \ Symbol("nationality")).writeNullable[String] and
      (__ \ Symbol("legallyIncapable")).writeNullable[YesNoDontKnow](legallyIncapableWrites) and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
    )(unlift(TrusteeIndividual.unapply))

}

case class TrusteeOrganisation(name: String,
                               phoneNumber: Option[String] = None,
                               email: Option[String] = None,
                               identification: Option[TrustIdentificationOrgType],
                               countryOfResidence: Option[String] = None,
                               entityStart: LocalDate,
                               provisional: Boolean) extends Trustee {

  override val `type`: String = BUSINESS_TRUSTEE
}

object TrusteeOrganisation {
  implicit val formats: Format[TrusteeOrganisation] = Json.format[TrusteeOrganisation]
}


case class TrustIdentificationOrgType(safeId: Option[String],
                                      utr: Option[String],
                                      address: Option[Address])

object TrustIdentificationOrgType {
  implicit val formats: Format[TrustIdentificationOrgType] = Json.format[TrustIdentificationOrgType]
}
