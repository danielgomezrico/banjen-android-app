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
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

const val TONE_SAMPLE_RATE = 44100

// Fade duration: 200ms/20-step ramp. Each step is 10ms, well above Android's
// ~4ms timer floor, ensuring the ramp completes reliably. 200ms covers the
// hardware cold-start transient window (50–200ms) so the tone is silent when
// the amplifier is noisy. The 200ms attack/release is imperceptible as a
// "ramp" but eliminates the CRSHSHHH noise on all tested devices.
private const val FADE_DURATION_MS = 200L
private const val FADE_STEPS = 20

// Amplitude scale: 0.7 = -3 dBFS. Leaves headroom for OEM DSP effects
// (loudness enhancers, equalizers) in the hardware audio pipeline.
internal const val AMPLITUDE_SCALE = 0.7f

// Warm-up track buffer: 4096 frames at 44100 Hz = ~93ms per write() call.
// Short enough to stay responsive to release(), long enough to avoid
// excessive CPU wake-ups.
private const val WARM_BUFFER_FRAMES = 4096

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
        samples[i] = (value * Short.MAX_VALUE * AMPLITUDE_SCALE).toInt().toShort()
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
        if (n <= 0) Double.MAX_VALUE else abs(sin(twoPiFOverFs * n))
    } ?: nominal
}

class ToneGenerator {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val trackMutex = Mutex()
    private var audioTrack: AudioTrack? = null
    private var activeJob: Job? = null

    // Silent keep-alive track — keeps the audio hardware path active so that
    // play() never triggers a cold-start transient (CRSHSHHH noise).
    // Runs at volume 0 for the lifetime of this ToneGenerator instance.
    private val warmTrack: AudioTrack = buildWarmTrack()
    private val warmJob: Job

    init {
        warmTrack.play()
        warmJob = scope.launch(Dispatchers.IO) {
            val silence = ShortArray(WARM_BUFFER_FRAMES) // zero-filled by JVM
            while (isActive) {
                val written = warmTrack.write(silence, 0, silence.size)
                if (written <= 0) break // error or track released
            }
        }
    }

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
        warmJob.cancel()
        scope.cancel()
        // Cancel the write loop before stopping/releasing to avoid
        // writing into a released track.
        warmTrack.stop()
        warmTrack.release()
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
            // 300ms drain (was 150ms). 2x safety margin over worst-case HAL buffer
            // latency. Volume is already 0 during this window so no audio is heard.
            delay(300L)
            runCatching { track.stop() }
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

    private fun buildWarmTrack(): AudioTrack {
        val bufferBytes = WARM_BUFFER_FRAMES * 2 // 16-bit = 2 bytes per frame
        val minBuf = AudioTrack.getMinBufferSize(
            TONE_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        return AudioTrack
            .Builder()
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat
                    .Builder()
                    .setSampleRate(TONE_SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(bufferBytes.coerceAtLeast(minBuf))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
            .also { it.setVolume(0f) }
    }
}
