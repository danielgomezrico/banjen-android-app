package com.makingiants.android.banjotuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

const val TONE_SAMPLE_RATE = 44100

fun generateSineWaveSamples(frequency: Float, sampleRate: Int, numSamples: Int): ShortArray {
    val samples = ShortArray(numSamples)
    val twoPiF = 2.0 * PI * frequency
    for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val value = sin(twoPiF * t)
        samples[i] = (value * Short.MAX_VALUE).toInt().toShort()
    }
    return samples
}

fun calculateLoopSampleCount(frequency: Float, sampleRate: Int): Int {
    val samplesPerCycle = sampleRate.toFloat() / frequency
    val cycles = (sampleRate / samplesPerCycle).roundToInt()
    return (cycles * samplesPerCycle).roundToInt()
}

class ToneGenerator {
    private var audioTrack: AudioTrack? = null
    val isPlaying: Boolean get() = audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING

    fun play(frequency: Float) {
        stop()

        val loopSamples = calculateLoopSampleCount(frequency, TONE_SAMPLE_RATE)
        val samples = generateSineWaveSamples(frequency, TONE_SAMPLE_RATE, loopSamples)
        val bufferSize = loopSamples * 2 // 16-bit = 2 bytes per sample

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(TONE_SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize.coerceAtLeast(AudioTrack.getMinBufferSize(
                TONE_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.setLoopPoints(0, loopSamples, -1)
        track.play()

        audioTrack = track
    }

    fun stop() {
        audioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                pause()
                flush()
            }
            release()
        }
        audioTrack = null
    }
}
