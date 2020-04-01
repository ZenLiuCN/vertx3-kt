@file:JvmName("Launcher")

package cn.zenliu.vertx.kotlin

import io.vertx.core.*
import io.vertx.core.logging.*

class ApplicationLauncher : Launcher() {
	override fun dispatch(args: Array<out String>?) {
		System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)
		super.dispatch(args)
	}
}

fun main(args: Array<String>) {
	ApplicationLauncher().dispatch(mutableListOf("run", "cn.zenliu.vertx.kotlin.WebApi", *args).toTypedArray())
}
