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

package controllers.trustee

import models.{Trustee, TrusteeIndividual, TrusteeOrganisation}
import play.api.Logging
import play.api.i18n.I18nSupport
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

trait TrusteeFilterer extends FrontendBaseController with I18nSupport with Logging {

  def filterOutMentallyIncapableTrustees(trustees: List[Trustee]): List[(Trustee, Int)] = {
    trustees
      .zipWithIndex
      .filter(_._1 match {
        case trustee: TrusteeIndividual => !trustee.mentalCapacityYesNo.contains(false)
        case _: TrusteeOrganisation => true
      })
  }
}
