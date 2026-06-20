package com.rustam.quizapp.audio

import android.content.Context
import android.util.Log

/**
 * Resolves the raw audio resources backing each [SoundType].
 *
 * Files live in `app/src/main/res/raw/` as OGG (Kenney Interface Sounds, CC0).
 */
object SoundResources {

    /** [SoundType] -> base file name expected under `res/raw/<name>.ogg`. */
    private val FILE_NAMES = mapOf(
        SoundType.CORRECT to "correct",
        SoundType.INCORRECT to "incorrect",
        SoundType.COMPLETE to "complete",
        SoundType.CLICK to "click"
    )

    /**
     * Returns the map of available sounds to their raw resource ids, ready to hand to
     * [SoundManager]. Files that are not present are omitted and reported via [Log.w].
     */
    fun load(context: Context): Map<SoundType, Int> {
        val resources = context.resources
        val packageName = context.packageName
        return FILE_NAMES.mapNotNull { (type, name) ->
            val resId = resources.getIdentifier(name, "raw", packageName)
            if (resId == 0) {
                Log.w(
                    TAG,
                    "Missing sound: add 'res/raw/$name.ogg' to enable $type. Skipping for now."
                )
                null
            } else {
                type to resId
            }
        }.toMap()
    }

    private const val TAG = "SoundResources"
}
