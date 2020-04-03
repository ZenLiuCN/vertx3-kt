package cn.zenliu.vertx.kotlin.reactive.case


fun String.guessStyle(unicode: Boolean = false): CaseStyle? = if (unicode)
	UnicodeStyle.values().find { it.validate(this) }
else
	AsciiStyle.values().find { it.validate(this) }


fun String.formatStyle(from: UnicodeStyle, to: UnicodeStyle) = to.format(this, from)
fun String.formatStyle(from: AsciiStyle, to: AsciiStyle) = to.format(this, from)

fun String.toStyle(style: AsciiStyle) = guessStyle(false)?.let { style.format(this, it) }
fun String.toStyle(style: UnicodeStyle) = guessStyle(true)?.let { style.format(this, it) }


fun String.toCamel(unicode: Boolean = false) = if (unicode) toStyle(UnicodeStyle.Camel) else toStyle(AsciiStyle.Camel)
fun String.toPascal(unicode: Boolean = false) = if (unicode) toStyle(UnicodeStyle.Pascal) else toStyle(AsciiStyle.Pascal)
fun String.toSnake(unicode: Boolean = false) = if (unicode) toStyle(UnicodeStyle.Snake) else toStyle(AsciiStyle.Snake)
fun String.toSnakeUpper(unicode: Boolean = false) = if (unicode) toStyle(UnicodeStyle.SnakeUpper) else toStyle(AsciiStyle.SnakeUpper)
fun String.toKebab(unicode: Boolean = false) = if (unicode) toStyle(UnicodeStyle.Kebab) else toStyle(AsciiStyle.Kebab)
fun String.toKebabUpper(unicode: Boolean = false) = if (unicode) toStyle(UnicodeStyle.KebabUpper) else toStyle(AsciiStyle.KebabUpper)


