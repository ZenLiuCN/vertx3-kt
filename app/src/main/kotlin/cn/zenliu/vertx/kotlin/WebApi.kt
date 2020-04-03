package cn.zenliu.vertx.kotlin

import cn.zenliu.vertx.kotlin.model.Tables.DOCTOR
import cn.zenliu.vertx.kotlin.reactive.Database
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch


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
				launch(coroutineContext) {
					Database.query<User> {
						select(DOCTOR.ID, DOCTOR.NAME)
							.from(DOCTOR)
							.limit(1)
					}.let {
						ctx.response().end(Json.encode(it))
					}
				}
			}.failureHandler {
				logger.error("error", it.failure())
			}
	}
}

data class User(
	val id: Long,
	val name: String
)