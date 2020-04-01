package cn.zenliu.vertx.kotlin

import io.vertx.config.*
import io.vertx.core.http.*
import io.vertx.core.json.*
import io.vertx.core.logging.*
import io.vertx.ext.web.*
import io.vertx.kotlin.core.json.*
import io.vertx.kotlin.coroutines.*
import java.time.*

/**
 *
 */
class WebApi : CoroutineVerticle() {
	private val router: Router by lazy { Router.router(vertx) }
	private val server: HttpServer by lazy { vertx.createHttpServer() }
	private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
	private lateinit var conf: JsonObject
	private val port get() = conf.get<JsonObject>("http").getInteger("port", 8080)
	private val retriever by lazy {
		ConfigRetriever.create(vertx,
			ConfigRetrieverOptions()
				.addStore(
					ConfigStoreOptions(
						json {
							obj(
								"type" to "file",
								"format" to "hocon",
								"config" to obj {
									put("path", "app.conf")
								}
							)

						}
					)))
	}


	override suspend fun start() {
		retriever.getConfig {
			conf = it.result()
			register()
			server.requestHandler(router).listen(port)
			logger.info("service listen on $port")
		}
	}

	override suspend fun stop() {
		server.close()
		logger.info("service listen on $port")
	}

	private fun register() {
		router.get("/hello")
			.handler { ctx ->
				ctx.response().end("hello${Instant.now().atOffset(ZoneOffset.of("+8"))}")
			}
	}
}
