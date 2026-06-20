package com.rustam.quizapp.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Logical sound effects the app can play. */
enum class SoundType { CORRECT, INCORRECT, COMPLETE, CLICK }

/**
 * Thin wrapper around [SoundPool] that preloads a fixed set of short effects and plays
 * them by [SoundType].
 *
 * @param context used only to load the samples; an application context is taken internally.
 * @param sounds  map of [SoundType] to a raw resource id (e.g. `R.raw.correct`). Typically
 *                built with [SoundResources.load]. Entries are preloaded on construction.
 * @param soundEnabled stream of the "sound enabled" setting; when it emits `false`,
 *                [play] becomes a no-op. The current value is mirrored into a volatile flag
 *                so [play] stays non-suspending and cheap to call from the UI thread.
 * @param scope   coroutine scope used to observe [soundEnabled]. Pass a scope whose lifetime
 *                matches this manager's (e.g. `viewModelScope`); cancelling it stops the
 *                observation. Remember to also call [release] (e.g. from `onCleared`).
 */
class SoundManager(
    context: Context,
    sounds: Map<SoundType, Int>,
    soundEnabled: Flow<Boolean>,
    scope: CoroutineScope
) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    /** [SoundType] -> SoundPool sample id of the loaded sample. */
    private val sampleIds: Map<SoundType, Int>

    @Volatile
    private var enabled: Boolean = true

    init {
        val appContext = context.applicationContext
        sampleIds = sounds.mapValues { (_, resId) ->
            soundPool.load(appContext, resId, DEFAULT_PRIORITY)
        }
        soundEnabled
            .onEach { enabled = it }
            .launchIn(scope)
    }

    /**
     * Plays the effect for [type]. Does nothing if sound is disabled, the type was not
     * preloaded, or the sample has not finished loading yet.
     */
    fun play(type: SoundType) {
        if (!enabled) return
        val sampleId = sampleIds[type] ?: return
        soundPool.play(sampleId, VOLUME, VOLUME, DEFAULT_PRIORITY, NO_LOOP, NORMAL_RATE)
    }

    /** Releases the underlying [SoundPool]. Call from `onCleared`/`onDestroy`. */
    fun release() {
        soundPool.release()
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val DEFAULT_PRIORITY = 1
        const val VOLUME = 1f
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
    }
}
