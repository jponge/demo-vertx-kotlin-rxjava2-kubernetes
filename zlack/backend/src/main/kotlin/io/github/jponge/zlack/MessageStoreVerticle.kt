package io.github.jponge.zlack

import io.reactivex.rxkotlin.subscribeBy
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.Message
import io.vertx.reactivex.ext.mongo.MongoClient
import org.slf4j.LoggerFactory

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
        "db_name" to "zlack",
        "useObjectId" to true
      )
    })

    vertx.eventBus().consumer<JsonObject>("messages.store", this::store)
    vertx.eventBus().consumer<JsonObject>("messages.get-all", this::all)
  }

  private fun store(cmd: Message<JsonObject>) {
    client.rxInsert(MONGO_COLLECTION, cmd.body())
      .subscribeBy(
        onSuccess = { id ->
          logger.info("Stored: id=${id} - ${cmd.body().encode()}")
          cmd.reply(json { obj("id" to id) })
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
              "author" to obj.getString("author", "???"),
              "content" to obj.getString("content", "")
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
