package cn.zenliu.vertx.kotlin.reactive

import cn.zenliu.vertx.kotlin.*
import io.vertx.core.*
import io.vertx.kotlin.core.json.*
import io.vertx.kotlin.pgclient.*
import io.vertx.kotlin.sqlclient.*
import io.vertx.pgclient.*
import io.vertx.sqlclient.*
import kotlinx.coroutines.*
import kotlin.reflect.*


object Database {
	private lateinit var vertx: Vertx
	private lateinit var scope: CoroutineScope
	val client: SqlClient by lazy {
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


	fun init(v: Vertx) {
		this.vertx = v
	}
}

fun <T : Any> Row.mapTo(klz: KClass<T>) = when {
	size() == 0 -> null
	else -> klz.constructors.find { it.parameters.size == this.size() }?.let {
		it.callBy(
			it.parameters.associateWith { p ->
				this.get(
					(p.type.classifier as? KClass<*>)?.java ?: throw Throwable("unknown type to get"),
					p.index)
			}
		)
	}
}
