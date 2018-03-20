package io.github.jponge.temperature

import io.vertx.core.AbstractVerticle
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.slf4j.LoggerFactory
import java.util.*

class TemperatureVerticle : AbstractVerticle() {

  val uuid = UUID.randomUUID().toString()
  val logger = LoggerFactory.getLogger(TemperatureVerticle::class.java)

  var temperature: Double = 21.0

  override fun start() {
    val delta = config().getString("temp-delta", "0.1").toDouble()
    val period = config().getString("update-period", "1000").toLong()
    temperature = config().getString("temp-init", "21.0").toDouble()
    logger.info("Temperature simulation parameters: init=${temperature}, delta=${delta}, period=${period}ms")

    val random = Random()
    vertx.setPeriodic(period) {
      if (random.nextBoolean()) {
        temperature = updateTemperature(random, delta)
        logger.info("Temperature is now ${temperature}")
        vertx.eventBus().publish("temperature.updates", json {
          obj(
            "id" to uuid,
            "temperature" to temperature
          )
        })
      }
    }
  }

  private fun updateTemperature(random: Random, delta: Double) =
    if (random.nextBoolean()) (temperature + delta) else (temperature - delta)
}
