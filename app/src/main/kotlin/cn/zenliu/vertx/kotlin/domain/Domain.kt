package cn.zenliu.vertx.kotlin.domain

import cn.zenliu.vertx.kotlin.AppLauncher
import cn.zenliu.vertx.kotlin.model.tables.daos.DoctorDao
import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.pgclient.pgConnectOptionsOf
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import org.jooq.DSLContext
import org.jooq.SQLDialect.MYSQL
import org.jooq.SQLDialect.POSTGRES
import org.jooq.impl.DSL
import java.sql.Connection
import javax.sql.DataSource


object Domain {
	val cfg by lazy { AppLauncher.config.getJsonObject("pg", JsonObject()) }
	val opt by lazy {
		pgConnectOptionsOf(
			cachePreparedStatements = cfg.get("cachePreparedStatements"),
			connectTimeout = cfg.get("connectTimeout"),
			crlPaths = cfg.get("crlPaths"),
			crlValues = cfg.get("crlValues"),
			database = cfg.get("database"),
			enabledCipherSuites = cfg.get("enabledCipherSuites"),
			enabledSecureTransportProtocols = cfg.get("enabledSecureTransportProtocols"),
			host = cfg.get("host"),
			hostnameVerificationAlgorithm = cfg.get("hostnameVerificationAlgorithm"),
			idleTimeout = cfg.get("idleTimeout"),
			idleTimeoutUnit = cfg.get("idleTimeoutUnit"),
			jdkSslEngineOptions = cfg.get("jdkSslEngineOptions"),
			keyStoreOptions = cfg.get("keyStoreOptions"),
			localAddress = cfg.get("localAddress"),
			logActivity = cfg.get("logActivity"),
			metricsName = cfg.get("metricsName"),
			openSslEngineOptions = cfg.get("openSslEngineOptions"),
			password = cfg.get("password"),
			pemKeyCertOptions = cfg.get("pemKeyCertOptions"),
			pemTrustOptions = cfg.get("pemTrustOptions"),
			pfxKeyCertOptions = cfg.get("pfxKeyCertOptions"),
			pfxTrustOptions = cfg.get("pfxTrustOptions"),
			pipeliningLimit = cfg.get("pipeliningLimit"),
			port = cfg.get("port"),
			preparedStatementCacheMaxSize = cfg.get("preparedStatementCacheMaxSize"),
			preparedStatementCacheSqlLimit = cfg.get("preparedStatementCacheSqlLimit"),
			properties = cfg.get("properties"),
			proxyOptions = cfg.get("proxyOptions"),
			receiveBufferSize = cfg.get("receiveBufferSize"),
			reconnectAttempts = cfg.get("reconnectAttempts"),
			reconnectInterval = cfg.get("reconnectInterval"),
			reuseAddress = cfg.get("reuseAddress"),
			reusePort = cfg.get("reusePort"),
			sendBufferSize = cfg.get("sendBufferSize"),
			soLinger = cfg.get("soLinger"),
			ssl = cfg.get("ssl"),
			sslHandshakeTimeout = cfg.get("sslHandshakeTimeout"),
			sslHandshakeTimeoutUnit = cfg.get("sslHandshakeTimeoutUnit"),
			sslMode = cfg.get("sslMode"),
			tcpCork = cfg.get("tcpCork"),
			tcpFastOpen = cfg.get("tcpFastOpen"),
			tcpKeepAlive = cfg.get("tcpKeepAlive"),
			tcpNoDelay = cfg.get("tcpNoDelay"),
			tcpQuickAck = cfg.get("tcpQuickAck"),
			trafficClass = cfg.get("trafficClass"),
			trustAll = cfg.get("trustAll"),
			trustStoreOptions = cfg.get("trustStoreOptions"),
			useAlpn = cfg.get("useAlpn"),
			usePooledBuffers = cfg["usePooledBuffers"],
			user = cfg.get("user")
		)
	}
	val poolOpt by lazy { PoolOptions(cfg.getJsonObject("pool")) }
	lateinit var client: PgPool
		private set
	val Dsl by lazy { DSL.using(POSTGRES) }
	fun dsl(act: QueryFn) =
		act.invoke(Dsl)

	inline fun <reified T : AbstractReactiveVertxDAO<*, *, *, *, *, *, *>> dao(act: T.() -> Unit) {
		act.invoke(T::class.constructors.first().call(Dsl.configuration(), client))
	}

	private lateinit var vertx: Vertx
	fun setVertx(vertx: Vertx) {
		this.vertx = vertx
		this.client = PgPool.pool(vertx, opt, poolOpt)
	}

}

typealias QueryFn = DSLContext.() -> Unit