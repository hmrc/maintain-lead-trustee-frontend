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

package controllers.leadtrustee

import com.google.inject.Inject
import connectors.TrustConnector
import controllers.actions.StandardActionSets
import controllers.leadtrustee.individual.actions.NameRequiredAction
import mapping.LeadTrusteesExtractor
import models.{LeadTrusteeIndividual, Mode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewmodels.AnswerSection
import viewmodels.leadtrustee.individual.CheckYourAnswersHelper
import views.html.leadtrustee.LeadTrusteeDetailsView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DetailsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            standardActionSets: StandardActionSets,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: LeadTrusteeDetailsView,
                                            connector: TrustConnector,
                                            extractor: LeadTrusteesExtractor,
                                            repository: PlaybackRepository,
                                            checkYourAnswersHelper: CheckYourAnswersHelper,
                                            nameRequiredAction: NameRequiredAction
                                          ) (implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (standardActionSets.IdentifiedUserWithData andThen nameRequiredAction).async {
    implicit request =>

      connector.getLeadTrustee(request.userAnswers.utr).flatMap {
        case trusteeInd: LeadTrusteeIndividual =>
          val answers: Try[UserAnswers] = extractor.extractLeadTrusteeIndividual(request.userAnswers, trusteeInd)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _ <- repository.set(updatedAnswers)
          } yield {
            val bound = checkYourAnswersHelper.bind(updatedAnswers, request.leadTrusteeName)
            val sections = Seq(AnswerSection(None, Seq(
              bound.name,
              bound.dateOfBirth,
              bound.nationalInsuranceNumber,
              bound.liveInTheUkYesNoPage,
              bound.ukAddress,
              bound.nonUkAddress,
              bound.emailAddressYesNo,
              bound.emailAddress,
              bound.telephoneNumber
            ).flatten))

            Ok(view(sections))
          }
        case _ => val sections = Seq(AnswerSection(None, Seq()))

          Future.successful(Ok(view(sections)))
      }
  }
  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.IdentifiedUserWithData.async {
    implicit request =>
        ???
  }
}
