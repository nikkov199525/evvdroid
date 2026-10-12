package org.evvdroid

import org.junit.Assert.assertEquals
import org.junit.Test

class PunctuationTest {

	private fun quiet(text: String) = Punctuation.quiet(text).trim().replace(Regex("""\s+"""), " ")

	@Test
	fun marksWithNoWordBeforeThemAreSilent() {
		assertEquals("", quiet(",,,"))
		assertEquals("", quiet("…,\"\""))
		assertEquals("", quiet("@#$%"))
		assertEquals("ёлка", quiet("«ёлка»"))
	}

	@Test
	fun aRunAfterAWordBecomesOnePause() {
		assertEquals("Слово, слово", quiet("Слово ,,, слово"))
		assertEquals("Привет, сказал он.", quiet("\"Привет\" -- сказал он."))
		assertEquals("Что?", quiet("Что?!.."))
		assertEquals("Да,", quiet("— Да…"))
		assertEquals("он сказал.", quiet("он сказал.\""))
	}

	@Test
	fun marksInsideWordsAndNumbersStay() {
		assertEquals("3.5 и 10:30, 1,5 т.е. 1-го", quiet("3.5 и 10:30, 1,5 т.е. 1-го"))
		assertEquals("don't quoted", quiet("don't 'quoted'"))
		assertEquals("50%", quiet("50%"))
	}

	@Test
	fun anAccentWrittenAsACombiningMarkStaysOnItsLetter() {
		assertEquals("Capek and ü", quiet("Capek and ü"))
		assertEquals("й", quiet("й"))
	}

	@Test
	fun nothingIsAddedAtTheEnd() {
		assertEquals("Hello.", Punctuation.quiet("Hello."))
		assertEquals("Hello.", Punctuation.quiet("Hello.\""))
		assertEquals("a b", Punctuation.quiet("a (b"))
	}
}
