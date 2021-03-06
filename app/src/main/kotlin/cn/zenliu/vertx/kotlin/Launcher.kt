@file:JvmName("Launcher")

package cn.zenliu.vertx.kotlin

import cn.zenliu.vertx.kotlin.BusEvents.*
import com.typesafe.config.*
import io.vertx.config.*
import io.vertx.core.*
import io.vertx.core.json.*
import io.vertx.core.logging.*
import io.vertx.kotlin.core.json.*
import kotlin.collections.set

class AppLauncher : Launcher() {

	override fun dispatch(args: Array<out String>?) {
		System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)
		super.dispatch(args)
	}

	override fun beforeStartingVertx(options: VertxOptions?) {
		mergeVertxOption(options) {
			super.beforeStartingVertx(this)
		}
	}


	override fun afterStartingVertx(vertx: Vertx?) {
		super.afterStartingVertx(vertx?.also {
			setRetriever(it)
		})
	}

	override fun afterConfigParsed(config: JsonObject?) {
		mergeConfig(config) {
			super.afterConfigParsed(this)
		}
	}

	companion object {
		private val configStore: MutableMap<String, ConfigStoreOptions> = mutableMapOf("vertx" to ConfigStoreOptions(
			json {
				obj(
					"type" to "file",
					"format" to "hocon",
					"config" to obj(
						"path" to "vertx.conf"
					)
				)
			}
		))
		private lateinit var gRetriever: ConfigRetriever

		val config by lazy {
			ConfigFactory.load("vertx.conf").resolve().root().render(ConfigRenderOptions.concise()
				.setJson(true).setComments(false).setFormatted(false)).let { JsonObject(it) }
		}

		private fun VertxOptions.toJson() = run {
			val json = mutableMapOf<String, Any?>()
			val obj = this
			if (obj.addressResolverOptions != null) {
				json["addressResolverOptions"] = obj.addressResolverOptions.toJson()
			}
			json["blockedThreadCheckInterval"] = obj.blockedThreadCheckInterval
			if (obj.blockedThreadCheckIntervalUnit != null) {
				json["blockedThreadCheckIntervalUnit"] = obj.blockedThreadCheckIntervalUnit.name
			}
			if (obj.clusterHost != null) {
				json["clusterHost"] = obj.clusterHost
			}
			json["clusterPingInterval"] = obj.clusterPingInterval
			json["clusterPingReplyInterval"] = obj.clusterPingReplyInterval
			json["clusterPort"] = obj.clusterPort
			if (obj.clusterPublicHost != null) {
				json["clusterPublicHost"] = obj.clusterPublicHost
			}
			json["clusterPublicPort"] = obj.clusterPublicPort
			json["clustered"] = obj.isClustered
			if (obj.eventBusOptions != null) {
				json["eventBusOptions"] = obj.eventBusOptions.toJson()
			}
			json["eventLoopPoolSize"] = obj.eventLoopPoolSize
			json["fileResolverCachingEnabled"] = obj.isFileResolverCachingEnabled
			if (obj.fileSystemOptions != null) {
				json["fileSystemOptions"] = obj.fileSystemOptions.toJson()
			}
			json["haEnabled"] = obj.isHAEnabled
			if (obj.haGroup != null) {
				json["haGroup"] = obj.haGroup
			}
			json["internalBlockingPoolSize"] = obj.internalBlockingPoolSize
			json["maxEventLoopExecuteTime"] = obj.maxEventLoopExecuteTime
			if (obj.maxEventLoopExecuteTimeUnit != null) {
				json["maxEventLoopExecuteTimeUnit"] = obj.maxEventLoopExecuteTimeUnit.name
			}
			json["maxWorkerExecuteTime"] = obj.maxWorkerExecuteTime
			if (obj.maxWorkerExecuteTimeUnit != null) {
				json["maxWorkerExecuteTimeUnit"] = obj.maxWorkerExecuteTimeUnit.name
			}
			if (obj.metricsOptions != null) {
				json["metricsOptions"] = obj.metricsOptions.toJson()
			}
			json["preferNativeTransport"] = obj.preferNativeTransport
			json["quorumSize"] = obj.quorumSize
			json["warningExceptionTime"] = obj.warningExceptionTime
			if (obj.warningExceptionTimeUnit != null) {
				json["warningExceptionTimeUnit"] = obj.warningExceptionTimeUnit.name
			}
			json["workerPoolSize"] = obj.workerPoolSize
			JsonObject(json)
		}

		private fun setRetriever(vertx: Vertx) {
			gRetriever = ConfigRetriever.create(vertx,
				ConfigRetrieverOptions().apply {
					stores.addAll(configStore.values)
				})
			gRetriever.listen {
				val json = it.newConfiguration
				vertx.eventBus().publish(CONFIG_UPDATE.name, json)
			}
		}

		/*private fun loadConfig(vertx: Vertx, then: () -> Unit) {
			val retriever = ConfigRetriever.create(vertx,
				ConfigRetrieverOptions()
					.addStore(configStore["vertx"]))
			retriever.getConfig {
				gConfig = it.result()
				when {
					!gConfig.containsKey("configStore") || gConfig.getJsonObject("configStore") == null -> Unit
					else -> gConfig.getJsonObject("configStore")
						.map { (k, v) -> k to ConfigStoreOptions(v as? JsonObject) }
						.let { cf -> configStore.putAll(cf) }
				}
				retriever.close()
				then.invoke()
			}
		}*/

		private fun mergeVertxOption(options: VertxOptions?, then: VertxOptions?.() -> Unit) {
			when {
				config.containsKey("global") && options != null
				-> VertxOptions(options.toJson().mergeIn(config.getJsonObject("global", JsonObject()), true))
				config.containsKey("global") && options == null
				-> VertxOptions(config.getJsonObject("global", JsonObject()))
				else -> options
			}.let { then.invoke(it) }
		}

		private fun mergeConfig(config: JsonObject?, then: JsonObject?.() -> Unit) {
			then(config?.mergeIn(this.config) ?: this.config)
		}

	}
}

fun main(args: Array<String>) {
	AppLauncher()
		.dispatch(
			if (args.contains("run"))
				args
			else
				mutableListOf("run", "cn.zenliu.vertx.kotlin.WebApi", *args).toTypedArray()
		)
}
