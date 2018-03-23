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

package io.github.jponge.temperature

import io.vertx.core.AbstractVerticle
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.slf4j.LoggerFactory
import java.util.*

class TemperatureVerticle : AbstractVerticle() {

  private val uuid = UUID.randomUUID().toString()
  private val logger = LoggerFactory.getLogger(TemperatureVerticle::class.java)

  private var temperature: Double = 21.0

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

    vertx.eventBus().consumer<Double>("temperature.backdoor") {
      temperature = it.body()
    }
  }

  private fun updateTemperature(random: Random, delta: Double) =
    if (random.nextBoolean()) (temperature + delta) else (temperature - delta)
}
