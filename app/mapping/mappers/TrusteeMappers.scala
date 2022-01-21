/*
 * Copyright 2022 HM Revenue & Customs
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

package mapping.mappers

import mapping.mappers.leadtrustee.{LeadTrusteeIndividualMapper, LeadTrusteeOrganisationMapper}
import mapping.mappers.trustee.{TrusteeIndividualMapper, TrusteeOrganisationMapper}
import models._

import javax.inject.Inject

class TrusteeMappers @Inject()(trusteeIndividualMapper: TrusteeIndividualMapper,
                               trusteeOrganisationMapper: TrusteeOrganisationMapper,
                               leadTrusteeIndividualMapper: LeadTrusteeIndividualMapper,
                               leadTrusteeOrganisationMapper: LeadTrusteeOrganisationMapper) {

  def mapToTrusteeIndividual(userAnswers: UserAnswers): Option[TrusteeIndividual] = {
    trusteeIndividualMapper.map(userAnswers)
  }

  def mapToTrusteeOrganisation(userAnswers: UserAnswers): Option[TrusteeOrganisation] = {
    trusteeOrganisationMapper.map(userAnswers)
  }

  def mapToLeadTrusteeIndividual(userAnswers: UserAnswers): Option[LeadTrusteeIndividual] = {
    leadTrusteeIndividualMapper.map(userAnswers)
  }

  def mapToLeadTrusteeOrganisation(userAnswers: UserAnswers): Option[LeadTrusteeOrganisation] = {
    leadTrusteeOrganisationMapper.map(userAnswers)
  }

}
