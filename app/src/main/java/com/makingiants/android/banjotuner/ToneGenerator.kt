package com.makingiants.android.banjotuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

const val TONE_SAMPLE_RATE = 44100

// Fade duration: 20ms/20-step ramp — imperceptible to the ear but eliminates
// waveform discontinuity clicks on start/stop. 20 steps gives a smooth ramp
// even when the Android timer batches late (~4ms floor per step).
private const val FADE_DURATION_MS = 20L
private const val FADE_STEPS = 20

fun generateSineWaveSamples(
    frequency: Float,
    sampleRate: Int,
    numSamples: Int,
): ShortArray {
    val samples = ShortArray(numSamples)
    val twoPiF = 2.0 * PI * frequency
    for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val value = sin(twoPiF * t)
        samples[i] = (value * Short.MAX_VALUE).toInt().toShort()
    }
    return samples
}

fun calculateLoopSampleCount(
    frequency: Float,
    sampleRate: Int,
): Int {
    val samplesPerCycle = sampleRate.toFloat() / frequency
    val cycles = (sampleRate / samplesPerCycle).roundToInt()
    val nominal = (cycles * samplesPerCycle).roundToInt()
    val halfPeriod = maxOf(1, (samplesPerCycle / 2).toInt())
    val twoPiFOverFs = 2.0 * PI * frequency / sampleRate
    return (nominal - halfPeriod..nominal + halfPeriod).minByOrNull { n ->
        if (n <= 0) Double.MAX_VALUE else kotlin.math.abs(sin(twoPiFOverFs * n))
    } ?: nominal
}

class ToneGenerator {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val trackMutex = Mutex()
    private var audioTrack: AudioTrack? = null
    private var activeJob: Job? = null

    val isPlaying: Boolean
        get() = audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING

    /**
     * Begin playing a tone at [frequency] Hz with a short fade-in.
     * If a tone is already playing it fades out first. Safe to call from the main thread.
     */
    fun play(frequency: Float) {
        activeJob?.cancel()
        activeJob =
            scope.launch {
                fadeOutAndRelease()
                if (!isActive) return@launch

                val loopSamples = calculateLoopSampleCount(frequency, TONE_SAMPLE_RATE)
                val samples = generateSineWaveSamples(frequency, TONE_SAMPLE_RATE, loopSamples)
                val bufferSize = loopSamples * 2
                val minBuf =
                    AudioTrack.getMinBufferSize(
                        TONE_SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                    )
                val track =
                    AudioTrack
                        .Builder()
                        .setAudioAttributes(
                            AudioAttributes
                                .Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build(),
                        ).setAudioFormat(
                            AudioFormat
                                .Builder()
                                .setSampleRate(TONE_SAMPLE_RATE)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        ).setBufferSizeInBytes(bufferSize.coerceAtLeast(minBuf))
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                track.write(samples, 0, samples.size)
                track.setLoopPoints(0, loopSamples, -1)

                if (!isActive) {
                    track.release()
                    return@launch
                }

                track.setVolume(0f)
                track.play()

                trackMutex.withLock { audioTrack = track }

                fadeIn(track)
            }
    }

    /**
     * Stop the currently playing tone with a short fade-out to avoid a click.
     * Safe to call from the main thread — returns immediately.
     */
    fun stop() {
        activeJob?.cancel()
        activeJob =
            scope.launch {
                fadeOutAndRelease()
            }
    }

    /**
     * Release all resources. Call when this ToneGenerator will no longer be used.
     */
    fun release() {
        activeJob?.cancel()
        scope.cancel()
    }

    private suspend fun fadeOutAndRelease() {
        val track =
            trackMutex.withLock {
                audioTrack.also { audioTrack = null }
            } ?: return

        withContext(NonCancellable) {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                fadeOut(track)
            }
            track.setVolume(0f)
            delay(20L)
            runCatching { track.pause() }
            track.release()
        }
    }

    // delay() is a cancellation-cooperative suspend point — it throws
    // CancellationException when the parent coroutine is cancelled, so
    // no explicit isActive check is needed inside these loops.

    private suspend fun fadeOut(track: AudioTrack) {
        val stepDelayMs = FADE_DURATION_MS / FADE_STEPS
        for (step in FADE_STEPS downTo 0) {
            track.setVolume(step.toFloat() / FADE_STEPS)
            if (step > 0) delay(stepDelayMs)
        }
    }

    private suspend fun fadeIn(track: AudioTrack) {
        val stepDelayMs = FADE_DURATION_MS / FADE_STEPS
        for (step in 0..FADE_STEPS) {
            track.setVolume(step.toFloat() / FADE_STEPS)
            if (step < FADE_STEPS) delay(stepDelayMs)
        }
    }
}
