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

package repositories

import models.UtrSession
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import uk.gov.hmrc.mongo.test.MongoSupport

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global

class ActiveSessionRepositorySpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with MongoSupport
    with MongoSuite
    with BeforeAndAfterEach
    with BaseMongoIndexSpec {

  override def beforeEach() = await(repository.collection.deleteMany(BsonDocument()).toFuture())

  lazy val repository: ActiveSessionRepositoryImpl = new ActiveSessionRepositoryImpl(mongoComponent, config)(global)

  "a session repository" should {

    "must return None when no cache exists" in {

      val internalId = "Int-328969d0-557e-4559-sdba-074d0597107e"

      repository.get(internalId).futureValue mustBe None
    }

    "must return a UtrSession when one exists" in {

      val internalId = "Int-328969d0-557e-2559-96ba-074d0597107e"

      val session = UtrSession(internalId, "utr")

      val initial = repository.set(session).futureValue

      initial mustBe true

      repository.get(internalId).futureValue.value.utr mustBe "utr"
    }

    "must override an existing session for an internalId" in {

      val internalId = "Int-328969d0-557e-4559-96ba-0d4d0597107e"

      val session = UtrSession(internalId, "utr")

      repository.set(session).futureValue

      repository.get(internalId).futureValue.value.utr        mustBe "utr"
      repository.get(internalId).futureValue.value.internalId mustBe internalId

      // update

      val session2 = UtrSession(internalId, "utr2")

      repository.set(session2).futureValue

      repository.get(internalId).futureValue.value.utr        mustBe "utr2"
      repository.get(internalId).futureValue.value.internalId mustBe internalId
    }
  }

  "have all expected indexes" in {
    val expectedIndexes = Seq(
      IndexModel(ascending("_id"), IndexOptions().name("_id_")),
      IndexModel(
        ascending("updatedAt"),
        IndexOptions().name("session-updated-at-index").expireAfter(config.cachettlSessionInSeconds, TimeUnit.SECONDS)
      ),
      IndexModel(ascending("utr"), IndexOptions().name("utr-index").unique(false)),
      IndexModel(ascending("internalId"), IndexOptions().name("internal-id-index").unique(false))
    )

    assertIndexes(expectedIndexes, getIndexes(repository.collection))
  }

}
