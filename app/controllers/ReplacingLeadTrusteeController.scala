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

package controllers

import controllers.actions.StandardActionSets
import controllers.leadtrustee.individual.{routes => ltiRts}
import controllers.leadtrustee.organisation.{routes => ltoRts}
import forms.ReplaceLeadTrusteeFormProvider
import handlers.ErrorHandler
import mapping.extractors.leadtrustee._
import models.YesNoDontKnow.No
import models._
import models.requests.DataRequest
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RadioOption
import views.html.ReplacingLeadTrusteeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ReplacingLeadTrusteeController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                playbackRepository: PlaybackRepository,
                                                trust: TrustService,
                                                standardActionSets: StandardActionSets,
                                                formProvider: ReplaceLeadTrusteeFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: ReplacingLeadTrusteeView,
                                                errorHandler: ErrorHandler,
                                                individualTrusteeToLeadTrusteeExtractor: IndividualTrusteeToLeadTrusteeExtractor,
                                                organisationTrusteeToLeadTrusteeExtractor: OrganisationTrusteeToLeadTrusteeExtractor
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messageKeyPrefix: String = "replacingLeadTrustee"

  private val form: Form[String] = formProvider.withPrefix(messageKeyPrefix)

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.identifier) map {
        case AllTrustees(leadTrustee, trustees) =>
          val radioOptions = generateRadioOptions(trustees)
          Ok(view(form, getLeadTrusteeName(leadTrustee), radioOptions))
      } recoverWith {
        recovery
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.identifier) flatMap {
        case AllTrustees(leadTrustee, trustees) =>
          form.bindFromRequest().fold(
            formWithErrors => {
              val radioOptions = generateRadioOptions(trustees)
              Future.successful(BadRequest(view(formWithErrors, getLeadTrusteeName(leadTrustee), radioOptions)))
            },
            value => {
              val index = value.toInt
              trustees(index) match {
                case trustee: TrusteeIndividual =>
                  val extractedAnswers = individualTrusteeToLeadTrusteeExtractor.extract(request.userAnswers, trustee, index)
                  populateUserAnswersAndRedirect(extractedAnswers, ltiRts.NeedToAnswerQuestionsController.onPageLoad())
                case trustee: TrusteeOrganisation =>
                  val extractedAnswers = organisationTrusteeToLeadTrusteeExtractor.extract(request.userAnswers, trustee, index)
                  populateUserAnswersAndRedirect(extractedAnswers, ltoRts.NeedToAnswerQuestionsController.onPageLoad())
              }
            }
          )
      } recoverWith {
        recovery
      }
  }

  private def generateRadioOptions(trustees: List[Trustee]): List[RadioOption] = {
    trustees
      .zipWithIndex
      .filter(_._1 match {
        case trustee: TrusteeIndividual => !trustee.mentalCapacityYesNo.contains(No)
        case _: TrusteeOrganisation => true
      })
      .map { x =>
        val name = x._1 match {
          case trustee: TrusteeIndividual => trustee.name.displayName
          case trustee: TrusteeOrganisation => trustee.name
        }
        RadioOption(s"$messageKeyPrefix.${x._2}", s"${x._2}", name)
      }
  }

  private def getLeadTrusteeName(leadTrustee: Option[LeadTrustee])(implicit request: DataRequest[AnyContent]): String = {
    leadTrustee match {
      case Some(individual: LeadTrusteeIndividual) => individual.name.displayName
      case Some(organisation: LeadTrusteeOrganisation) => organisation.name
      case None => request.messages(messagesApi)("leadTrusteeName.defaultText")
    }
  }

  private def populateUserAnswersAndRedirect(extractedAnswers: Try[UserAnswers],
                                             redirectUrl: Call): Future[Result] = {
    for {
      updatedAnswers <- Future.fromTry(extractedAnswers)
      _ <- playbackRepository.set(updatedAnswers)
    } yield Redirect(redirectUrl)
  }

  private def recovery(implicit request: DataRequest[AnyContent]): PartialFunction[Throwable, Future[Result]] = {
    case e =>
      logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
        s" Problem getting trustees: ${e.getMessage}")

      Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
  }

}
