package com.makingiants.android.banjotuner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.abs

enum class TuningAnimationState {
    IDLE,
    STRING_SELECTED,
    NO_SIGNAL,
    FLAT,
    SHARP,
    CLOSE,
    IN_TUNE,
}

object TuningAnimationConstants {
    // Timing
    const val IDLE_BREATHE_DURATION_MS = 2000
    const val SELECTED_PULSE_DURATION_MS = 1500
    const val IN_TUNE_REST_DURATION_MS = 3000
    const val CELEBRATION_HOLD_MS = 200
    const val CELEBRATION_EXPAND_MS = 400
    const val CELEBRATION_SETTLE_MS = 300
    const val STATE_TRANSITION_MS = 300
    const val CROSS_FADE_MS = 300

    // Scale
    const val IDLE_SCALE_MIN = 0.97f
    const val IDLE_SCALE_MAX = 1.03f
    const val SELECTED_SCALE_MIN = 1.08f
    const val SELECTED_SCALE_MAX = 1.12f
    const val CLOSE_BEAT_SCALE_MIN = 0.96f
    const val CLOSE_BEAT_SCALE_MAX = 1.06f
    const val IN_TUNE_EXPAND_SCALE = 1.15f
    const val IN_TUNE_REST_SCALE_MIN = 1.07f
    const val IN_TUNE_REST_SCALE_MAX = 1.09f

    // Ring sizes (fraction of composable min dimension / 2)
    const val RING_OUTER_FRACTION = 0.80f
    const val RING_MIDDLE_FRACTION = 0.60f
    const val RING_INNER_FRACTION = 0.40f

    // Stroke widths
    val RING_OUTER_STROKE = 2.dp
    val RING_MIDDLE_STROKE = 2.5.dp
    val RING_INNER_STROKE = 3.dp

    // Center dot (diameters in dp)
    val CENTER_DOT_IDLE = 8.dp
    val CENTER_DOT_SELECTED = 12.dp
    val CENTER_DOT_IN_TUNE = 16.dp
    val CENTER_DOT_REST = 14.dp

    // Animation area
    val ANIMATION_HEIGHT = 200.dp

    // Beat frequency
    const val BEAT_HZ_MIN = 0.5f
    const val BEAT_HZ_MAX = 6f
    const val BEAT_CENTS_DIVISOR = 5f

    // Asymmetry
    const val MAX_ASYMMETRY_FRACTION = 0.15f
    const val WOBBLE_AMPLITUDE_DP = 3f
    const val WOBBLE_FREQUENCY_HZ = 3f
}

// Per-string accent colors (indexed by string position 0–3)
internal val stringAccentColors = listOf(
    Color(0xFFA67B5B), // Index 0: D3 — Deep Copper
    Color(0xFF6D94A1), // Index 1: G3 — Muted Teal
    Color(0xFFC4915A), // Index 2: B3 — Soft Amber
    Color(0xFFD4A84B), // Index 3: D4 — Warm Gold
)
internal val neutralColor = Color(0xFFF5E6D3)
internal val coolShiftColor = Color(0xFF8BA6B3)
internal val warmGoldColor = Color(0xFFD4A84B)

internal fun deriveTuningAnimationState(
    selectedOption: Int,
    pitchCheckMode: Boolean,
    pitchResult: PitchResult?,
): TuningAnimationState {
    if (selectedOption < 0) return TuningAnimationState.IDLE
    if (!pitchCheckMode) return TuningAnimationState.STRING_SELECTED
    val result = pitchResult ?: return TuningAnimationState.NO_SIGNAL
    return when (result.status) {
        TuningStatus.NO_SIGNAL -> TuningAnimationState.NO_SIGNAL
        TuningStatus.FLAT -> TuningAnimationState.FLAT
        TuningStatus.SHARP -> TuningAnimationState.SHARP
        TuningStatus.CLOSE -> TuningAnimationState.CLOSE
        TuningStatus.IN_TUNE -> TuningAnimationState.IN_TUNE
    }
}

internal fun beatFrequencyHz(centDeviation: Float): Float {
    val absCents = abs(centDeviation)
    return (absCents / TuningAnimationConstants.BEAT_CENTS_DIVISOR)
        .coerceIn(TuningAnimationConstants.BEAT_HZ_MIN, TuningAnimationConstants.BEAT_HZ_MAX)
}

internal fun ringAsymmetryOffset(centDeviation: Float): Float {
    return (centDeviation / 50f).coerceIn(
        -TuningAnimationConstants.MAX_ASYMMETRY_FRACTION,
        TuningAnimationConstants.MAX_ASYMMETRY_FRACTION,
    )
}

/**
 * Tuning animation wrapper. Tries to render via Rive state machine;
 * falls back to a minimal Canvas animation if the .riv resource is
 * missing or fails to load. Preserves the same public API so
 * EarActivity.kt requires no changes.
 */
@Composable
fun TuningAnimation(
    selectedOption: State<Int>,
    pitchCheckMode: State<Boolean>,
    pitchResult: State<PitchResult?>,
    isVolumeLow: State<Boolean>,
    modifier: Modifier = Modifier,
) {
    val animState = deriveTuningAnimationState(
        selectedOption.value,
        pitchCheckMode.value,
        pitchResult.value,
    )

    val context = LocalContext.current
    val riveResId = remember { resolveRiveResource(context) }
    var riveLoadFailed by remember { mutableStateOf(false) }

    if (riveResId != 0 && !riveLoadFailed) {
        RiveTuningAnimation(
            riveResId = riveResId,
            animState = animState,
            selectedString = selectedOption.value,
            centDeviation = pitchResult.value?.centDeviation?.toFloat() ?: 0f,
            isVolumeLow = isVolumeLow.value,
            onLoadFailed = { riveLoadFailed = true },
            modifier = modifier,
        )
    } else {
        val baseAccent = if (selectedOption.value in stringAccentColors.indices) {
            stringAccentColors[selectedOption.value]
        } else {
            neutralColor
        }
        CanvasFallbackAnimation(ringColor = baseAccent, modifier = modifier)
    }
}

/**
 * Minimal Canvas fallback: 3 concentric rings + center dot with idle
 * breathing animation. Renders immediately (no async loading) and provides
 * a graceful degradation when the Rive .riv file is unavailable.
 */
@Composable
internal fun CanvasFallbackAnimation(
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(
        targetValue = ringColor,
        animationSpec = tween(TuningAnimationConstants.CROSS_FADE_MS),
        label = "fallback color",
    )

    val breatheTransition = rememberInfiniteTransition(label = "fallback breathe")
    val breatheScale by breatheTransition.animateFloat(
        initialValue = TuningAnimationConstants.IDLE_SCALE_MIN,
        targetValue = TuningAnimationConstants.IDLE_SCALE_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = TuningAnimationConstants.IDLE_BREATHE_DURATION_MS / 2,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fallback breathe scale",
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = size.minDimension / 2f

        // Background glow
        drawCircle(
            color = animatedColor,
            radius = baseRadius * TuningAnimationConstants.RING_OUTER_FRACTION * breatheScale,
            center = center,
            alpha = 0.04f,
        )

        // Outer ring
        drawCircle(
            color = animatedColor,
            radius = baseRadius * TuningAnimationConstants.RING_OUTER_FRACTION * breatheScale,
            center = center,
            alpha = 0.2f,
            style = Stroke(width = TuningAnimationConstants.RING_OUTER_STROKE.toPx()),
        )

        // Middle ring
        drawCircle(
            color = animatedColor,
            radius = baseRadius * TuningAnimationConstants.RING_MIDDLE_FRACTION * breatheScale,
            center = center,
            alpha = 0.25f,
            style = Stroke(width = TuningAnimationConstants.RING_MIDDLE_STROKE.toPx()),
        )

        // Inner ring
        drawCircle(
            color = animatedColor,
            radius = baseRadius * TuningAnimationConstants.RING_INNER_FRACTION * breatheScale,
            center = center,
            alpha = 0.3f,
            style = Stroke(width = TuningAnimationConstants.RING_INNER_STROKE.toPx()),
        )

        // Center dot
        drawCircle(
            color = animatedColor,
            radius = 5.dp.toPx(),
            center = center,
            alpha = 0.4f,
        )
    }
}
