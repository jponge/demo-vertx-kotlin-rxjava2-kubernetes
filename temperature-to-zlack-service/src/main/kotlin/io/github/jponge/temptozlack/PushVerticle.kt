package io.github.jponge.temptozlack

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.awaitResult
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

class PushVerticle : CoroutineVerticle() {

  private val logger = LoggerFactory.getLogger(PushVerticle::class.java)

  override suspend fun start() {
    logger.info("Starting")
    val webClient = WebClient.create(vertx)

    while (true) {
      try {
        awaitEvent<Long> { vertx.setTimer(15_000, it) }
        val response = gatherTemperatures(webClient)
        for (obj in response.body().getJsonArray("temperatures")) {
          val temp = obj as JsonObject
          val temperature = temp.getDouble("temperature")
          if (temperature >= 25.0) {
            val message = json {
              obj(
                "author" to "(alerts)",
                "content" to "🚨 temperature of sensor ${temp.getString("id")} is about ${temperature.roundToInt()}C"
              )
            }
            postTemperatures(webClient, message)
          }
        }
      } catch (t: Throwable) {
        logger.error("Something went wrong", t)
      }
    }
  }

  private suspend fun postTemperatures(webClient: WebClient, message: JsonObject) {
    logger.info("Posting a temperature: ${message.encode()}")
    awaitResult<HttpResponse<Buffer>> {
      webClient
        .post(8080, "zlack", "/api/messages")
        .putHeader("Content-Type", "application/json")
        .sendJson(message, it)
    }
  }

  private suspend fun gatherTemperatures(webClient: WebClient): HttpResponse<JsonObject> {
    logger.info("Gathering temperatures")
    return awaitResult<HttpResponse<JsonObject>> {
      webClient
        .get(8080, "temperature-gateway", "/temperatures")
        .`as`(BodyCodec.jsonObject())
        .send(it)
    }
  }
}
