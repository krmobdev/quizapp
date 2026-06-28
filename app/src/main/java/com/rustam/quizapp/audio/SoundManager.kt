package com.rustam.quizapp.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
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
 * When OGG files are absent from `res/raw/` the manager falls back to short tones generated
 * via [ToneGenerator] so the app is never completely silent before the audio assets are added.
 * Add `res/raw/correct.ogg`, `incorrect.ogg`, `complete.ogg`, `click.ogg` (Kenney CC0) to
 * replace the fallback tones with real sounds.
 *
 * @param context      used only to load the samples; an application context is taken internally.
 * @param sounds       map of [SoundType] to a raw resource id (e.g. `R.raw.correct`). Typically
 *                     built with [SoundResources.load]. Entries are preloaded on construction.
 * @param soundEnabled stream of the "sound enabled" setting; when it emits `false`,
 *                     [play] becomes a no-op.
 * @param scope        coroutine scope used to observe [soundEnabled].
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

    /** Fallback tones for sound types that have no OGG file yet. */
    private val fallbackTones: Map<SoundType, Pair<Int, Int>> = mapOf(
        SoundType.CORRECT   to (ToneGenerator.TONE_PROP_BEEP  to 80),
        SoundType.INCORRECT to (ToneGenerator.TONE_PROP_NACK  to 120),
        SoundType.COMPLETE  to (ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 200),
        SoundType.CLICK     to (ToneGenerator.TONE_PROP_BEEP2 to 40)
    )

    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, TONE_VOLUME_PERCENT)
    } catch (e: RuntimeException) {
        null
    }

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
     * Plays the effect for [type]. Uses the preloaded OGG sample when available; falls back
     * to a short ToneGenerator tone otherwise. Does nothing when sound is disabled.
     */
    fun play(type: SoundType) {
        if (!enabled) return
        val sampleId = sampleIds[type]
        if (sampleId != null) {
            soundPool.play(sampleId, VOLUME, VOLUME, DEFAULT_PRIORITY, NO_LOOP, NORMAL_RATE)
        } else {
            val (tone, durationMs) = fallbackTones[type] ?: return
            toneGenerator?.startTone(tone, durationMs)
        }
    }

    /** Releases the underlying [SoundPool] and [ToneGenerator]. Call from `onCleared`/`onDestroy`. */
    fun release() {
        soundPool.release()
        toneGenerator?.release()
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val DEFAULT_PRIORITY = 1
        const val VOLUME = 1f
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
        const val TONE_VOLUME_PERCENT = 80
    }
}
