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

package controllers.leadtrustee.organisation

import com.google.inject.Inject
import connectors.TrustConnector
import controllers.actions.StandardActionSets
import controllers.leadtrustee.actions.NameRequiredAction
import handlers.ErrorHandler
import mapping.extractors.TrusteeExtractors
import mapping.mappers.TrusteeMappers
import models.requests.DataRequest
import models.{LeadTrusteeOrganisation, UserAnswers}
import pages.leadtrustee.IsReplacingLeadTrusteePage
import pages.leadtrustee.organisation.IndexPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.checkYourAnswers.TrusteePrintHelpers
import viewmodels.AnswerSection
import views.html.leadtrustee.organisation.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        connector: TrustConnector,
                                        extractor: TrusteeExtractors,
                                        printHelper: TrusteePrintHelpers,
                                        mapper: TrusteeMappers,
                                        repository: PlaybackRepository,
                                        nameAction: NameRequiredAction,
                                        errorHandler: ErrorHandler
                                      )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private def logInfo(implicit request: DataRequest[AnyContent]): String =
    s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]"

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      connector.getLeadTrustee(request.userAnswers.identifier).flatMap {
        case org: LeadTrusteeOrganisation =>
          val answers: Try[UserAnswers] = extractor.extractLeadTrusteeOrganisation(request.userAnswers, org)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _ <- repository.set(updatedAnswers)
          } yield {
            renderLeadTrustee(updatedAnswers, org.name)
          }
        case _ =>
          logger.error(s"$logInfo Expected lead trustee to be of type LeadTrusteeOrganisation")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      } recoverWith {
        case e =>
          logger.error(s"$logInfo Unable to retrieve Lead Trustee from trusts: ${e.getMessage}")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }
  }

  def onPageLoadUpdated(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request => renderLeadTrustee(request.userAnswers, request.leadTrusteeName)(request.request)
  }

  private def renderLeadTrustee(userAnswers: UserAnswers, name: String)(implicit request: DataRequest[AnyContent]): Result = {
    val section: AnswerSection = printHelper.printLeadOrganisationTrustee(
      userAnswers = userAnswers,
      name = name
    )

    Ok(view(section))
  }

  def onSubmit1(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>
      val userAnswers = request.userAnswers
      mapper.mapToLeadTrusteeOrganisation(userAnswers) match {
        case Some(lt) =>
          val transform = () => userAnswers.get(IndexPage) match {
            case None =>
              userAnswers.get(IsReplacingLeadTrusteePage) match {
                case Some(true) =>
                  logger.info(s"$logInfo Adding new lead trustee to replace existing")
                  connector.demoteLeadTrustee(userAnswers.identifier, lt)
                case _ =>
                  logger.info(s"$logInfo Amending lead trustee")
                  connector.amendLeadTrustee(userAnswers.identifier, lt)
              }
            case Some(index) =>
              logger.info(s"$logInfo Promoting lead trustee")
              connector.promoteTrustee(userAnswers.identifier, index, lt)
          }
          submitTransform(transform, userAnswers)
        case _ =>
          logger.error(s"$logInfo Unable to build lead trustee organisation from user answers. Cannot continue with submitting transform.")
          errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async { implicit request =>
    val userAnswers = request.userAnswers
    val identifier = userAnswers.identifier
    mapper.mapToLeadTrusteeOrganisation(userAnswers) match {
      case Some(leadTrustee) =>
        connectorCall(userAnswers, identifier, leadTrustee).flatMap {
          case Right(response) =>
            submitTransform(() => Future.successful(response), userAnswers)
          case Left(error) =>
            logger.error(s"$logInfo $error")
            errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
        }
      case None =>
        logger.error(s"$logInfo Unable to map lead trustee organisation from user answers")
        errorHandler.internalServerErrorTemplate.map(html => InternalServerError(html))
    }
  }

  private def submitTransform(transform: () => Future[HttpResponse], userAnswers: UserAnswers): Future[Result] = {
    logger.info("[CheckDetailsController][submitTransform] Deleting lead trustee from user answers for Organisation")
    for {
      _ <- transform()
      cleanedAnswers <- Future.fromTry(userAnswers.remove(IsReplacingLeadTrusteePage))
      updatedUserAnswers <- Future.fromTry(cleanedAnswers.deleteAtPath(pages.leadtrustee.basePath))
      _ <- repository.set(updatedUserAnswers)
    } yield Redirect(controllers.routes.AddATrusteeController.onPageLoad())
  }

  private def connectorCall(
                                      userAnswers: UserAnswers,
                                      identifier: String,
                                      leadTrustee: LeadTrusteeOrganisation
                                    )(implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[Either[String, HttpResponse]] = {
    val indexPage = userAnswers.get(IndexPage)
    val call: Future[HttpResponse] = indexPage match {
      case Some(index) =>
        logger.info(s"$logInfo Promoting lead trustee at index $index")
        connector.promoteTrustee(identifier, index, leadTrustee)
      case None =>
        userAnswers.get(IsReplacingLeadTrusteePage) match {
          case Some(true) =>
            logger.info(s"$logInfo Adding new lead trustee to replace existing")
            connector.demoteLeadTrustee(userAnswers.identifier, leadTrustee)
          case _ =>
            logger.info(s"$logInfo Amending lead trustee")
            connector.amendLeadTrustee(userAnswers.identifier, leadTrustee)
        }
    }
    call.map { response =>
      if (response.status == OK) Right(response)
      else
        Left(s"$logInfo Connector call failed with status ${response.status}")
    } recover {
      case ex =>
        Left(s"$logInfo Connector call failed with exception: ${ex.getMessage}")
    }
  }
}
