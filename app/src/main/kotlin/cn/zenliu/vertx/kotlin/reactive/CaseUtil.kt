@file:JvmName("CaseUtil")

package cn.zenliu.vertx.kotlin.reactive


import cn.zenliu.vertx.kotlin.reactive.Util.AnyChar
import cn.zenliu.vertx.kotlin.reactive.Util.AsIs
import cn.zenliu.vertx.kotlin.reactive.Util.IsDash
import cn.zenliu.vertx.kotlin.reactive.Util.IsLetterOrDigit
import cn.zenliu.vertx.kotlin.reactive.Util.IsLower
import cn.zenliu.vertx.kotlin.reactive.Util.IsSpace
import cn.zenliu.vertx.kotlin.reactive.Util.IsUnderScore
import cn.zenliu.vertx.kotlin.reactive.Util.IsUpper
import cn.zenliu.vertx.kotlin.reactive.Util.ToDash
import cn.zenliu.vertx.kotlin.reactive.Util.ToLower
import cn.zenliu.vertx.kotlin.reactive.Util.ToSpace
import cn.zenliu.vertx.kotlin.reactive.Util.ToUnderScore
import cn.zenliu.vertx.kotlin.reactive.Util.ToUpper
import cn.zenliu.vertx.kotlin.reactive.Util.dash
import cn.zenliu.vertx.kotlin.reactive.Util.space
import cn.zenliu.vertx.kotlin.reactive.Util.underscore

private object Util {
	internal const val space = ' '
	internal const val dash = '-'
	internal const val underscore = '_'
	internal val IsLetter = Char::isLetter
	internal val IsLetterOrDigit = Char::isLetterOrDigit
	internal val IsUpper = Char::isUpperCase
	internal val IsLower = Char::isLowerCase
	internal val IsDivider = { divider: Char -> { char: Char -> char == divider } }
	internal val IsUnderScore = IsDivider(underscore)
	internal val IsDash = IsDivider(dash)
	internal val IsSpace = IsDivider(space)

	internal val AnyChar = { _: Char -> true }

	internal val ToUpper = { char: Char -> arrayOf(char.toUpperCase()) }
	internal val ToLower = { char: Char -> arrayOf(char.toLowerCase()) }

	internal val ToDivider = { divider: Char -> { char: Char -> arrayOf(divider, char) } }

	internal val ToUnderScore = ToDivider(underscore)
	internal val ToSpace = ToDivider(space)
	internal val ToDash = ToDivider(dash)
	internal val AsIs = { c: Char -> arrayOf(c) }
}

enum class CaseStyle(
	private val divider: Char? = null,
	private val allLower: Boolean = false,
	private val allUpper: Boolean = false,
	private val chkStart: (Char) -> Boolean = IsLower,
	private val chkStartWord: (Char) -> Boolean = IsUpper,
	private val covStart: (Char) -> Array<Char> = ToLower,
	private val covStartWord: (Char) -> Array<Char> = ToUpper,
	private val covOther: (Char) -> Array<Char> = ToLower
) {
	Raw(
		divider = space,
		chkStart = AnyChar,
		chkStartWord = IsSpace,
		covStartWord = ToSpace,
		covOther = AsIs
	),
	RawLower(
		divider = space,
		allLower = true,
		chkStart = AnyChar,
		chkStartWord = IsSpace,
		covStartWord = ToSpace,
		covOther = ToLower
	),
	RawUpper(
		divider = space,
		allUpper = true,
		chkStart = AnyChar,
		chkStartWord = IsSpace,
		covStartWord = ToSpace,
		covOther = ToUpper
	),
	Camel(),
	Pascal(
		chkStart = IsUpper,
		covStart = ToUpper
	),
	Snake(
		divider = underscore,
		allLower = true,
		chkStartWord = IsUnderScore,
		covStartWord = ToUnderScore
	),
	SnakeUpper(
		divider = underscore,
		allUpper = true,
		chkStart = IsUpper,
		chkStartWord = IsUnderScore,
		covStartWord = ToUnderScore,
		covOther = ToUpper
	),
	Kebab(
		divider = dash,
		allLower = true,
		chkStartWord = IsDash,
		covStartWord = ToDash
	),
	KebabUpper(
		divider = dash,
		allUpper = true,
		chkStartWord = IsDash,
		chkStart = IsUpper,
		covStartWord = ToDash,
		covOther = ToUpper
	),
	;

	fun validate(source: String): Boolean {
		val src = source.trim()
		if (allLower && src.find(IsUpper) != null) return false
		if (allUpper && src.find(IsLower) != null) return false
		//not allow none Letter or Digit
		if (divider == null && !src.all(IsLetterOrDigit)) return false
		if (divider != null && (
				//not allow continues double divider
				src.indexOf(arrayOf(divider, divider).toString()) >= 0
					//not allow none letter and Digit and divider
					|| src.find { !IsLetterOrDigit(it) && it != divider } != null
				)) return false
		//so we just allow it
		return true
	}

	fun convertTo(source: String, style: CaseStyle) = run {
		val hasDivider = divider != null
		var isStart = true
		var lastDivider = false
		when {
			source.isBlank() -> null
			!validate(source) -> null
			else -> source.trim().toCharArray().joinToString("") {
				when {
					isStart -> {
						isStart = false
						style.covStart(it).toString()
					}
					hasDivider && chkStartWord(it) -> {
						lastDivider = true
						""
					}
					hasDivider && lastDivider -> style.covStartWord(it).toString()
					else -> style.covOther(it).toString()
				}
			}
		}
	}


	fun convert(source: String): String? =
		values()
			.find { it.validate(source) }
			?.convertTo(source, this)


	companion object {
		fun isCaseStyle(source: String, style: CaseStyle) =
			source.isNotBlank()
				&& style.validate(source)

		fun style(source: String) = values().find { it.validate(source) }
	}


}

fun String.toStyle(style: CaseStyle) = style.convert(this)
fun String.isStyle(style: CaseStyle) = style.validate(this)
fun String.fromStyle(style: CaseStyle, to: CaseStyle) = style.convertTo(this, to)
fun String.toCamel() = CaseStyle.Camel.convert(this)
fun String.toSnake() = CaseStyle.Snake.convert(this)
fun String.toKebab() = CaseStyle.Kebab.convert(this)
fun String.toKebabUpper() = CaseStyle.KebabUpper.convert(this)
fun String.toPascal() = CaseStyle.Pascal.convert(this)
fun String.toSnakeUpper() = CaseStyle.SnakeUpper.convert(this)
fun String.toRaw() = CaseStyle.Raw.convert(this)
fun String.toRawLower() = CaseStyle.RawLower.convert(this)
fun String.toRawUpper() = CaseStyle.RawUpper.convert(this)
