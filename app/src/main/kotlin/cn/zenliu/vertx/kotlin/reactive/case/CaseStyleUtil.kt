@file:JvmName("CaseStyleUtil")
package cn.zenliu.vertx.kotlin.reactive.case


interface CaseStyle {
	val allMatch: ((Char) -> Boolean)?
	val firstLetter: (Char) -> Boolean
	val checkingChar: (Char) -> Boolean
	val divider: Char?
	val charStart: (Char) -> String
	val wordBondy: (Char) -> Boolean
	val wordStart: (Char) -> String
	val wordOther: (Char) -> String

	fun validate(source: String): Boolean = source.trim()
		.takeIf { it.isNotBlank() }
		?.let {
			when {
				// first letter should be ascii
				!firstLetter(it[0]) -> null
				//if define all match
				allMatch != null && !it.filter(checkingChar).all(allMatch!!) -> null
				//now it is valid
				else -> true
			}
		} != null

	fun format(source: String, from: CaseStyle) =
		from.toWords(source)?.mapIndexed { i, w ->
			buildString {
				if (i == 0) {
					append(charStart(w[0]))
					append(w.subSequence(1 until w.length).map(wordOther))
				} else {
					append(wordStart(w[0]))
					append(w.subSequence(1 until w.length).map(wordOther))
				}
			}
		}?.joinToString("")


	fun toWords(source: String): List<String>? =
		source.takeIf { validate(it) }?.trim()?.let {
			when {
				//have divider
				divider != null -> it.split(divider!!)
				//just check word bondy
				else -> it.fold(mutableListOf<MutableList<Char>>())
				{ acc, c ->
					if (wordBondy(c)) {
						acc.add(mutableListOf(c))
					} else {
						acc.last().add(c)
					}
					acc
				}.map { t -> t.toCharArray().toString() }
			}
		}
}

private const val space = ' '
private const val dash = '-'
private const val underscore = '_'
private const val CASE_MASK = 0x20
private val isAsciiLower = { c: Char -> c in 'a'..'z' }
private val isAsciiUpper = { c: Char -> c in 'A'..'Z' }
private val isAsciiLetter = { c: Char -> c in 'A'..'Z' || c in 'a'..'z' }
private val toAsciiLower = { c: Char -> if (isAsciiLower(c)) c else c.toInt().xor(CASE_MASK).toChar() }
private val toAsciiUpper = { c: Char -> if (isAsciiUpper(c)) c else c.toInt().xor(CASE_MASK).toChar() }
private fun checkEq(c: Char) = { c1: Char -> c1 == c }
private fun checkIn(c: Char, c2: Char) = { c1: Char -> c1 in c..c2 }
private val checkAny = { c: Char -> true }
private val asIs = { c: Char -> c.toString() }
private val asWith = { pre: Char, case: (Char) -> String -> { c: Char -> pre + case(c) } }
private val asAsciiUpper = { c: Char -> (c.takeIf(isAsciiLower)?.let(toAsciiLower) ?: c).toString() }
private val asAsciiLower = { c: Char -> (c.takeIf(isAsciiUpper)?.let(toAsciiUpper) ?: c).toString() }
private val asUpper = { c: Char -> c.toUpperCase().toString() }
private val asLower = { c: Char -> c.toLowerCase().toString() }
private val isUpper = Char::isUpperCase
private val isLower = Char::isLowerCase

enum class AsciiStyle(
	//default is Pascal
	override val allMatch: ((Char) -> Boolean)? = null,
	override val firstLetter: (Char) -> Boolean = isAsciiUpper,
	override val checkingChar: (Char) -> Boolean = isAsciiLetter,
	override val divider: Char? = null,
	override val charStart: (Char) -> String = asAsciiUpper,
	bondyChecker: (Char) -> Boolean = firstLetter,
	override val wordStart: (Char) -> String = charStart,
	override val wordOther: (Char) -> String = asAsciiLower
) : CaseStyle {
	Pascal(),
	Camel(
		firstLetter = isAsciiLower
	),
	Snake(
		allMatch = isAsciiLower,
		firstLetter = isAsciiLower,
		divider = underscore,
		wordStart = asWith(underscore, asAsciiLower),
		wordOther = asAsciiLower
	),
	SnakeUpper(
		allMatch = isAsciiUpper,
		firstLetter = isAsciiUpper,
		divider = underscore,
		wordStart = asWith(underscore, asAsciiUpper),
		wordOther = asAsciiUpper
	),
	Kebab(
		allMatch = isAsciiLower,
		firstLetter = isAsciiLower,
		divider = dash,
		wordStart = asWith(dash, asAsciiLower),
		wordOther = asAsciiLower
	),
	KebabUpper(
		allMatch = isAsciiUpper,
		firstLetter = isAsciiUpper,
		divider = dash,
		wordStart = asWith(dash, asAsciiUpper),
		wordOther = asAsciiUpper
	),
	RawLower(
		allMatch = isAsciiLower,
		firstLetter = isAsciiLower,
		divider = space,
		wordStart = asWith(space, asAsciiLower),
		wordOther = asAsciiLower
	),
	RawUpper(
		allMatch = isAsciiUpper,
		firstLetter = isAsciiUpper,
		divider = space,
		wordStart = asWith(space, asAsciiUpper),
		wordOther = asAsciiUpper
	),
	Raw(
		divider = space,
		wordStart = asWith(space, asIs),
		wordOther = asIs
	),
	;

	override val wordBondy: (Char) -> Boolean = if (divider != null) checkEq(divider!!) else bondyChecker
}

enum class UnicodeStyle(
	//default is Pascal
	override val allMatch: ((Char) -> Boolean)? = null,
	override val firstLetter: (Char) -> Boolean = isUpper,
	override val checkingChar: (Char) -> Boolean = checkAny,
	override val divider: Char? = null,
	override val charStart: (Char) -> String = asUpper,
	bondyChecker: (Char) -> Boolean = firstLetter,
	override val wordStart: (Char) -> String = charStart,
	override val wordOther: (Char) -> String = asLower
) : CaseStyle {
	Pascal(),
	Camel(
		firstLetter = isLower
	),
	Snake(
		allMatch = isLower,
		firstLetter = isLower,
		divider = underscore,
		wordStart = asWith(underscore, asLower),
		wordOther = asLower
	),
	SnakeUpper(
		allMatch = isUpper,
		firstLetter = isUpper,
		divider = underscore,
		wordStart = asWith(underscore, asUpper),
		wordOther = asUpper
	),
	Kebab(
		allMatch = isLower,
		firstLetter = isLower,
		divider = dash,
		wordStart = asWith(dash, asLower),
		wordOther = asLower
	),
	KebabUpper(
		allMatch = isUpper,
		firstLetter = isUpper,
		divider = dash,
		wordStart = asWith(dash, asUpper),
		wordOther = asUpper
	),
	RawLower(
		allMatch = isLower,
		firstLetter = isLower,
		divider = space,
		wordStart = asWith(space, asLower),
		wordOther = asLower
	),
	RawUpper(
		allMatch = isUpper,
		firstLetter = isUpper,
		divider = space,
		wordStart = asWith(space, asUpper),
		wordOther = asUpper
	),
	Raw(
		divider = space,
		wordStart = asWith(space, asIs),
		wordOther = asIs
	),
	;

	override val wordBondy: (Char) -> Boolean = if (divider != null) checkEq(divider!!) else bondyChecker
}
