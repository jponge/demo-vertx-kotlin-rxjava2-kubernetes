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

package io.github.jponge.temptozlack

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

class PushVerticle : CoroutineVerticle() {

  private val logger = LoggerFactory.getLogger(PushVerticle::class.java)

  override suspend fun start() {
    logger.info("Starting")
    val webClient = WebClient.create(vertx)

    awaitResult<HttpServer> {
      vertx.createHttpServer().requestHandler { req ->
        logger.info("Liveness check")
        req.response().end("Ok")
      }.listen(3000, it)
    }
    logger.info("HTTP server on port 3000 is for liveness probes")

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
