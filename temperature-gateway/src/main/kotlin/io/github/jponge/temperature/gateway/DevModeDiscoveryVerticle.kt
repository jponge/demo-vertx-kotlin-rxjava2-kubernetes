package io.github.jponge.temperature.gateway

import io.reactivex.rxkotlin.subscribeBy
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.servicediscovery.ServiceDiscovery
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint
import org.slf4j.LoggerFactory

class DevModeDiscoveryVerticle : AbstractVerticle() {

  val logger = LoggerFactory.getLogger(DevModeDiscoveryVerticle::class.java)

  override fun start() {
    logger.info("Service discovery for development purposes, manually injecting references")
    val discovery = ServiceDiscovery.create(vertx)

    val host = "localhost"
    val path = "/api/temperature"
    val meta = json {
      obj("app" to "temperature-service")
    }

    val s1 = HttpEndpoint.createRecord("temp1", host, 6000, path, meta)
    val s2 = HttpEndpoint.createRecord("temp2", host, 6001, path, meta)
    val s3 = HttpEndpoint.createRecord("temp3", host, 6002, path, meta)

    discovery
      .rxPublish(s1)
      .flatMap { discovery.rxPublish(s2) }
      .flatMap { discovery.rxPublish(s3) }
      .subscribeBy(onSuccess = {
        logger.info("All 3 local services have been published")
      }, onError = {
        logger.error("Error while publishing a service", it)
      })
  }
}
