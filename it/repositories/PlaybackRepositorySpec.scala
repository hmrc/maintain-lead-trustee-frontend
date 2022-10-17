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

package repositories

import java.time.LocalDate

import models.UserAnswers
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext.Implicits.global

class PlaybackRepositorySpec extends AnyWordSpec with Matchers
  with ScalaFutures with OptionValues with MongoSupport with MongoSuite with BeforeAndAfterEach {

  override def beforeEach() = await(repository.collection.deleteMany(BsonDocument()).toFuture())

  lazy val repository: PlaybackRepositoryImpl = new PlaybackRepositoryImpl(mongoComponent, config)

  "a session repository" should {

    "must return None when no answer exists" in {
      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
      val utr = "Testing"
      val sessionId = "Test"

      repository.get(internalId, utr, sessionId).futureValue mustBe None
    }

  }

  "must return the userAnswers after insert" in {
    val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
    val utr = "Testing"
    val sessionId = "Test"
    val newId = s"$internalId-$utr-$sessionId"
    val trustStartDate = LocalDate.parse("2022-01-02")
    val userAnswers: UserAnswers = UserAnswers(internalId,utr,sessionId,newId,trustStartDate)

    repository.get(internalId, utr, sessionId).futureValue mustBe None

    repository.set(userAnswers).futureValue mustBe true

    val userAnswerstest = repository.get(internalId, utr, sessionId).futureValue
    userAnswerstest.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers)

  }

  "must return the userAnswers after update" in {
    val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"
    val utr = "Testing"
    val sessionId = "Test"
    val newId = s"$internalId-$utr-$sessionId"
    val trustStartDate = LocalDate.parse("2022-01-02")
    val userAnswers: UserAnswers = UserAnswers(internalId,utr,sessionId,newId,trustStartDate)
    val userAnswers2 = userAnswers.copy(data = Json.obj("key" -> "123"), isUnderlyingData5mld = true)

    repository.get(internalId, utr, sessionId).futureValue mustBe None

    repository.set(userAnswers).futureValue mustBe true

    val userAnswerstest = repository.get(internalId, utr, sessionId).futureValue
    userAnswerstest.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers)

    //update

    repository.set(userAnswers2).futureValue mustBe true

    val userAnswerstest2 = repository.get(internalId, utr, sessionId).futureValue
    userAnswerstest2.map(_.copy(updatedAt = userAnswers.updatedAt)) mustBe Some(userAnswers2)

  }

}
