package cn.zenliu.vertx.kotlin

import io.vertx.core.http.*
import io.vertx.ext.web.*
import io.vertx.kotlin.coroutines.*

/**
 *
 */
class WebApi : CoroutineVerticle() {
	val router: Router by lazy { Router.router(vertx) }
	val server: HttpServer by lazy { vertx.createHttpServer() }

	override suspend fun start() {
		register()
		server.requestHandler(router).listen(8080)
	}

	override suspend fun stop() {
		server.close()
	}

	private suspend fun register() {
		router.get("/hello")
			.handler { ctx ->
				ctx.response().end("hello")
			}
	}
}
