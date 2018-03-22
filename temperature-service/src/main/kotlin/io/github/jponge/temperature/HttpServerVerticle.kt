package io.github.jponge.temperature

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

class HttpServerVerticle : AbstractVerticle() {

  private val logger = LoggerFactory.getLogger(HttpServerVerticle::class.java)

  private var latestTemperatureInfo = JsonObject()

  override fun start(startFuture: Future<Void>) {
    val port = config().getString("http-port", "8080").toInt()
    logger.info("Trying to start a HTTP server on port ${port}")

    vertx.eventBus().consumer<JsonObject>("temperature.updates") { msg ->
      latestTemperatureInfo = msg.body()
    }

    val router = Router.router(vertx)
    router.get("/api/temperature").handler(this::giveTemperature)
    router.put("/api/temperature/:value").handler(this::backdoor)

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(port) {
        if (it.succeeded()) {
          logger.info("HTTP server running on port ${port}")
          startFuture.complete()
        } else {
          logger.error("HTTP server could not be started on port ${port}", it.cause())
          startFuture.fail(it.cause())
        }
      }
  }

  private fun giveTemperature(context: RoutingContext) {
    logger.info("Temperature request from ${context.request().remoteAddress()}")
    if (latestTemperatureInfo.isEmpty) {
      context.response().setStatusCode(500).end()
    } else {
      context.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(latestTemperatureInfo.encode())
    }
  }

  private fun backdoor(context: RoutingContext) {
    val temperature = context.request().getParam("value").toDouble()
    logger.info("Backdoor to update the temperature to ${temperature}")
    vertx.eventBus().send("temperature.backdoor", temperature)
    context.response().end()
  }
}
