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

import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import models.{MongoDateTimeFormats, UserAnswers}
import play.api.Configuration
import play.api.libs.json._
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlaybackRepositoryImpl @Inject()(override val mongo: MongoDriver,
                                       config: Configuration
                                      )(override implicit val ec: ExecutionContext)
  extends PlaybackRepository
  with IndexManager {

  implicit final val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

  override val collectionName: String = "user-answers"

  override val dropIndexes: Boolean =
    config.get[Boolean]("microservice.services.features.mongo.dropIndexes")

  private val cacheTtl = config.get[Int]("mongodb.playback.ttlSeconds")

  private def collection: Future[JSONCollection] =
    for {
      _ <- ensureIndexes
      res <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
    } yield res

  private val lastUpdatedIndex = MongoIndex(
    key = Seq("updatedAt" -> IndexType.Ascending),
    name = "user-answers-updated-at-index",
    expireAfterSeconds = Some(cacheTtl)
  )

  private val internalIdAndUtrIndex = MongoIndex(
    key = Seq("internalId" -> IndexType.Ascending, "utr" -> IndexType.Ascending),
    name = "internal-id-and-utr-compound-index"
  )

  private def ensureIndexes = for {
      collection              <- mongo.api.database.map(_.collection[JSONCollection](collectionName))
      createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
      createdIdIndex          <- collection.indexesManager.ensure(internalIdAndUtrIndex)
    } yield createdLastUpdatedIndex && createdIdIndex

  override def get(internalId: String, utr: String): Future[Option[UserAnswers]] = {

    val selector = Json.obj(
      "internalId" -> internalId,
      "utr" -> utr
    )

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "updatedAt" -> MongoDateTimeFormats.localDateTimeWrite.writes(LocalDateTime.now)
      )
    )

    for {
      col <- collection
      r <- col.findAndUpdate(
        selector = selector,
        update = modifier,
        fetchNewObject = true,
        upsert = false,
        sort = None,
        fields = None,
        bypassDocumentValidation = false,
        writeConcern = WriteConcern.Default,
        maxTime = None,
        collation = None,
        arrayFilters = Nil
      )
    } yield r.result[UserAnswers]
  }

  override def set(userAnswers: UserAnswers): Future[Boolean] = {

    val selector = Json.obj(
      "internalId" -> userAnswers.internalId,
      "utr" -> userAnswers.identifier
    )

    val modifier = Json.obj(
      "$set" -> userAnswers.copy(updatedAt = LocalDateTime.now)
    )

    for {
      col <- collection
      r <- col.update(ordered = false).one(selector, modifier, upsert = true, multi = false)
    } yield r.ok
  }

  override def resetCache(internalId: String, utr: String): Future[Option[JsObject]] = {
    val selector = Json.obj(
      "internalId" -> internalId,
      "utr" -> utr
    )

    for {
      col <- collection
      r <- col.findAndRemove(selector, None, None, WriteConcern.Default, None, None, Seq.empty)
    } yield r.value
  }
}

trait PlaybackRepository {

  def get(internalId: String, utr: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]

  def resetCache(internalId: String, utr: String): Future[Option[JsObject]]
}
