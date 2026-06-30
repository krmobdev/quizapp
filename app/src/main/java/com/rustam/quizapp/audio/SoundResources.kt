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
     *
     * NOTE: getIdentifier must use the R.java namespace ("com.rustam.quizapp"), NOT
     * context.packageName which includes the applicationIdSuffix on debug builds
     * ("com.rustam.quizapp.debug") and therefore returns 0 for every lookup.
     */
    fun load(context: Context): Map<SoundType, Int> {
        val resources = context.resources
        // Namespace matches the build.gradle namespace = "com.rustam.quizapp".
        // applicationIdSuffix on debug builds makes context.packageName differ from namespace.
        val namespace = context.packageName
            .let { pkg -> if (pkg.contains('.')) pkg.substringBeforeLast('.').let { base ->
                // If the last segment looks like a build-type suffix (debug/release/staging),
                // strip it; otherwise keep the full package name.
                val suffix = pkg.substringAfterLast('.')
                if (suffix in setOf("debug", "release", "staging", "qa")) base else pkg
            } else pkg }
        return FILE_NAMES.mapNotNull { (type, name) ->
            // Try namespace first; fall back to raw applicationId (handles edge cases).
            val resId = resources.getIdentifier(name, "raw", namespace)
                .takeIf { it != 0 }
                ?: resources.getIdentifier(name, "raw", context.packageName)
            if (resId == 0) {
                Log.w(TAG, "Missing sound: add 'res/raw/$name.ogg' to enable $type.")
                null
            } else {
                type to resId
            }
        }.toMap()
    }

    private const val TAG = "SoundResources"
}
