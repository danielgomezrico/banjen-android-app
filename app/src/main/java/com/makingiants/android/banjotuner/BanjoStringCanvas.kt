package com.makingiants.android.banjotuner

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlinx.coroutines.launch

// --- Color Palette ---

private val bgGradientStops = listOf(
    0.00f to Color(0xFF1A1210),
    0.35f to Color(0xFF231A15),
    0.65f to Color(0xFF1E1512),
    1.00f to Color(0xFF141010),
)

private data class StringColors(
    val idle: Color,
    val active: Color,
    val glow: Color,
    val label: Color,
)

private val stringPalette = listOf(
    StringColors(Color(0xFF8C7161), Color(0xFFD4956A), Color(0xFFD4956A), Color(0xFFB89A86)), // D3
    StringColors(Color(0xFF6B8490), Color(0xFF5AAFCB), Color(0xFF5AAFCB), Color(0xFF8EADB8)), // G3
    StringColors(Color(0xFF8C8062), Color(0xFFCBA55A), Color(0xFFCBA55A), Color(0xFFB8A882)), // B3
    StringColors(Color(0xFF907466), Color(0xFFE07850), Color(0xFFE07850), Color(0xFFC09A8A)), // D4
)

private val nutBridgeColor = Color(0xFF3D322A)
private val nutBridgeHighlight = Color(0xFF5C4A3E)
private val fretColor = Color(0xFF2E2420)

// --- String Config ---

private val stringLabels = listOf("D3", "G3", "B3", "D4")
private val stringOrdinals = listOf("4th", "3rd", "2nd", "1st")
private val idleThicknessDp = floatArrayOf(4.0f, 3.2f, 2.6f, 2.0f)
private val activeThicknessDp = floatArrayOf(5.5f, 4.5f, 3.6f, 2.8f)

// Vibration parameters per string
private val vibrationAmplitudeDp = floatArrayOf(8.0f, 6.5f, 5.0f, 4.0f)
private val vibrationFrequencyHz = floatArrayOf(3.0f, 4.0f, 5.5f, 6.0f)
private val vibrationWavePeaks = floatArrayOf(2.5f, 3.0f, 3.5f, 4.0f)

private val springStiffness = floatArrayOf(300f, 350f, 400f, 500f)      // D3→D4
private val springDamping = floatArrayOf(0.50f, 0.55f, 0.60f, 0.65f)
private val releaseDurationMs = intArrayOf(400, 350, 300, 220)          // D3→D4
private val initialSharpness = floatArrayOf(0.7f, 0.8f, 1.0f, 1.2f)   // D3→D4
private val sharpnessTransMs = intArrayOf(250, 200, 160, 120)

private val fretPositions = floatArrayOf(0.18f, 0.33f, 0.45f, 0.55f, 0.65f)

private const val NUM_STRINGS = 4
private const val BREATHING_PERIOD_MS = 2400
private const val BREATHING_OPACITY_MIN = 0.70f
private const val BREATHING_OPACITY_MAX = 1.0f
private const val DIMMED_OPACITY = 0.35f
private const val BLUR_WIDTH_RATIO = 2.0f
private const val HAZE_WIDTH_RATIO = 3.5f
private val breathingAmplitude = floatArrayOf(0.09f, 0.08f, 0.05f, 0.04f)  // D3→D4
private val breathingPeriodS = floatArrayOf(2.8f, 2.4f, 3.2f, 1.8f)        // incommensurate
private const val NUT_BRIDGE_HEIGHT_DP = 12f
private const val SAFE_PADDING_DP = 16f

@Composable
fun BanjoStringCanvas(
    selectedString: Int,
    onStringSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "strings-breathe")

    // Breathing animation (opacity)
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = BREATHING_OPACITY_MIN,
        targetValue = BREATHING_OPACITY_MAX,
        animationSpec = infiniteRepeatable(
            animation = tween(BREATHING_PERIOD_MS / 2, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe-alpha",
    )

    // Idle shimmer phase
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-phase",
    )

    // Per-string vibration amplitude animatable
    val vibrationAmplitudes = remember {
        Array(NUM_STRINGS) { Animatable(0f) }
    }

    // Per-string color lerp factor (0 = idle, 1 = active)
    val colorFactors = remember {
        Array(NUM_STRINGS) { Animatable(0f) }
    }

    // Per-string opacity (1.0 = full, 0.35 = dimmed)
    val stringOpacities = remember {
        Array(NUM_STRINGS) { Animatable(1f) }
    }

    // Per-string wave sharpness (3.0=pure sine, ~0.7=angular at D3 pluck)
    val waveSharpness = remember { Array(NUM_STRINGS) { Animatable(3.0f) } }

    // Tap zone: 0=nut, 1=center, 2=bridge (set during tap, used in sharpness offset)
    val tapZone = remember { mutableIntStateOf(1) }

    // Per-string attack progress (0=biased envelope, 1=symmetric)
    val attackProgress = remember { Array(NUM_STRINGS) { Animatable(1f) } }

    // Per-string last tap Y normalized (0=nut, 1=bridge)
    val touchYNorms = remember { FloatArray(NUM_STRINGS) { 0.5f } }

    // String geometry refs for pointerInput scope (updated each draw frame)
    val stringTopPx = remember { mutableFloatStateOf(0f) }
    val stringBottomPx = remember { mutableFloatStateOf(0f) }

    // Continuous phase for sine wave animation
    var wavePhase by remember { mutableFloatStateOf(0f) }

    // Advance wave phase continuously using frame callbacks
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            kotlinx.coroutines.delay(16L) // ~60fps
            val now = System.nanoTime()
            if (lastNanos > 0L) {
                val dtSeconds = (now - lastNanos) / 1_000_000_000f
                wavePhase += dtSeconds
            }
            lastNanos = now
        }
    }

    // React to selectedString changes: animate amplitudes, colors, opacities
    for (i in 0 until NUM_STRINGS) {
        LaunchedEffect(selectedString, i) {
            if (selectedString == i) {
                // This string is now active — spring onset (parallel with color + sharpness)
                launch { vibrationAmplitudes[i].animateTo(1f, spring(stiffness = springStiffness[i], dampingRatio = springDamping[i])) }
                val sharpnessOffset = when (tapZone.intValue) { 0 -> 0.05f; 2 -> 0.10f; else -> 0f }
                launch {
                    waveSharpness[i].snapTo(initialSharpness[i] + sharpnessOffset)
                    waveSharpness[i].animateTo(3.0f, tween(sharpnessTransMs[i], easing = EaseOutCubic))
                }
                launch { attackProgress[i].snapTo(0f); attackProgress[i].animateTo(1f, tween(300, easing = EaseOutCubic)) }
                colorFactors[i].animateTo(1f, tween(200, easing = EaseOutCubic))
            } else if (selectedString >= 0) {
                // Another string is active — decay and dim
                val easing = if (i < 2) EaseOutCubic else EaseOutQuad
                vibrationAmplitudes[i].animateTo(0f, tween(releaseDurationMs[i], easing = easing))
                colorFactors[i].animateTo(0f, tween(350, easing = EaseOutCubic))
                stringOpacities[i].animateTo(DIMMED_OPACITY, tween(300, easing = EaseOutCubic))
            } else {
                // No string selected — return to idle
                val easing = if (i < 2) EaseOutCubic else EaseOutQuad
                vibrationAmplitudes[i].animateTo(0f, tween(releaseDurationMs[i], easing = easing))
                colorFactors[i].animateTo(0f, tween(350, easing = EaseOutCubic))
                stringOpacities[i].animateTo(1f, tween(300, easing = EaseOutCubic))
            }
        }

        // Keep active string at full opacity
        LaunchedEffect(selectedString, i) {
            if (selectedString == i) {
                stringOpacities[i].animateTo(1f, tween(200, easing = EaseOutCubic))
            }
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(selectedString) {
                detectTapGestures { offset ->
                    val hPad = SAFE_PADDING_DP * density
                    val availableWidth = size.width - 2 * hPad
                    val bandWidth = availableWidth / NUM_STRINGS
                    val relX = offset.x - hPad
                    if (relX < 0 || relX > availableWidth) return@detectTapGestures
                    val tappedIndex = (relX / bandWidth).toInt().coerceIn(0, NUM_STRINGS - 1)

                    // Precise Y normalization using string geometry refs
                    val sTop = stringTopPx.floatValue
                    val sBottom = stringBottomPx.floatValue
                    val sLength = sBottom - sTop
                    val yNorm = if (sLength > 0f) {
                        ((offset.y.coerceIn(sTop, sBottom) - sTop) / sLength).coerceIn(0.05f, 0.95f)
                    } else 0.5f
                    touchYNorms[tappedIndex] = yNorm
                    tapZone.intValue = when { yNorm < 0.3f -> 0; yNorm > 0.7f -> 2; else -> 1 }

                    if (tappedIndex == selectedString) {
                        onStringSelected(-1)
                    } else {
                        onStringSelected(tappedIndex)
                    }
                }
            },
    ) {
        val w = size.width
        val h = size.height
        val density = this.density
        val hPad = SAFE_PADDING_DP * density
        val vPad = SAFE_PADDING_DP * density
        val nutH = NUT_BRIDGE_HEIGHT_DP * density
        val bridgeH = NUT_BRIDGE_HEIGHT_DP * density

        // --- Background gradient ---
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = bgGradientStops.toTypedArray(),
            ),
        )

        // --- Vignette ---
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.40f)),
                center = Offset(w / 2, h / 2),
                radius = maxOf(w, h) * 0.85f,
            ),
        )

        // --- Nut and Bridge ---
        val nutY = vPad
        val bridgeY = h - vPad - bridgeH
        val barLeft = hPad
        val barWidth = w - 2 * hPad
        val cornerRadius = 2f * density

        // Nut
        drawRoundRect(
            color = nutBridgeColor,
            topLeft = Offset(barLeft, nutY),
            size = Size(barWidth, nutH),
            cornerRadius = CornerRadius(cornerRadius),
        )
        drawRect(
            color = nutBridgeHighlight.copy(alpha = 0.60f),
            topLeft = Offset(barLeft, nutY),
            size = Size(barWidth, 1f * density),
        )

        // Bridge
        drawRoundRect(
            color = nutBridgeColor,
            topLeft = Offset(barLeft, bridgeY),
            size = Size(barWidth, bridgeH),
            cornerRadius = CornerRadius(cornerRadius),
        )
        drawRect(
            color = nutBridgeHighlight.copy(alpha = 0.60f),
            topLeft = Offset(barLeft, bridgeY),
            size = Size(barWidth, 1f * density),
        )

        // --- Fret lines ---
        val stringTop = nutY + nutH
        val stringBottom = bridgeY
        val stringLength = stringBottom - stringTop

        // Update refs for pointerInput scope
        stringTopPx.floatValue = stringTop
        stringBottomPx.floatValue = stringBottom

        for (fretPos in fretPositions) {
            val fretY = stringTop + stringLength * fretPos
            drawRect(
                color = fretColor.copy(alpha = 0.35f),
                topLeft = Offset(barLeft, fretY),
                size = Size(barWidth, 1f * density),
            )
        }

        // --- Strings ---
        val availableWidth = w - 2 * hPad
        val bandWidth = availableWidth / NUM_STRINGS

        for (i in 0 until NUM_STRINGS) {
            val centerX = hPad + bandWidth * (i + 0.5f)
            val palette = stringPalette[i]
            val vibAmp = vibrationAmplitudes[i].value
            val colorFactor = colorFactors[i].value
            val opacity = stringOpacities[i].value

            // Lerp between idle and active colors
            val stringColor = lerpColor(palette.idle, palette.active, colorFactor)
            val glowColor = palette.glow

            // Determine effective alpha (breathing for idle, full for active)
            val effectiveAlpha = if (vibAmp > 0.01f) {
                opacity
            } else {
                opacity * breatheAlpha
            }

            // String thickness
            val idleThick = idleThicknessDp[i] * density
            val activeThick = activeThicknessDp[i] * density
            val currentThick = idleThick + (activeThick - idleThick) * colorFactor

            // Amplitude in pixels
            val maxAmplitudePx = vibrationAmplitudeDp[i] * density * vibAmp
            val shimmerAmpPx = 0.3f * density

            // Breathing width factor during sustain
            val isActive = vibAmp > 0.01f
            val breathFactor = if (isActive) {
                1f + breathingAmplitude[i] * sin(2f * PI.toFloat() * wavePhase / breathingPeriodS[i])
            } else 1f
            val glowAlpha = if (isActive) 0.45f else 0.20f

            val coreWidth = currentThick * breathFactor
            val blurWidth = coreWidth * BLUR_WIDTH_RATIO
            val hazeWidth = coreWidth * HAZE_WIDTH_RATIO

            // Layer 1: haze (outermost, faintest)
            drawStringPath(
                centerX = centerX,
                stringTop = stringTop,
                stringLength = stringLength,
                amplitude = maxAmplitudePx,
                shimmerAmp = if (!isActive) shimmerAmpPx else 0f,
                shimmerPhase = shimmerPhase,
                wavePhase = wavePhase,
                wavePeaks = vibrationWavePeaks[i],
                waveFreqHz = vibrationFrequencyHz[i],
                color = glowColor,
                alpha = glowAlpha * 0.15f * effectiveAlpha,
                strokeWidth = hazeWidth,
            )
            // Layer 2: blur
            drawStringPath(
                centerX = centerX,
                stringTop = stringTop,
                stringLength = stringLength,
                amplitude = maxAmplitudePx,
                shimmerAmp = if (!isActive) shimmerAmpPx else 0f,
                shimmerPhase = shimmerPhase,
                wavePhase = wavePhase,
                wavePeaks = vibrationWavePeaks[i],
                waveFreqHz = vibrationFrequencyHz[i],
                color = glowColor,
                alpha = glowAlpha * 0.40f * effectiveAlpha,
                strokeWidth = blurWidth,
            )

            // Layer 3: core (main string)
            drawStringPath(
                centerX = centerX,
                stringTop = stringTop,
                stringLength = stringLength,
                amplitude = maxAmplitudePx,
                shimmerAmp = if (!isActive) shimmerAmpPx else 0f,
                shimmerPhase = shimmerPhase,
                wavePhase = wavePhase,
                wavePeaks = vibrationWavePeaks[i],
                waveFreqHz = vibrationFrequencyHz[i],
                color = stringColor,
                alpha = effectiveAlpha,
                strokeWidth = coreWidth,
                sharpness = waveSharpness[i].value,
                touchYNorm = touchYNorms[i],
                attackProgress = attackProgress[i].value,
                isWound = i < 2,
            )

            // --- Labels ---
            val labelColor = palette.label
            val labelAlpha = if (isActive) 1f else effectiveAlpha * 0.65f
            val labelY = bridgeY - 76f * density

            drawStringLabel(
                textMeasurer = textMeasurer,
                primary = stringLabels[i],
                secondary = stringOrdinals[i],
                centerX = centerX,
                primaryY = labelY,
                color = labelColor,
                alpha = labelAlpha,
                density = density,
                fontScale = colorFactor,
            )
        }
    }
}

private fun DrawScope.drawStringPath(
    centerX: Float,
    stringTop: Float,
    stringLength: Float,
    amplitude: Float,
    shimmerAmp: Float,
    shimmerPhase: Float,
    wavePhase: Float,
    wavePeaks: Float,
    waveFreqHz: Float,
    color: Color,
    alpha: Float,
    strokeWidth: Float,
    sharpness: Float = 3.0f,
    touchYNorm: Float = 0.5f,
    attackProgress: Float = 1.0f,
    isWound: Boolean = false,
) {
    if (alpha <= 0.001f) return

    val path = Path()
    val segments = 80
    val segmentHeight = stringLength / segments

    for (s in 0..segments) {
        val y = stringTop + s * segmentHeight
        val yNorm = if (stringLength > 0) (s.toFloat() / segments) else 0f

        // Amplitude envelope with gaussian bias during attack
        val symmetricEnv = sin(PI * yNorm).toFloat()
        val gaussianBias = exp((-((yNorm - touchYNorm) * (yNorm - touchYNorm)) / (2f * 0.35f * 0.35f)).toDouble()).toFloat()
        val biasedEnv = (symmetricEnv * gaussianBias).coerceAtLeast(0f)
        val envelope = symmetricEnv * attackProgress + biasedEnv * (1f - attackProgress)

        // Sine wave vibration with sharpness shaping
        val vibrationOffset = if (amplitude > 0.01f) {
            val rawSine = sin(2.0 * PI * wavePeaks * yNorm + wavePhase * waveFreqHz * 2.0 * PI)
            val shapedSine = if (sharpness >= 2.9f) {
                rawSine.toFloat()
            } else {
                val s = rawSine.toFloat()
                sign(s) * abs(s).pow(1f / sharpness)
            }
            amplitude * envelope * shapedSine
        } else {
            0f
        }

        // Wound-string micro-texture (D3/G3 only)
        val microTexture = if (isWound && amplitude > 0.01f) {
            sin(47.0 * yNorm * 2.0 * PI).toFloat() * 0.03f * amplitude
        } else 0f

        // Idle shimmer
        val shimmerOffset = shimmerAmp * envelope * sin(
            2.0 * PI * yNorm + shimmerPhase.toDouble(),
        ).toFloat()

        val x = centerX + vibrationOffset + microTexture + shimmerOffset

        if (s == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
        style = Stroke(width = strokeWidth, join = StrokeJoin.Round),
    )
}

private fun DrawScope.drawStringLabel(
    textMeasurer: TextMeasurer,
    primary: String,
    secondary: String,
    centerX: Float,
    primaryY: Float,
    color: Color,
    alpha: Float,
    density: Float,
    fontScale: Float,
) {
    val primarySp = 22f + (28f - 22f) * fontScale.coerceIn(0f, 1f)
    val secondarySp = 15f + (20f - 15f) * fontScale.coerceIn(0f, 1f)
    val primaryStyle = TextStyle(
        fontSize = primarySp.sp,
        fontWeight = FontWeight.Medium,
        color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
        letterSpacing = 1.5.sp,
    )
    val secondaryStyle = TextStyle(
        fontSize = secondarySp.sp,
        fontWeight = FontWeight.Normal,
        color = color.copy(alpha = (alpha * 0.8f).coerceIn(0f, 1f)),
        letterSpacing = 0.5.sp,
    )

    val primaryResult = textMeasurer.measure(primary, primaryStyle)
    val secondaryResult = textMeasurer.measure(secondary, secondaryStyle)

    drawText(
        textLayoutResult = primaryResult,
        topLeft = Offset(
            centerX - primaryResult.size.width / 2f,
            primaryY,
        ),
    )
    drawText(
        textLayoutResult = secondaryResult,
        topLeft = Offset(
            centerX - secondaryResult.size.width / 2f,
            primaryY + primaryResult.size.height + 4f * density,
        ),
    )
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f,
    )
}
