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

import java.util.concurrent.TimeUnit
import org.mongodb.scala.model.{FindOneAndUpdateOptions, ReplaceOptions, Updates}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Filters.equal
import java.time.LocalDateTime
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.UtrSession
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActiveSessionRepositoryImpl @Inject()(val mongoComponent: MongoComponent,
                                            val config: FrontendAppConfig
                                           )(implicit val ec: ExecutionContext)
  extends PlayMongoRepository[UtrSession](
    collectionName = "session",
    mongoComponent = mongoComponent,
    domainFormat = Format(UtrSession.reads,UtrSession.writes),
    indexes = Seq(
      IndexModel(
        ascending("updatedAt"),
        IndexOptions()
          .unique(false)
          .name("session-updated-at-index")
          .expireAfter(config.cachettlSessionInSeconds, TimeUnit.SECONDS)),
      IndexModel(
        ascending("utr"),
        IndexOptions()
          .unique(false)
          .name("utr-index")
      )
    ), replaceIndexes = config.dropIndexes

  ) with Logging with ActiveSessionRepository {

  override def get(internalId: String): Future[Option[UtrSession]] = {

    val selector = equal("internalId", internalId)

    val modifier = Updates.set("updatedAt", LocalDateTime.now())

    val updateOption = new FindOneAndUpdateOptions().upsert(false)

    collection.findOneAndUpdate(selector, modifier, updateOption).toFutureOption()
  }

  override def set(session: UtrSession): Future[Boolean] = {

    val selector = equal("internalId", session.internalId)

    collection.replaceOne(selector, session.copy(updatedAt = LocalDateTime.now), ReplaceOptions().upsert(true))
      .headOption().map(_.exists(_.wasAcknowledged()))
  }
}

@ImplementedBy(classOf[ActiveSessionRepositoryImpl])
trait ActiveSessionRepository {

  def get(internalId: String): Future[Option[UtrSession]]

  def set(session: UtrSession): Future[Boolean]
}
