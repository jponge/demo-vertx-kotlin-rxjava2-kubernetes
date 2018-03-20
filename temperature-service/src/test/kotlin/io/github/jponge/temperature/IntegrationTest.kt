package io.github.jponge.temperature

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("👀 Some integration tests")
@ExtendWith(VertxExtension::class)
class IntegrationTest {

  @BeforeEach
  fun prepare(vertx: Vertx, context: VertxTestContext) {

    val tempDeployed = context.checkpoint()
    vertx.deployVerticle(TemperatureVerticle(), context.succeeding {
      tempDeployed.flag()
    })

    val serverDeployed = context.checkpoint()
    vertx.deployVerticle(HttpServerVerticle(), context.succeeding {
      serverDeployed.flag()
    })
  }

  @Test
  @DisplayName("☝️ Check that updates are being published to the event bus")
  fun updates(vertx: Vertx, context: VertxTestContext) {
    vertx.eventBus().consumer<JsonObject>("temperature.updates") { msg ->
      context.verify {
        assertFalse(msg.body().isEmpty)
        assertTrue(msg.body().containsKey("id"))
        assertTrue(msg.body().containsKey("temperature"))
        context.completeNow()
      }
    }
  }

  @Test
  @DisplayName("📡 Check that the HTTP server works")
  fun server(vertx: Vertx, context: VertxTestContext) {
    vertx.setTimer(1000) {
      WebClient.create(vertx)
        .get(8080, "localhost", "/api/temperature")
        .`as`(BodyCodec.jsonObject())
        .send {
          context.verify {
            assertTrue(it.succeeded())
            assertEquals(200, it.result().statusCode())
            context.completeNow()
          }
        }
    }
  }
}
