/*
 * Copyright 2018 Julien Ponge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jponge.zlack

import io.reactivex.rxkotlin.subscribeBy
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.Message
import io.vertx.reactivex.ext.mongo.MongoClient
import org.slf4j.LoggerFactory
import java.time.Instant

class MessageStoreVerticle : AbstractVerticle() {

  private val logger = LoggerFactory.getLogger(MessageStoreVerticle::class.java)

  private lateinit var client: MongoClient

  private val MONGO_COLLECTION = "messages"

  override fun start() {
    val host = config().getString("mongo-host", "localhost")
    val port = config().getString("mongo-port", "27017").toInt()
    logger.info("Starting the message store with MongoDB host=${host}, port=${port}")

    client = MongoClient.createShared(vertx, json {
      obj(
        "host" to host,
        "port" to port,
        "db_name" to "zlack",
        "useObjectId" to true
      )
    })

    vertx.eventBus().consumer<JsonObject>("messages.store", this::store)
    vertx.eventBus().consumer<JsonObject>("messages.get-all", this::all)
  }

  private fun store(cmd: Message<JsonObject>) {
    val now = Instant.now().epochSecond
    val documentWithTimestamp = cmd.body().copy().put("timestamp", System.currentTimeMillis())
    client.rxInsert(MONGO_COLLECTION, documentWithTimestamp)
      .subscribeBy(
        onSuccess = { id ->
          logger.info("Stored: id=${id} - ${cmd.body().encode()}")
          cmd.reply(json { obj("id" to id) })
          vertx.eventBus().publish("events.new-message", json {
            obj(
              "id" to id,
              "author" to cmd.body().getString("author"),
              "content" to cmd.body().getString("content"),
              "timestamp" to now
            )
          })
        },
        onError = { err ->
          logger.error("Could not store ${cmd.body().encode()}", err.cause)
          cmd.fail(1, err.message)
        })
  }

  private fun all(cmd: Message<JsonObject>) {
    client.rxFind(MONGO_COLLECTION, JsonObject())
      .map { jsonObjects ->
        jsonObjects.map { obj ->
          json {
            obj(
              "id" to obj.getString("_id"),
              "author" to obj.getString("author", "???"),
              "content" to obj.getString("content", ""),
              "timestamp" to obj.getLong("timestamp", System.currentTimeMillis())
            )
          }
        }
      }
      .map { json { obj("messages" to it) } }
      .subscribeBy(
        onSuccess = { messages ->
          logger.info("Retrieved ${messages.getJsonArray("messages").size()} messages")
          cmd.reply(messages)
        },
        onError = { err ->
          logger.error("Could not retrieve messages", err)
          cmd.fail(2, err.message)
        })
  }
}
