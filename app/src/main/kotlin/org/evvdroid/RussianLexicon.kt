package org.evvdroid

import android.content.Context
import android.util.Log
import java.io.File

/**
 * RuLex, Igor Poretsky's pronunciation dictionary for Russian, for the engine
 * to find.
 *
 * It is under the GNU GPL, so it is a file of its own rather than part of the
 * engine: the APK carries it as an asset, `rulex.dat`, and the engine reads it
 * from a path it is told. An asset is not a path, so it is copied out once, to
 * device-protected storage like everything else the engine reads at startup,
 * since a screen reader speaks before the phone is unlocked. Without it the
 * engine says Russian as it did before RuLex, from its own stress table.
 */
object RussianLexicon {

	private const val ASSET = "rulex.dat"

	@Volatile
	private var told = false

	/** Copies the lexicon out if it has changed and tells the engine where it is. */
	fun install(context: Context) {
		if (told || !EvvNative.loaded) return
		val file = File(DirectBoot.storage(context).filesDir, ASSET)
		try {
			val assets = context.applicationContext.assets
			// Stored uncompressed in the APK, so its length is known without
			// reading it, which is what says whether the copy is current.
			val length = assets.openFd(ASSET).use { it.length }
			if (!file.isFile || file.length() != length) {
				val partial = File(file.parentFile, "$ASSET.part")
				assets.open(ASSET).use { input ->
					partial.outputStream().use { input.copyTo(it) }
				}
				if (!partial.renameTo(file)) {
					file.delete()
					partial.renameTo(file)
				}
			}
		} catch (e: Exception) {
			// No asset, or no room: Russian is spoken without RuLex.
			Log.w("evvdroid", "RuLex is not available: $e")
			if (!file.isFile) return
		}
		EvvNative.setRussianLexicon(file.absolutePath)
		told = true
	}
}
