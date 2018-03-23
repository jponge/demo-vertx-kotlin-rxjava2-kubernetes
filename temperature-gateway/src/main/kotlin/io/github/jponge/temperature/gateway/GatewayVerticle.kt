package io.github.jponge.temperature.gateway

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import io.vertx.reactivex.ext.web.client.WebClient
import io.vertx.reactivex.ext.web.codec.BodyCodec
import io.vertx.reactivex.servicediscovery.ServiceDiscovery
import io.vertx.reactivex.servicediscovery.ServiceReference
import io.vertx.servicediscovery.kubernetes.KubernetesServiceImporter
import org.slf4j.LoggerFactory

class GatewayVerticle : AbstractVerticle() {

  private val logger = LoggerFactory.getLogger(GatewayVerticle::class.java)

  private lateinit var discovery: ServiceDiscovery

  private var serviceReferences = mutableListOf<ServiceReference>()
  private var webClients = mutableListOf<WebClient>()

  override fun start(startFuture: Future<Void>) {
    val port = config().getString("http-port", "8080").toInt()
    logger.info("Trying to start a HTTP server on port ${port}")

    discovery = ServiceDiscovery.create(vertx)
    if (config().getString("use-kubernetes", "false")!!.toBoolean()) {
      discovery.delegate.registerServiceImporter(KubernetesServiceImporter(), JsonObject())
    }

    discoverServices()
    vertx.setPeriodic(10_000) {
      discoverServices()
    }

    val router = Router.router(vertx)
    router.get("/temperatures").handler(this::fetchAllAvailableTemperatures)

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .rxListen(port)
      .subscribeBy(
        onSuccess = {
          logger.info("HTTP server running on port ${port}")
          startFuture.complete()
        },
        onError = {
          logger.error("HTTP server could not be started on port ${port}", it)
          startFuture.fail(it)
        })
  }

  private fun discoverServices() {
    logger.info("Discarding previously discovered services")
    serviceReferences.forEach { it.release() }
    serviceReferences.clear()
    webClients.clear()

    val filter = json {
      obj("app" to "temperature-service")
    }

    discovery.rxGetRecords(filter)
      .toObservable()
      .flatMap { Observable.fromIterable(it) }
      .map { discovery.getReference(it) }
      .subscribeBy(onNext = { ref ->
        logger.info("Discovered ${ref.record().location}")
        serviceReferences.add(ref)
        webClients.add(ref.getAs(WebClient::class.java))
      })
  }

  // TODO: add resiliency on HTTP errors
  private fun fetchAllAvailableTemperatures(context: RoutingContext) {
    logger.info("Request from ${context.request().remoteAddress()}")

    val requests = webClients.map { client ->
      client.get("/api/temperature")
        .`as`(BodyCodec.jsonObject())
        .rxSend()
    }.toList()

    val responses = JsonArray()
    Single.merge(requests).subscribeBy(
      onNext = {
        responses.add(it.body())
      },
      onError = {
        logger.error("Error while talking to the services", it)
        context.response().setStatusCode(500).end()
      },
      onComplete = {
        val payload = json {
          obj("temperatures" to responses)
        }
        context.response()
          .setStatusCode(200)
          .putHeader("Content-Type", "application/json")
          .end(payload.encode())
      })
  }
}
