package org.evvdroid

import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/**
 * Android text turned into the bytes the language in force reads.
 *
 * Eight of the eleven modules read the Windows Western byte set and [WesternText]
 * is the whole of what they want. Three do not.
 *
 * Polish has eight letters that set has no room for, so the module declares
 * them and the engine converts its text from UTF-8 itself. Nothing else about
 * the text changes: a character the module does not declare still goes through
 * the Western list, so everything but those eight is flattened exactly as it is
 * for every other language.
 *
 * Russian is turned into phonemes by the engine itself, which the US English
 * module then speaks: the engine decides each word's stress and sounds, says
 * the numbers in Russian and the letters by their Russian names. So it wants
 * the text as it was written, in UTF-8, with the Cyrillic left standing and
 * everything else flattened as for every other language.
 *
 * Japanese has a romanizer in front of the engine and that reads Shift-JIS. The
 * code set a Japanese instance takes is fixed when the instance is made and the
 * engine refuses to move it afterwards, so Shift-JIS is what it gets.
 */
object EngineText {

	/**
	 * The bytes to hand the engine speaking [language], without a terminator:
	 * the native side adds one, and a nought anywhere inside would end the text
	 * early, so there is none in what comes back.
	 */
	fun encode(language: Int, text: String): ByteArray = when (Eci.baseLanguage(language)) {
		Eci.JAPANESE -> japanese(text)
		Eci.POLISH -> WesternText.flatten(text) { it in POLISH_LETTERS }
			.toByteArray(Charsets.UTF_8)
		Eci.RUSSIAN -> WesternText.flatten(text) { isRussianOwn(it) }
			.toByteArray(Charsets.UTF_8)
		else -> WesternText.encode(text)
	}

	/** The eight letters Polish's own alphabet has and the Western set does
	 *  not, in both cases. Its o with an acute is in the Western set already
	 *  and so is not here. */
	private val POLISH_LETTERS = "ąćęłńśźżĄĆĘŁŃŚŹŻ"

	/** What Russian text keeps for the engine to read itself: the Cyrillic
	 *  block, which takes in the Ukrainian and Belarusian letters the engine
	 *  reads as their nearest Russian, the stress mark a careful text puts over
	 *  a vowel, which the engine passes over, and the no-break spaces Russian
	 *  groups thousands with, which the engine reads as the one number they
	 *  are. */
	private fun isRussianOwn(c: Char): Boolean =
		c in '\u0400'..'\u04ff' || c == '\u0301' || c == '\u00a0' || c == '\u202f' ||
			c == '\u2009'

	private val SHIFT_JIS: Charset? = runCatching { Charset.forName("Shift_JIS") }.getOrNull()

	/**
	 * Shift-JIS, with a space for anything the set has no room for.
	 *
	 * A space rather than the encoder's own question mark, for the reason
	 * [WesternText] gives: a character that cannot be said should cost silence
	 * and not a word. Nothing here flattens the typography first, because
	 * Shift-JIS has the curly quotes, the dashes and the ellipsis that the
	 * Western set does not.
	 */
	private fun japanese(text: String): ByteArray {
		val set = SHIFT_JIS ?: return WesternText.encode(text)
		val encoder = set.newEncoder()
			.onMalformedInput(CodingErrorAction.REPLACE)
			.onUnmappableCharacter(CodingErrorAction.REPLACE)
			.replaceWith(byteArrayOf(' '.code.toByte()))
		val made = encoder.encode(CharBuffer.wrap(text.replace('\u0000', ' ')))
		val out = ByteArray(made.remaining())
		made.get(out)
		return out
	}
}
