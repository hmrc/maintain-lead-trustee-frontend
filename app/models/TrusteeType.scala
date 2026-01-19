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

import viewmodels.RadioOption

sealed trait TrusteeType

object TrusteeType extends Enumerable.Implicits {

  case object LeadTrustee extends WithName("leadTrustee") with TrusteeType
  case object Trustee extends WithName("trustee") with TrusteeType

  val values: List[TrusteeType] = List(
    LeadTrustee, Trustee
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("trusteeType", value.toString)
  }

  implicit val enumerable: Enumerable[TrusteeType] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
