package cn.zenliu.vertx.kotlin.reactive

import cn.zenliu.vertx.kotlin.*
import io.vertx.core.*
import io.vertx.kotlin.core.json.*
import io.vertx.kotlin.pgclient.*
import io.vertx.kotlin.sqlclient.*
import io.vertx.pgclient.*
import io.vertx.sqlclient.*
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Transaction
import kotlinx.coroutines.*
import org.jooq.*
import org.jooq.SQLDialect.*
import org.jooq.conf.*
import org.jooq.impl.*
import kotlin.reflect.*


object Database {
	private lateinit var vertx: Vertx
	private lateinit var scope: CoroutineScope
	val client: Pool by lazy {
		val connOpt = AppLauncher.config.getJsonObject("postgres")
		val poolOpt = connOpt.getJsonObject("pool")
		PgPool.pool(
			vertx,
			pgConnectOptionsOf(
				cachePreparedStatements = connOpt["cachePreparedStatements"],
				connectTimeout = connOpt["connectTimeout"],
				crlPaths = connOpt["crlPaths"],
				crlValues = connOpt["crlValues"],
				database = connOpt["database"],
				enabledCipherSuites = connOpt["enabledCipherSuites"],
				enabledSecureTransportProtocols = connOpt["enabledSecureTransportProtocols"],
				host = connOpt["host"],
				hostnameVerificationAlgorithm = connOpt["hostnameVerificationAlgorithm"],
				idleTimeout = connOpt["idleTimeout"],
				idleTimeoutUnit = connOpt["idleTimeoutUnit"],
				jdkSslEngineOptions = connOpt["jdkSslEngineOptions"],
				keyStoreOptions = connOpt["keyStoreOptions"],
				localAddress = connOpt["localAddress"],
				logActivity = connOpt["logActivity"],
				metricsName = connOpt["metricsName"],
				openSslEngineOptions = connOpt["openSslEngineOptions"],
				password = connOpt["password"],
				pemKeyCertOptions = connOpt["pemKeyCertOptions"],
				pemTrustOptions = connOpt["pemTrustOptions"],
				pfxKeyCertOptions = connOpt["pfxKeyCertOptions"],
				pfxTrustOptions = connOpt["pfxTrustOptions"],
				pipeliningLimit = connOpt["pipeliningLimit"],
				port = connOpt["port"],
				preparedStatementCacheMaxSize = connOpt["preparedStatementCacheMaxSize"],
				preparedStatementCacheSqlLimit = connOpt["preparedStatementCacheSqlLimit"],
				properties = connOpt["properties"],
				proxyOptions = connOpt["proxyOptions"],
				receiveBufferSize = connOpt["receiveBufferSize"],
				reconnectAttempts = connOpt["reconnectAttempts"],
				reconnectInterval = connOpt["reconnectInterval"],
				reuseAddress = connOpt["reuseAddress"],
				reusePort = connOpt["reusePort"],
				sendBufferSize = connOpt["sendBufferSize"],
				soLinger = connOpt["soLinger"],
				ssl = connOpt["ssl"],
				sslHandshakeTimeout = connOpt["sslHandshakeTimeout"],
				sslHandshakeTimeoutUnit = connOpt["sslHandshakeTimeoutUnit"],
				sslMode = connOpt["sslMode"],
				tcpCork = connOpt["tcpCork"],
				tcpFastOpen = connOpt["tcpFastOpen"],
				tcpKeepAlive = connOpt["tcpKeepAlive"],
				tcpNoDelay = connOpt["tcpNoDelay"],
				tcpQuickAck = connOpt["tcpQuickAck"],
				trafficClass = connOpt["trafficClass"],
				trustAll = connOpt["trustAll"],
				trustStoreOptions = connOpt["trustStoreOptions"],
				useAlpn = connOpt["useAlpn"],
				usePooledBuffers = connOpt["usePooledBuffers"],
				user = connOpt["user"]
			), poolOptionsOf(
			maxSize = poolOpt["maxSize"],
			maxWaitQueueSize = poolOpt["maxWaitQueueSize"]
		))
	}
	val dsl = DSL.using(POSTGRES, Settings()
		.withStatementType(StatementType.STATIC_STATEMENT))

	suspend inline fun <reified T : Any> query(act: DSLContext.() -> ResultQuery<*>) = run {
		val q = act.invoke(dsl)
		client.preparedQuery(q.sql)
			.executeAwait()
			.map { it.mapTo(T::class) }
	}

	suspend inline fun transaction(act: Database.(Transaction) -> Unit) =
		client.beginAwait().let { act.invoke(this, it) }

	fun init(v: Vertx) {
		this.vertx = v
	}
}

/**
 * BOOLEAN (java.lang.Boolean)
 * INT2 (java.lang.Short)
 * INT4 (java.lang.Integer)
 * INT8 (java.lang.Long)
 * FLOAT4 (java.lang.Float)
 * FLOAT8 (java.lang.Double)
 * CHAR (java.lang.String)
 * VARCHAR (java.lang.String)
 * TEXT (java.lang.String)
 * ENUM (java.lang.String)
 * NAME (java.lang.String)
 * SERIAL2 (java.lang.Short)
 * SERIAL4 (java.lang.Integer)
 * SERIAL8 (java.lang.Long)
 * NUMERIC (io.vertx.sqlclient.data.Numeric)
 * UUID (java.util.UUID)
 * DATE (java.time.LocalDate)
 * TIME (java.time.LocalTime)
 * TIMETZ (java.time.OffsetTime)
 * TIMESTAMP (java.time.LocalDateTime)
 * TIMESTAMPTZ (java.time.OffsetDateTime)
 * INTERVAL (io.vertx.pgclient.data.Interval)
 * BYTEA (io.vertx.core.buffer.Buffer)
 * JSON (io.vertx.core.json.JsonObject, io.vertx.core.json.JsonArray, Number, Boolean, String, io.vertx.sqlclient.Tuple#JSON_NULL)
 * JSONB (io.vertx.core.json.JsonObject, io.vertx.core.json.JsonArray, Number, Boolean, String, io.vertx.sqlclient.Tuple#JSON_NULL)
 * POINT (io.vertx.pgclient.data.Point)
 * LINE (io.vertx.pgclient.data.Line)
 * LSEG (io.vertx.pgclient.data.LineSegment)
 * BOX (io.vertx.pgclient.data.Box)
 * PATH (io.vertx.pgclient.data.Path)
 * POLYGON (io.vertx.pgclient.data.Polygon)
 * CIRCLE (io.vertx.pgclient.data.Circle)
 * TSVECTOR (java.lang.String)
 * TSQUERY (java.lang.String)
 * @receiver Row
 * @param klz KClass<T>
 * @return T?
 */
fun <T : Any> Row.mapTo(klz: KClass<T>) = when {
	size() == 0 -> null
	else -> klz.constructors.find { it.parameters.size == this.size() }
		?.let {
			it.callBy(
				it.parameters.associateWith { p ->
					this.get(
						p.type.toJavaClass().javaObjectType,
						p.index)
				}
			)
		}
}

fun <T : Any> RowSet<Row>.mapTo(klz: KClass<T>, byName: String) = this.map { }

fun KType.toJavaClass() = (this.classifier as? KClass<*>) ?: throw Throwable("unknown type to get")
