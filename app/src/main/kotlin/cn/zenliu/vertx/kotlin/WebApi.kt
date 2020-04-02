package cn.zenliu.vertx.kotlin

import cn.zenliu.vertx.kotlin.reactive.*
import cn.zenliu.vertx.kotlin.reactive.Database
import cn.zenliu.vertx.kotlin.reactive.Database.async
import io.vertx.core.http.*
import io.vertx.core.json.*
import io.vertx.core.logging.*
import io.vertx.ext.web.*
import io.vertx.kotlin.coroutines.*
import org.jetbrains.exposed.sql.*

/**
 *
 */
class WebApi : CoroutineVerticle() {
	private val router: Router by lazy { Router.router(vertx) }
	private val server: HttpServer by lazy { vertx.createHttpServer() }
	private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
	private val port
		get() = config
			.getJsonObject("webApi", JsonObject())
			.getJsonObject("http", JsonObject())
			.getInteger("port", 8080)

	override suspend fun start() {
		register()
		Database.init(vertx)
		server.requestHandler(router).listen(port)
		logger.info("service listen on $port")
	}

	override suspend fun stop() {
		server.close()
		logger.info("service listen on $port")
	}

	private fun register() {
		router.get("/hello")
			.handler { ctx ->
				User.select {
					User.id eq 1
				}.async {
					ctx.response().end(Json.encode(it.result()))
				}
			}.failureHandler {
				logger.error("error", it.failure())
			}
	}
}
