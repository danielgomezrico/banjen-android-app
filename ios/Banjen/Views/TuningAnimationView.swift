import SwiftUI
import BanjenCore

// MARK: - String accent colors (mirrored from TuningAnimation.kt)

private let stringAccentColors: [Color] = [
    Color(red: 0x8D/255, green: 0x57/255, blue: 0x46/255), // D3 — dark brown
    Color(red: 0xCB/255, green: 0xE6/255, blue: 0xF7/255), // G3 — light blue
    Color(red: 0xE8/255, green: 0xB3/255, blue: 0x6B/255), // B3 — warm amber
    Color(red: 0xFB/255, green: 0x4F/255, blue: 0x00/255), // D4 — bright orange
]

private let neutralColor = Color(red: 0xFF/255, green: 0xFB/255, blue: 0xE9/255)

// MARK: - Ring/dot constants (from TuningAnimationConstants)

private let ringOuterFraction: CGFloat = 0.80
private let ringMiddleFraction: CGFloat = 0.60
private let ringInnerFraction: CGFloat = 0.40

private let ringOuterStroke: CGFloat = 2
private let ringMiddleStroke: CGFloat = 2.5
private let ringInnerStroke: CGFloat = 3

private let centerDotRadius: CGFloat = 5   // matches Kotlin's 5.dp.toPx() center dot

private let idleScaleMin: CGFloat = 0.97
private let idleScaleMax: CGFloat = 1.03
private let idleBreathePeriodMs: Double = 2000  // half-period per direction

// MARK: - TuningAnimationView

/// Concentric-ring tuning animation. Mirrors the Android `CanvasFallbackAnimation`
/// composable (3 rings + center dot, idle breathing 0.97↔1.03 over 2000ms,
/// cross-fade color on string change).
struct TuningAnimationView: View {
    let selectedOption: Int
    let pitchCheckMode: Bool
    let pitchResult: PitchResult?

    // Derived animation state (mirrors deriveTuningAnimationState in Kotlin)
    private var animState: TuningAnimationState {
        deriveTuningAnimationState(
            selectedOption: selectedOption,
            pitchCheckMode: pitchCheckMode,
            pitchResult: pitchResult
        )
    }

    // Accent color: per-string when a string is selected, else neutral
    private var accentColor: Color {
        if selectedOption >= 0, selectedOption < stringAccentColors.count {
            return stringAccentColors[selectedOption]
        }
        return neutralColor
    }

    var body: some View {
        TimelineView(.animation) { timeline in
            let now = timeline.date.timeIntervalSinceReferenceDate
            Canvas { ctx, size in
                drawRings(ctx: &ctx, size: size, now: now)
            }
        }
        .frame(maxWidth: .infinity)
        .animation(.easeInOut(duration: 0.3), value: selectedOption)
    }

    // MARK: - Draw

    private func drawRings(ctx: inout GraphicsContext, size: CGSize, now: Double) {
        let center = CGPoint(x: size.width / 2, y: size.height / 2)
        let baseRadius = min(size.width, size.height) / 2

        // Idle breathing: period = idleBreathePeriodMs ms per direction, triangle wave → sine-smoothed
        let fullPeriodS = idleBreathePeriodMs / 1000 * 2  // full oscillation
        let t = now.truncatingRemainder(dividingBy: fullPeriodS) / fullPeriodS
        // FastOutSlowIn approximation: use sin-based easing
        let breatheRaw = sin(.pi * t)  // 0→1→0 over full period
        let breatheScale = idleScaleMin + (idleScaleMax - idleScaleMin) * CGFloat(breatheRaw)

        let color = accentColor

        // Background glow
        ctx.fill(
            Path(ellipseIn: CGRect(
                x: center.x - baseRadius * ringOuterFraction * breatheScale,
                y: center.y - baseRadius * ringOuterFraction * breatheScale,
                width: 2 * baseRadius * ringOuterFraction * breatheScale,
                height: 2 * baseRadius * ringOuterFraction * breatheScale
            )),
            with: .color(color.opacity(0.04))
        )

        // Outer ring
        drawRing(ctx: &ctx, center: center, radius: baseRadius * ringOuterFraction * breatheScale,
                 color: color, alpha: 0.20, strokeWidth: ringOuterStroke)

        // Middle ring
        drawRing(ctx: &ctx, center: center, radius: baseRadius * ringMiddleFraction * breatheScale,
                 color: color, alpha: 0.25, strokeWidth: ringMiddleStroke)

        // Inner ring
        drawRing(ctx: &ctx, center: center, radius: baseRadius * ringInnerFraction * breatheScale,
                 color: color, alpha: 0.30, strokeWidth: ringInnerStroke)

        // Center dot
        ctx.fill(
            Path(ellipseIn: CGRect(
                x: center.x - centerDotRadius,
                y: center.y - centerDotRadius,
                width: centerDotRadius * 2,
                height: centerDotRadius * 2
            )),
            with: .color(color.opacity(0.40))
        )
    }

    private func drawRing(
        ctx: inout GraphicsContext,
        center: CGPoint,
        radius: CGFloat,
        color: Color,
        alpha: Double,
        strokeWidth: CGFloat
    ) {
        let rect = CGRect(
            x: center.x - radius,
            y: center.y - radius,
            width: radius * 2,
            height: radius * 2
        )
        ctx.stroke(
            Path(ellipseIn: rect),
            with: .color(color.opacity(alpha)),
            lineWidth: strokeWidth
        )
    }
}

// MARK: - Previews

#Preview("Idle") {
    TuningAnimationView(selectedOption: -1, pitchCheckMode: false, pitchResult: nil)
        .frame(height: 200)
        .background(Color(red: 0x1A/255, green: 0x12/255, blue: 0x10/255))
}

#Preview("String 1 selected") {
    TuningAnimationView(selectedOption: 1, pitchCheckMode: false, pitchResult: nil)
        .frame(height: 200)
        .background(Color(red: 0x1A/255, green: 0x12/255, blue: 0x10/255))
}

#Preview("In tune") {
    TuningAnimationView(
        selectedOption: 0,
        pitchCheckMode: true,
        pitchResult: PitchResult(detectedHz: 196.0, targetHz: 196.0, centDeviation: 0.5, status: .inTune)
    )
    .frame(height: 200)
    .background(Color(red: 0x1A/255, green: 0x12/255, blue: 0x10/255))
}
