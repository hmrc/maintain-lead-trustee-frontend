/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import views.behaviours.ViewBehaviours
import views.html.ChangeLeadTrusteeView

class ChangeLeadTrusteeViewSpec extends ViewBehaviours {

  "ChangeLeadTrustee view" must {

    val application = applicationBuilder().build()

    val view = application.injector.instanceOf[ChangeLeadTrusteeView]

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "changeLeadTrustee")
  }
}