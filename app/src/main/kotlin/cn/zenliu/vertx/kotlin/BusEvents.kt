package cn.zenliu.vertx.kotlin

enum class BusEvents {
	CONFIG_UPDATE;

	fun create(verticle: String) = "$name-$verticle"
}