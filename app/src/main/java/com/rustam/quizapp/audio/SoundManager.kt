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
 * Uses [AudioAttributes.USAGE_GAME] so sounds play through the media/game stream and are
 * NOT silenced by "Priority only" Do-Not-Disturb mode. Volume follows the device media
 * volume slider automatically.
 *
 * When OGG files are absent from `res/raw/` the manager falls back to short tones generated
 * via [ToneGenerator].
 */
class SoundManager(
    context: Context,
    sounds: Map<SoundType, Int>,
    soundEnabled: Flow<Boolean>,
    scope: CoroutineScope
) {

    private val appContext = context.applicationContext
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // USAGE_GAME routes to STREAM_MUSIC; not silenced by priority-only DND.
                .setUsage(AudioAttributes.USAGE_GAME)
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
        sampleIds = sounds.mapValues { (_, resId) ->
            soundPool.load(appContext, resId, DEFAULT_PRIORITY)
        }
        soundEnabled
            .onEach { enabled = it }
            .launchIn(scope)
    }

    /**
     * Plays the effect for [type]. Volume is taken from the device's media stream in real time
     * so the hardware volume buttons are respected. Does nothing when sound is disabled or
     * device volume is zero.
     */
    fun play(type: SoundType) {
        if (!enabled) return
        val vol = mediaVolume()
        if (vol == 0f) return
        val sampleId = sampleIds[type]
        if (sampleId != null) {
            soundPool.play(sampleId, vol, vol, DEFAULT_PRIORITY, NO_LOOP, NORMAL_RATE)
        } else {
            val (tone, durationMs) = fallbackTones[type] ?: return
            toneGenerator?.startTone(tone, durationMs)
        }
    }

    /** Normalised [0, 1] current media stream volume. */
    private fun mediaVolume(): Float {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return 1f
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max.toFloat()
    }

    /** Releases the underlying [SoundPool] and [ToneGenerator]. Call from `onCleared`/`onDestroy`. */
    fun release() {
        soundPool.release()
        toneGenerator?.release()
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val DEFAULT_PRIORITY = 1
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f
        const val TONE_VOLUME_PERCENT = 80
    }
}
