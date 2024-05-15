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

package mapping.extractors

import mapping.extractors.leadtrustee.{LeadTrusteeIndividualExtractor, LeadTrusteeOrganisationExtractor}
import mapping.extractors.trustee.{TrusteeIndividualExtractor, TrusteeOrganisationExtractor}
import models._

import javax.inject.Inject
import scala.util.Try

class TrusteeExtractors @Inject()(trusteeIndividualExtractor: TrusteeIndividualExtractor,
                                  trusteeOrganisationExtractor: TrusteeOrganisationExtractor,
                                  leadTrusteeIndividualExtractor: LeadTrusteeIndividualExtractor,
                                  leadTrusteeOrganisationExtractor: LeadTrusteeOrganisationExtractor) {

  def extractTrusteeIndividual(userAnswers: UserAnswers, trustee: TrusteeIndividual, index: Int): Try[UserAnswers] = {
    trusteeIndividualExtractor.extract(userAnswers, trustee, index)
  }

  def extractTrusteeOrganisation(userAnswers: UserAnswers, trustee: TrusteeOrganisation, index: Int): Try[UserAnswers] = {
    trusteeOrganisationExtractor.extract(userAnswers, trustee, index)
  }

  def extractLeadTrusteeIndividual(userAnswers: UserAnswers, trustee: LeadTrusteeIndividual): Try[UserAnswers] = {
    leadTrusteeIndividualExtractor.extract(userAnswers, trustee)
  }

  def extractLeadTrusteeOrganisation(userAnswers: UserAnswers, trustee: LeadTrusteeOrganisation): Try[UserAnswers] = {
    leadTrusteeOrganisationExtractor.extract(userAnswers, trustee)
  }

}
