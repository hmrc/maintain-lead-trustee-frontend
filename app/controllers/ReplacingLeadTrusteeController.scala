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

package controllers

import java.time.LocalDate

import controllers.actions.StandardActionSets
import forms.ReplaceLeadTrusteeFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.IndividualOrBusiness._
import models.requests.DataRequest
import models.{Address, AllTrustees, CombinedPassportOrIdCard, IdCard, IndividualIdentification, LeadTrustee, LeadTrusteeIndividual, LeadTrusteeOrganisation, NationalInsuranceNumber, NonUkAddress, Passport, TrustIdentificationOrgType, TrusteeIndividual, TrusteeOrganisation, UkAddress, UserAnswers}
import pages.leadtrustee.organisation.UtrPage
import pages.leadtrustee.{IndividualOrBusinessPage, individual => ltind, organisation => ltorg}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RadioOption
import views.html.ReplacingLeadTrusteeView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class ReplacingLeadTrusteeController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                playbackRepository: PlaybackRepository,
                                                trust: TrustService,
                                                standardActionSets: StandardActionSets,
                                                formProvider: ReplaceLeadTrusteeFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: ReplacingLeadTrusteeView,
                                                errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val messageKeyPrefix: String = "replacingLeadTrustee"

  val form = formProvider.withPrefix(messageKeyPrefix)
  val defaultRadioOption: RadioOption = RadioOption(s"$messageKeyPrefix.-1", "-1", s"$messageKeyPrefix.add-new")

  private val logger = Logger(getClass)

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.utr) map {
        case AllTrustees(leadTrustee, trustees) =>
          val trusteeNames = trustees
            .map {
              case ind: TrusteeIndividual => ind.name.displayName
              case org: TrusteeOrganisation => org.name
            }
            .zipWithIndex.map {
              x => RadioOption(s"$messageKeyPrefix.${x._2}", s"${x._2}", x._1)
            }

          Ok(view(form, getLeadTrusteeName(leadTrustee), trusteeNames))
      } recoverWith {
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.utr}]" +
            s" user cannot maintain trustees due to there being a problem getting trustees from trusts")

          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getAllTrustees(request.userAnswers.utr) flatMap {
        case AllTrustees(leadTrustee, trustees) =>
          val trusteeNames = trustees.map {
            case ind: TrusteeIndividual => ind.name.displayName
            case org: TrusteeOrganisation => org.name
          }.zipWithIndex.map(
            x => RadioOption(s"$messageKeyPrefix.${x._2}", s"${x._2}", x._1)
          )

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, getLeadTrusteeName(leadTrustee), trusteeNames))),
            value => {
              value.toInt match {
                case index =>
                  trustees(index) match {
                    case ind: TrusteeIndividual => populateUserAnswersAndRedirect(request.userAnswers, ind, index)
                    case org: TrusteeOrganisation => populateUserAnswersAndRedirect(request.userAnswers, org, index)
                  }
              }
            }
          )
      } recoverWith {
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.utr}]" +
            s" user cannot maintain trustees due to there being a problem getting trustees from trusts")

          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }

  private def getLeadTrusteeName(leadTrustee: Option[LeadTrustee])(implicit request: DataRequest[AnyContent]): String = {
    leadTrustee match {
      case Some(ltInd: LeadTrusteeIndividual) => ltInd.name.displayName
      case Some(ltOrg: LeadTrusteeOrganisation) => ltOrg.name
      case None => request.messages(messagesApi)("leadTrusteeName.defaultText")
    }
  }

  private def populateUserAnswersAndRedirect(userAnswers: UserAnswers, trustee: TrusteeIndividual, index: Int) = {
    for {
      updatedAnswers <- Future.fromTry(
        userAnswers.deleteAtPath(pages.leadtrustee.basePath)
          .flatMap(_.set(IndividualOrBusinessPage, Individual))
          .flatMap(_.set(ltind.IndexPage, index))
          .flatMap(_.set(ltind.NamePage, trustee.name))
          .flatMap(answers => extractDateOfBirth(trustee.dateOfBirth, answers))
          .flatMap(answers => extractIndIdentification(trustee.identification, answers))
          .flatMap(answers => extractIndAddress(trustee.address, answers))
          .flatMap(answers => extractIndTelephoneNumber(trustee.phoneNumber, answers))
      )
      _ <- playbackRepository.set(updatedAnswers)
    } yield Redirect(controllers.leadtrustee.individual.routes.NeedToAnswerQuestionsController.onPageLoad())
  }

  private def populateUserAnswersAndRedirect(userAnswers: UserAnswers, trustee: TrusteeOrganisation, index: Int) = {
    for {
      updatedAnswers <- Future.fromTry(
        userAnswers.deleteAtPath(pages.leadtrustee.basePath)
          .flatMap(_.set(IndividualOrBusinessPage, Business))
          .flatMap(_.set(ltorg.IndexPage, index))
          .flatMap(answers => extractOrgIdentification(trustee.identification, answers))
          .flatMap(_.set(ltorg.NamePage, trustee.name))
          .flatMap(answers => extractOrgEmail(trustee.email, answers))
          .flatMap(answers => extractOrgTelephoneNumber(trustee.phoneNumber, answers))
      )
      _ <- playbackRepository.set(updatedAnswers)
    } yield Redirect(controllers.leadtrustee.organisation.routes.NeedToAnswerQuestionsController.onPageLoad())
  }

  private def extractDateOfBirth(dateOfBirth: Option[LocalDate], answers: UserAnswers): Try[UserAnswers] = {
    dateOfBirth match {
      case Some(dob) =>
        answers.set(ltind.DateOfBirthPage, dob)
      case _ =>
        Success(answers)
    }
  }

  private def extractIndIdentification(identification: Option[IndividualIdentification], answers: UserAnswers) = {
    identification map {

      case NationalInsuranceNumber(nino) =>
        answers.set(ltind.UkCitizenPage, true)
          .flatMap(_.set(ltind.NationalInsuranceNumberPage, nino))

      case p:Passport =>
        answers.set(ltind.UkCitizenPage, false)
          .flatMap(_.set(ltind.PassportOrIdCardDetailsPage, p.asCombined))

      case id:IdCard =>
        answers.set(ltind.UkCitizenPage, false)
          .flatMap(_.set(ltind.PassportOrIdCardDetailsPage, id.asCombined))

      case c:CombinedPassportOrIdCard =>
        answers.set(ltind.UkCitizenPage, false)
          .flatMap(_.set(ltind.PassportOrIdCardDetailsPage, c))

    } getOrElse {
      Success(answers)
    }
  }

  private def extractIndAddress(address: Option[Address], answers: UserAnswers) = {
    address.map {
      case uk: UkAddress =>
        answers.set(ltind.LiveInTheUkYesNoPage, true)
          .flatMap(_.set(ltind.UkAddressPage, uk))
      case nonUk: NonUkAddress =>
        answers.set(ltind.LiveInTheUkYesNoPage, false)
          .flatMap(_.set(ltind.NonUkAddressPage, nonUk))
    }.getOrElse(Success(answers))
  }

  private def extractIndTelephoneNumber(phoneNumber: Option[String], answers: UserAnswers) = {
    phoneNumber match {
      case Some(tel) =>
        answers.set(ltind.TelephoneNumberPage, tel)
      case _ =>
        Success(answers)
    }
  }

  private def extractOrgIdentification(identification: Option[TrustIdentificationOrgType], answers: UserAnswers) = {
    identification map {
      case TrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(ltorg.RegisteredInUkYesNoPage, true)
          .flatMap(_.set(UtrPage, utr))
      case TrustIdentificationOrgType(_, None, Some(address)) =>
        answers.set(ltorg.RegisteredInUkYesNoPage, false)
          .flatMap(answers => extractOrgAddress(address, answers))
      case _ => Success(answers)
    } getOrElse {
      Success(answers)
    }
  }

  private def extractOrgAddress(address: Address, answers: UserAnswers) = {
    address match {
      case uk: UkAddress =>
        answers.set(ltorg.AddressInTheUkYesNoPage, true)
          .flatMap(_.set(ltorg.UkAddressPage, uk))
      case nonUk: NonUkAddress =>
        answers.set(ltorg.AddressInTheUkYesNoPage, false)
          .flatMap(_.set(ltorg.NonUkAddressPage, nonUk))

    }
  }

  private def extractOrgEmail(emailAddress: Option[String], answers: UserAnswers) = {
    emailAddress match {
      case Some(email) =>
        answers.set(ltorg.EmailAddressYesNoPage, true)
          .flatMap(_.set(ltorg.EmailAddressPage, email))
      case _ =>
        Success(answers)
    }
  }

  private def extractOrgTelephoneNumber(phoneNumber: Option[String], answers: UserAnswers) = {
    phoneNumber match {
      case Some(tel) =>
        answers.set(ltorg.TelephoneNumberPage, tel)
      case _ =>
        Success(answers)
    }
  }

}