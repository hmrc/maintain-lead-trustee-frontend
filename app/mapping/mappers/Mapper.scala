/*
 * Copyright 2026 HM Revenue & Customs
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

import models.UserAnswers
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.reflect.{ClassTag, classTag}

abstract class Mapper[T: ClassTag] extends Logging {

  def map(answers: UserAnswers): Option[T] =
    answers.data.validate[T](reads) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors)     =>
        logger.error(
          s"[UTR/URN: ${answers.identifier}]" +
            s" Failed to rehydrate ${classTag[T].runtimeClass.getSimpleName} from UserAnswers due to $errors"
        )
        None
    }

  val reads: Reads[T]

}
