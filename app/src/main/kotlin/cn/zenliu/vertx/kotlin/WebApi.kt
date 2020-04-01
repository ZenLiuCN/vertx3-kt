package cn.zenliu.vertx.kotlin

import io.vertx.core.*
import io.vertx.core.http.*
import io.vertx.core.logging.*
import io.vertx.ext.web.*
import io.vertx.kotlin.coroutines.*
import java.time.*

/**
 *
 */
class WebApi : CoroutineVerticle() {
	private val router: Router by lazy { Router.router(vertx) }
	private val server: HttpServer by lazy { vertx.createHttpServer() }
	private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
	private val port: Int get() = config.getInteger("http.port", 8080)

	override suspend fun start() {

		register()
		server.requestHandler(router).listen(port)
		logger.info("service listen on $port")

	}

	override suspend fun stop() {
		server.close()
	}

	private suspend fun register() {
		router.get("/hello")
			.handler { ctx ->
				ctx.response().end("hello${Instant.now().atOffset(ZoneOffset.of("+8"))}")
			}
	}
}
