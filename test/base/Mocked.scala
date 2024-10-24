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

package base

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import repositories.{ActiveSessionRepository, PlaybackRepository}

import scala.concurrent.Future

trait Mocked {

  val playbackRepository: PlaybackRepository = Mockito.mock(classOf[PlaybackRepository])
  val mockSessionRepository : ActiveSessionRepository = Mockito.mock(classOf[ActiveSessionRepository])

  when(playbackRepository.set(any())).thenReturn(Future.successful(true))
  when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

}
