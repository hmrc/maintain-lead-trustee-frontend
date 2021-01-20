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

package mapping.extractors

import base.SpecBase
import models.IndividualOrBusiness.Business
import models.{NonUkAddress, TrustIdentificationOrgType, TrusteeOrganisation, UkAddress}
import pages.trustee.IndividualOrBusinessPage
import pages.trustee.amend.organisation._

import java.time.LocalDate

class TrusteeOrganisationExtractorSpec extends SpecBase {

  private val index = 0

  private val name: String = "Name"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "country")

  private val extractor = new TrusteeOrganisationExtractor()

  "should populate user answers when trustee has a UTR" in {

    val trustee = TrusteeOrganisation(
      name = name,
      phoneNumber = None,
      email = None,
      identification = Some(TrustIdentificationOrgType(None, Some("utr"), None)),
      entityStart = date,
      provisional = true
    )

    val result = extractor.extract(emptyUserAnswers, trustee, index).get

    result.get(IndividualOrBusinessPage).get mustBe Business
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe true
    result.get(UtrPage).get mustBe "utr"
    result.get(AddressYesNoPage) mustNot be(defined)
    result.get(AddressInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)

  }

  "should populate user answers when trustee has a UK address" in {

    val trustee = TrusteeOrganisation(
      name = name,
      phoneNumber = None,
      email = None,
      identification = Some(TrustIdentificationOrgType(None, None, Some(ukAddress))),
      entityStart = date,
      provisional = true
    )

    val result = extractor.extract(emptyUserAnswers, trustee, index).get

    result.get(IndividualOrBusinessPage).get mustBe Business
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe false
    result.get(UtrPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(AddressInTheUkYesNoPage).get mustBe true
    result.get(UkAddressPage).get mustBe ukAddress
    result.get(NonUkAddressPage) mustNot be(defined)

  }

  "should populate user answers when trustee has a non-UK address" in {

    val trustee = TrusteeOrganisation(
      name = name,
      phoneNumber = None,
      email = None,
      identification = Some(TrustIdentificationOrgType(None, None, Some(nonUkAddress))),
      entityStart = date,
      provisional = true
    )

    val result = extractor.extract(emptyUserAnswers, trustee, index).get

    result.get(IndividualOrBusinessPage).get mustBe Business
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe false
    result.get(UtrPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe true
    result.get(AddressInTheUkYesNoPage).get mustBe false
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage).get mustBe nonUkAddress

  }

  "should populate user answers when trustee has no UTR or address" in {

    val trustee = TrusteeOrganisation(
      name = name,
      phoneNumber = None,
      email = None,
      identification = Some(TrustIdentificationOrgType(None, None, None)),
      entityStart = date,
      provisional = true
    )

    val result = extractor.extract(emptyUserAnswers, trustee, index).get

    result.get(IndividualOrBusinessPage).get mustBe Business
    result.get(NamePage).get mustBe name
    result.get(UtrYesNoPage).get mustBe false
    result.get(UtrPage) mustNot be(defined)
    result.get(AddressYesNoPage).get mustBe false
    result.get(AddressInTheUkYesNoPage) mustNot be(defined)
    result.get(UkAddressPage) mustNot be(defined)
    result.get(NonUkAddressPage) mustNot be(defined)

  }

}