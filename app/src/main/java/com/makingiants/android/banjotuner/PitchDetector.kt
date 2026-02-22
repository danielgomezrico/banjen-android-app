package com.makingiants.android.banjotuner

import kotlin.math.abs

class PitchDetector(
    private val sampleRate: Int = 44100,
) {
    private val yinThreshold = 0.15

    /**
     * Detects the fundamental frequency of the given audio samples using the YIN algorithm.
     * Returns the detected frequency in Hz, or -1.0 if no pitch is detected.
     */
    fun detectPitch(samples: FloatArray): Double {
        val halfLen = samples.size / 2
        val yinBuffer = FloatArray(halfLen)

        // Step 1: Difference function
        for (tau in 0 until halfLen) {
            var sum = 0.0f
            for (i in 0 until halfLen) {
                val diff = samples[i] - samples[i + tau]
                sum += diff * diff
            }
            yinBuffer[tau] = sum
        }

        // Step 2: Cumulative mean normalized difference
        yinBuffer[0] = 1.0f
        var runningSum = 0.0f
        for (tau in 1 until halfLen) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = yinBuffer[tau] * tau / runningSum
        }

        // Step 3: Absolute threshold — find first dip below threshold, then local minimum
        var tauEstimate = -1
        for (tau in 2 until halfLen) {
            if (yinBuffer[tau] < yinThreshold) {
                tauEstimate = tau
                // Walk forward to find the local minimum in this dip
                while (tauEstimate + 1 < halfLen && yinBuffer[tauEstimate + 1] < yinBuffer[tauEstimate]) {
                    tauEstimate++
                }
                break
            }
        }

        if (tauEstimate == -1) return -1.0

        // Step 4: Parabolic interpolation for sub-sample accuracy
        val refinedTau = parabolicInterpolation(yinBuffer, tauEstimate)

        // Step 5: Convert to frequency
        return sampleRate.toDouble() / refinedTau
    }

    private fun parabolicInterpolation(
        yinBuffer: FloatArray,
        tau: Int,
    ): Double {
        if (tau <= 0 || tau >= yinBuffer.size - 1) return tau.toDouble()

        val s0 = yinBuffer[tau - 1].toDouble()
        val s1 = yinBuffer[tau].toDouble()
        val s2 = yinBuffer[tau + 1].toDouble()

        val adjustment = (s2 - s0) / (2.0 * (2.0 * s1 - s2 - s0))
        return tau + adjustment
    }

    /**
     * Calculates the deviation in cents between detected and target frequencies.
     * Negative = flat, positive = sharp.
     */
    fun centsFromTarget(
        detected: Double,
        target: Double,
    ): Double {
        if (detected <= 0 || target <= 0) return 0.0
        return 1200.0 * (Math.log(detected / target) / Math.log(2.0))
    }

    /**
     * Classifies tuning status based on cent deviation.
     */
    fun classifyTuning(cents: Double): TuningStatus {
        val absCents = abs(cents)
        return when {
            absCents <= 10.0 -> TuningStatus.IN_TUNE
            absCents <= 25.0 -> TuningStatus.CLOSE
            cents > 0 -> TuningStatus.SHARP
            else -> TuningStatus.FLAT
        }
    }
}

enum class TuningStatus {
    IN_TUNE,
    CLOSE,
    SHARP,
    FLAT,
    NO_SIGNAL,
}

data class PitchResult(
    val detectedHz: Double,
    val targetHz: Double,
    val centDeviation: Double,
    val status: TuningStatus,
)

enum class BanjoString(
    val noteName: String,
    val frequencyHz: Double,
) {
    D4("D", 293.66),
    B3("B", 246.94),
    G3("G", 196.00),
    D3("D", 146.83),
}
