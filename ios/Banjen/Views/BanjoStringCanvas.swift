import SwiftUI
import BanjenCore

// MARK: - Color palette

private extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

private let bgGradientStops: [(CGFloat, Color)] = [
    (0.00, Color(hex: 0xFFFBE9)),
    (0.35, Color(hex: 0xF8F4E0)),
    (0.65, Color(hex: 0xF2EBD5)),
    (1.00, Color(hex: 0xEDE4C8)),
]

private struct StringColors {
    let idle: Color
    let active: Color
    let glow: Color
    let label: Color
}

// 5-entry palette: index 0 = g4 drone (5-string only), indices 1–4 = D3/G3/B3/D4
// paletteIdx = (i + (5 - numStrings)) % 5
private let stringPalette: [StringColors] = [
    StringColors(idle: Color(hex: 0x4F6B7A), active: Color(hex: 0x9BC8DC), glow: Color(hex: 0x9BC8DC), label: Color(hex: 0x6B8A9A)), // g4
    StringColors(idle: Color(hex: 0x49251E), active: Color(hex: 0x8D5746), glow: Color(hex: 0x8D5746), label: Color(hex: 0xFFFBE9)), // D3
    StringColors(idle: Color(hex: 0x5A7C90), active: Color(hex: 0xCBE6F7), glow: Color(hex: 0xCBE6F7), label: Color(hex: 0x2E2420)), // G3
    StringColors(idle: Color(hex: 0x8C6640), active: Color(hex: 0xE8B36B), glow: Color(hex: 0xE8B36B), label: Color(hex: 0x3D322A)), // B3
    StringColors(idle: Color(hex: 0x9F3A0A), active: Color(hex: 0xFB4F00), glow: Color(hex: 0xFB4F00), label: Color(hex: 0xFFFBE9)), // D4
]

private let nutBridgeColor = Color(hex: 0x49251E)
private let nutBridgeHighlight = Color(hex: 0x6B4035)
private let fretColor = Color(hex: 0x2E2420)

private let fretPositions: [CGFloat] = [0.18, 0.33, 0.45, 0.55, 0.65]

private let breathingPeriodMs: Double = 2400
private let breathingOpacityMin: Double = 0.70
private let breathingOpacityMax: Double = 1.00
private let dimmedOpacity: Double = 0.35
private let blurWidthRatio: CGFloat = 2.0
private let hazeWidthRatio: CGFloat = 3.5
private let bridgeHeightDp: CGFloat = 12
private let safePaddingDp: CGFloat = 16
private let shimmerPeriodMs: Double = 3200

// MARK: - Per-string physics

private struct StringPhysics {
    let idleThickPt: CGFloat
    let activeThickPt: CGFloat
    let vibAmpPt: CGFloat
    let vibFreqHz: Double
    let vibWavePeaks: Double
    let springStiffness: Double    // used to set animation response
    let springDamping: Double
    let releaseDurationMs: Double
    let initialSharpness: Double
    let sharpnessTransMs: Double
    let breathingAmplitude: Double
    let breathingPeriodS: Double
    let isWound: Bool
}

private func lerp(_ a: Double, _ b: Double, _ t: Double) -> Double {
    a + (b - a) * min(max(t, 0), 1)
}
private func lerpF(_ a: CGFloat, _ b: CGFloat, _ t: CGFloat) -> CGFloat {
    a + (b - a) * min(max(t, 0), 1)
}

private func computeStringPhysics(_ notes: [Note]) -> [StringPhysics] {
    let freqMin = notes.map(\.frequency).min() ?? 0
    let freqMax = notes.map(\.frequency).max() ?? 0
    let golden = 0.6180339887
    return notes.enumerated().map { i, note in
        let fn = freqMax > freqMin
            ? Double((note.frequency - freqMin) / (freqMax - freqMin))
            : 0.5
        return StringPhysics(
            idleThickPt: CGFloat(lerp(4.0, 2.0, fn)),
            activeThickPt: CGFloat(lerp(5.5, 2.8, fn)),
            vibAmpPt: CGFloat(lerp(8.0, 4.0, fn)),
            vibFreqHz: lerp(3.0, 6.0, fn),
            vibWavePeaks: lerp(2.5, 4.0, fn),
            springStiffness: lerp(300, 500, fn),
            springDamping: lerp(0.50, 0.65, fn),
            releaseDurationMs: lerp(400, 220, fn),
            initialSharpness: lerp(0.7, 1.2, fn),
            sharpnessTransMs: lerp(250, 120, fn),
            breathingAmplitude: lerp(0.09, 0.04, fn),
            breathingPeriodS: 1.5 + 1.8 * (Double(i) * golden).truncatingRemainder(dividingBy: 1.0),
            isWound: note.frequency < 220
        )
    }
}

private func ordinalSuffix(_ n: Int) -> String {
    switch n { case 1: "st"; case 2: "nd"; case 3: "rd"; default: "th" }
}

// MARK: - Per-string animation state

private struct StringAnimState {
    // All values range [0,1] except sharpness and opacityFactor
    var vibrationAmp: Double = 0         // 0 = still, 1 = full vibration
    var colorFactor: Double = 0          // 0 = idle color, 1 = active color
    var opacityFactor: Double = 1        // 1 = full, dimmedOpacity when dimmed
    var waveSharpness: Double = 3.0      // 3 = pure sine; lower = angular
    var attackProgress: Double = 1       // 0 = biased gaussian, 1 = symmetric
    var revealProgress: Double = 0       // 0 = hidden, 1 = fully drawn
    var touchYNorm: Double = 0.5         // last tap Y (0=nut, 1=bridge)
}

// MARK: - BanjoStringCanvas

struct BanjoStringCanvas: View {
    let notes: [Note]
    let selectedString: Int
    let onStringSelected: (Int) -> Void

    // Per-string animation state (driven from .onChange)
    @State private var animStates: [StringAnimState]
    // Global continuous phase (elapsed seconds, advanced by TimelineView)
    @State private var wavePhase: Double = 0
    // Shared breathing and shimmer (global, computed from phase)
    // Tap zone: 0=nut, 1=center, 2=bridge
    @State private var tapZone: Int = 1

    // String geometry (top/bottom Y in points) — updated during draw, read during tap
    @State private var stringTopPt: CGFloat = 0
    @State private var stringBottomPt: CGFloat = 0

    // Real view size captured by GeometryReader; used in tap band computation
    @State private var viewSize: CGSize = .zero

    // Physics derived from notes
    @State private var physics: [StringPhysics]

    // Track previous selectedString so we can react on change
    @State private var prevSelected: Int = -2   // sentinel

    init(notes: [Note], selectedString: Int, onStringSelected: @escaping (Int) -> Void) {
        self.notes = notes
        self.selectedString = selectedString
        self.onStringSelected = onStringSelected
        let n = notes.count
        _animStates = State(initialValue: Array(repeating: StringAnimState(), count: n))
        _physics = State(initialValue: computeStringPhysics(notes))
    }

    var body: some View {
        TimelineView(.animation) { timeline in
            let now = timeline.date.timeIntervalSinceReferenceDate
            Canvas { ctx, size in
                drawFrame(ctx: &ctx, size: size, now: now)
            }
            .onChange(of: now) { _, newNow in
                // Advance wavePhase at ~60fps (TimelineView drives this)
                // We keep a simple elapsed-second counter via the timeline date
                wavePhase = newNow.truncatingRemainder(dividingBy: 3600) // keep it small
            }
            .onChange(of: notes) { _, newNotes in
                physics = computeStringPhysics(newNotes)
                let n = newNotes.count
                animStates = Array(repeating: StringAnimState(), count: n)
                // Stagger reveal animations
                for i in 0..<n {
                    let delay = Double(i) * 0.15
                    DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                        withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.38)) {
                            if i < animStates.count { animStates[i].revealProgress = 1 }
                        }
                    }
                }
            }
            .onChange(of: selectedString) { oldVal, newVal in
                handleSelectionChange(newSelected: newVal)
            }
            .onAppear {
                // Trigger opening reveal stagger
                let n = notes.count
                for i in 0..<n {
                    let delay = Double(i) * 0.15
                    DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                        withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.38)) {
                            if i < animStates.count { animStates[i].revealProgress = 1 }
                        }
                    }
                }
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onEnded { value in
                        handleTap(at: value.location)
                    }
            )
            .background(
                GeometryReader { geo in
                    Color.clear
                        .onAppear { viewSize = geo.size }
                        .onChange(of: geo.size) { _, s in viewSize = s }
                }
            )
            .overlay(accessibilityOverlay)
        }
    }

    // MARK: - Tap handling

    private func handleTap(at location: CGPoint) {
        let n = notes.count
        guard n > 0, viewSize.width > 0 else { return }

        // Snap reveal if mid-animation
        let anyRevealing = animStates.contains { $0.revealProgress < 1 }
        if anyRevealing {
            for i in 0..<animStates.count { animStates[i].revealProgress = 1 }
        }

        // Band index from X (mirrors Kotlin: relX / bandWidth, clamped)
        let avail = viewSize.width - 2 * safePaddingDp
        let relX = location.x - safePaddingDp
        guard relX >= 0, relX <= avail else { return }
        let band = Int(relX / (avail / CGFloat(n))).clamped(to: 0...(n - 1))

        // Tap zone from Y (nut <0.3, bridge >0.7, center else)
        let sTop = stringTopPt
        let sBottom = stringBottomPt
        let sLen = sBottom - sTop
        if sLen > 0 {
            let yNorm = ((location.y - sTop) / sLen).clamped(to: 0.05...0.95)
            tapZone = yNorm < 0.3 ? 0 : yNorm > 0.7 ? 2 : 1
        }
        if band < animStates.count {
            animStates[band].touchYNorm = sLen > 0
                ? Double(((location.y - sTop) / sLen).clamped(to: 0.05...0.95))
                : 0.5
        }

        // Toggle off if tapping active string, else select
        if band == selectedString {
            onStringSelected(-1)
        } else {
            onStringSelected(band)
        }
    }

    // MARK: - Selection change

    private func handleSelectionChange(newSelected: Int) {
        let n = animStates.count
        for i in 0..<n {
            let p = physics[i]
            if newSelected == i {
                // Sharpness offset by tap zone
                let sharpnessOffset: Double = tapZone == 0 ? 0.05 : tapZone == 2 ? 0.10 : 0
                // Spring onset for vibration
                withAnimation(.interpolatingSpring(stiffness: p.springStiffness, damping: p.springDamping)) {
                    animStates[i].vibrationAmp = 1
                }
                // Sharpness: snap to initial then animate to 3.0
                animStates[i].waveSharpness = p.initialSharpness + sharpnessOffset
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: p.sharpnessTransMs / 1000)) {
                    animStates[i].waveSharpness = 3.0
                }
                // Attack: snap to 0 then animate to 1
                animStates[i].attackProgress = 0
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.3)) {
                    animStates[i].attackProgress = 1
                }
                // Color
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.2)) {
                    animStates[i].colorFactor = 1
                    animStates[i].opacityFactor = 1
                }
            } else if newSelected >= 0 {
                // Another string active — decay and dim
                let dur = p.releaseDurationMs / 1000
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: dur)) {
                    animStates[i].vibrationAmp = 0
                }
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.35)) {
                    animStates[i].colorFactor = 0
                }
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.3)) {
                    animStates[i].opacityFactor = dimmedOpacity
                }
            } else {
                // None selected — return to idle
                let dur = p.releaseDurationMs / 1000
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: dur)) {
                    animStates[i].vibrationAmp = 0
                }
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.35)) {
                    animStates[i].colorFactor = 0
                }
                withAnimation(.timingCurve(0.25, 0.46, 0.45, 0.94, duration: 0.3)) {
                    animStates[i].opacityFactor = 1
                }
            }
        }
    }

    // MARK: - Draw

    private func drawFrame(ctx: inout GraphicsContext, size: CGSize, now: Double) {
        let n = notes.count
        guard n > 0 else { return }

        let w = size.width
        let h = size.height
        let hPad = safePaddingDp
        let vPad = safePaddingDp
        let bridgeH = bridgeHeightDp
        let cornerRadius: CGFloat = 2

        // Compute global time-based animation values
        let breatheT = (now.truncatingRemainder(dividingBy: breathingPeriodMs / 1000)) / (breathingPeriodMs / 1000)
        // EaseInOutSine approximation: sin(π * t / 2) for forward, then reverse
        let breathePhase = breatheT.truncatingRemainder(dividingBy: 1.0)
        let breatheOscillator = sin(.pi * breathePhase) // 0→1→0
        let breatheAlpha = breathingOpacityMin + (breathingOpacityMax - breathingOpacityMin) * breatheOscillator

        let shimmerPhase = (now.truncatingRemainder(dividingBy: shimmerPeriodMs / 1000)) / (shimmerPeriodMs / 1000) * (2 * .pi)

        // --- Background gradient ---
        let gradient = Gradient(stops: bgGradientStops.map {
            Gradient.Stop(color: $0.1, location: $0.0)
        })
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(gradient, startPoint: .zero, endPoint: CGPoint(x: 0, y: h))
        )

        // --- Vignette ---
        let vigRadius = max(w, h) * 0.85
        let vigGradient = Gradient(colors: [.clear, Color.black.opacity(0.40)])
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .radialGradient(vigGradient, center: CGPoint(x: w/2, y: h/2), startRadius: 0, endRadius: vigRadius)
        )

        // --- Bridge ---
        let bridgeY = h - vPad - bridgeH
        let barLeft = hPad
        let barWidth = w - 2 * hPad

        let bridgeRect = CGRect(x: barLeft, y: bridgeY, width: barWidth, height: bridgeH)
        ctx.fill(Path(roundedRect: bridgeRect, cornerRadius: cornerRadius), with: .color(nutBridgeColor))
        ctx.fill(
            Path(CGRect(x: barLeft, y: bridgeY, width: barWidth, height: 1)),
            with: .color(nutBridgeHighlight.opacity(0.60))
        )

        // --- Frets ---
        let stringTop: CGFloat = 0
        let stringBottom = bridgeY
        let stringLength = stringBottom - stringTop

        // Store geometry for tap handling (DispatchQueue.main.async to avoid mutation during view update)
        if stringTopPt != stringTop || stringBottomPt != stringBottom {
            let top = stringTop
            let bot = stringBottom
            DispatchQueue.main.async {
                stringTopPt = top
                stringBottomPt = bot
            }
        }

        for fretPos in fretPositions {
            let fretY = stringTop + stringLength * fretPos
            ctx.fill(
                Path(CGRect(x: barLeft, y: fretY, width: barWidth, height: 1)),
                with: .color(fretColor.opacity(0.35))
            )
        }

        // --- Strings ---
        let availableWidth = w - 2 * hPad
        let bandWidth = availableWidth / CGFloat(n)

        for i in 0..<n {
            let anim = animStates[i]
            let p = physics[i]
            let paletteIdx = (i + (5 - n)) % 5
            let palette = stringPalette[paletteIdx]

            let centerX = hPad + bandWidth * (CGFloat(i) + 0.5)

            let stringColor = lerpColor(palette.idle, palette.active, CGFloat(anim.colorFactor))
            let glowColor = palette.glow

            let isActive = anim.vibrationAmp > 0.01
            let effectiveAlpha: CGFloat = isActive
                ? CGFloat(anim.opacityFactor)
                : CGFloat(anim.opacityFactor) * CGFloat(breatheAlpha)

            let idleThick = p.idleThickPt
            let activeThick = p.activeThickPt
            let currentThick = lerpF(idleThick, activeThick, CGFloat(anim.colorFactor))

            let maxAmpPx = p.vibAmpPt * CGFloat(anim.vibrationAmp)
            let shimmerAmpPx: CGFloat = 0.3

            // Breathing width factor
            let breathFactor: CGFloat = isActive
                ? CGFloat(1 + p.breathingAmplitude * sin(2 * .pi * now / p.breathingPeriodS))
                : 1

            let glowAlpha: CGFloat = isActive ? 0.45 : 0.20
            let coreWidth = currentThick * breathFactor
            let blurWidth = coreWidth * blurWidthRatio
            let hazeWidth = coreWidth * hazeWidthRatio

            // Layer 1: haze
            drawStringPath(
                ctx: &ctx,
                centerX: centerX,
                stringTop: stringTop,
                stringLength: stringLength,
                amplitude: maxAmpPx,
                shimmerAmp: isActive ? 0 : shimmerAmpPx,
                shimmerPhase: shimmerPhase,
                wavePhase: now,
                wavePeaks: p.vibWavePeaks,
                waveFreqHz: p.vibFreqHz,
                color: glowColor,
                alpha: glowAlpha * 0.15 * effectiveAlpha,
                strokeWidth: hazeWidth,
                revealProgress: CGFloat(anim.revealProgress)
            )

            // Layer 2: blur
            drawStringPath(
                ctx: &ctx,
                centerX: centerX,
                stringTop: stringTop,
                stringLength: stringLength,
                amplitude: maxAmpPx,
                shimmerAmp: isActive ? 0 : shimmerAmpPx,
                shimmerPhase: shimmerPhase,
                wavePhase: now,
                wavePeaks: p.vibWavePeaks,
                waveFreqHz: p.vibFreqHz,
                color: glowColor,
                alpha: glowAlpha * 0.40 * effectiveAlpha,
                strokeWidth: blurWidth,
                revealProgress: CGFloat(anim.revealProgress)
            )

            // Layer 3: core
            drawStringPath(
                ctx: &ctx,
                centerX: centerX,
                stringTop: stringTop,
                stringLength: stringLength,
                amplitude: maxAmpPx,
                shimmerAmp: isActive ? 0 : shimmerAmpPx,
                shimmerPhase: shimmerPhase,
                wavePhase: now,
                wavePeaks: p.vibWavePeaks,
                waveFreqHz: p.vibFreqHz,
                color: stringColor,
                alpha: effectiveAlpha,
                strokeWidth: coreWidth,
                sharpness: anim.waveSharpness,
                touchYNorm: anim.touchYNorm,
                attackProgress: anim.attackProgress,
                revealProgress: CGFloat(anim.revealProgress),
                isWound: p.isWound
            )

            // --- Label ---
            let labelColor = palette.label
            let labelAlpha = (isActive ? 1.0 : Double(effectiveAlpha) * 0.65) * anim.revealProgress
            let labelY = bridgeY - 76
            let ordinalNum = n - i
            let ordinal = "\(ordinalNum)\(ordinalSuffix(ordinalNum))"

            drawStringLabel(
                ctx: &ctx,
                primary: notes[i].name,
                secondary: ordinal,
                centerX: centerX,
                primaryY: labelY,
                color: labelColor,
                alpha: CGFloat(labelAlpha),
                colorFactor: CGFloat(anim.colorFactor)
            )
        }
    }

    // MARK: - String path drawing

    private func drawStringPath(
        ctx: inout GraphicsContext,
        centerX: CGFloat,
        stringTop: CGFloat,
        stringLength: CGFloat,
        amplitude: CGFloat,
        shimmerAmp: CGFloat,
        shimmerPhase: Double,
        wavePhase: Double,
        wavePeaks: Double,
        waveFreqHz: Double,
        color: Color,
        alpha: CGFloat,
        strokeWidth: CGFloat,
        sharpness: Double = 3.0,
        touchYNorm: Double = 0.5,
        attackProgress: Double = 1.0,
        revealProgress: CGFloat = 1.0,
        isWound: Bool = false
    ) {
        guard alpha > 0.001, revealProgress > 0 else { return }

        let segments = 80
        let revealSegments = max(1, Int(revealProgress * CGFloat(segments)))
        let segH = stringLength / CGFloat(segments)

        var path = Path()
        for s in 0...revealSegments {
            let y = stringTop + CGFloat(s) * segH
            let yNorm = stringLength > 0 ? Double(s) / Double(segments) : 0

            // Envelope
            let symEnv = sin(.pi * yNorm)
            let gaussExp = -((yNorm - touchYNorm) * (yNorm - touchYNorm)) / (2 * 0.35 * 0.35)
            let gaussianBias = max(0, symEnv * exp(gaussExp))
            let envelope = symEnv * attackProgress + gaussianBias * (1 - attackProgress)

            // Vibration with sharpness shaping
            let vibOffset: CGFloat
            if amplitude > 0.01 {
                let rawSine = sin(2 * .pi * wavePeaks * yNorm + wavePhase * waveFreqHz * 2 * .pi)
                let shapedSine: Double
                if sharpness >= 2.9 {
                    shapedSine = rawSine
                } else {
                    let s = rawSine
                    shapedSine = (s >= 0 ? 1 : -1) * pow(abs(s), 1 / sharpness)
                }
                vibOffset = CGFloat(amplitude) * CGFloat(envelope) * CGFloat(shapedSine)
            } else {
                vibOffset = 0
            }

            // Wound micro-texture
            let microTexture: CGFloat = (isWound && amplitude > 0.01)
                ? CGFloat(sin(47 * yNorm * 2 * .pi) * 0.03) * amplitude
                : 0

            // Idle shimmer
            let shimmerOffset: CGFloat = shimmerAmp * CGFloat(envelope) * CGFloat(sin(2 * .pi * yNorm + shimmerPhase))

            let x = centerX + vibOffset + microTexture + shimmerOffset

            if s == 0 {
                path.move(to: CGPoint(x: x, y: y))
            } else {
                path.addLine(to: CGPoint(x: x, y: y))
            }
        }

        ctx.stroke(path, with: .color(color.opacity(Double(alpha.clamped(to: 0...1)))), lineWidth: strokeWidth)
    }

    // MARK: - Label drawing

    private func drawStringLabel(
        ctx: inout GraphicsContext,
        primary: String,
        secondary: String,
        centerX: CGFloat,
        primaryY: CGFloat,
        color: Color,
        alpha: CGFloat,
        colorFactor: CGFloat
    ) {
        guard alpha > 0.001 else { return }

        let primarySp: CGFloat = 28 + (36 - 28) * colorFactor.clamped(to: 0...1)
        let secondarySp: CGFloat = 19 + (26 - 19) * colorFactor.clamped(to: 0...1)

        let primaryText = Text(primary)
            .font(.system(size: primarySp, weight: .medium))
            .foregroundColor(color.opacity(Double(alpha.clamped(to: 0...1))))
            .kerning(1.5)

        let secondaryText = Text(secondary)
            .font(.system(size: secondarySp, weight: .regular))
            .foregroundColor(color.opacity(Double((alpha * 0.8).clamped(to: 0...1))))
            .kerning(0.5)

        let resolvedPrimary = ctx.resolve(primaryText)
        let resolvedSecondary = ctx.resolve(secondaryText)

        let primarySize = resolvedPrimary.measure(in: CGSize(width: 200, height: 200))
        let secondarySize = resolvedSecondary.measure(in: CGSize(width: 200, height: 200))

        ctx.draw(resolvedPrimary, at: CGPoint(x: centerX - primarySize.width / 2, y: primaryY), anchor: .topLeading)
        ctx.draw(resolvedSecondary, at: CGPoint(x: centerX - secondarySize.width / 2, y: primaryY + primarySize.height + 4), anchor: .topLeading)
    }

    // MARK: - Accessibility overlay

    @ViewBuilder
    private var accessibilityOverlay: some View {
        HStack(spacing: 0) {
            ForEach(0..<notes.count, id: \.self) { i in
                let ordinalNum = notes.count - i
                let suffix = ordinalSuffix(ordinalNum)
                let noteName = notes[i].name
                let isSelected = selectedString == i
                let description = "\(ordinalNum)\(suffix) string, note \(noteName). Double tap to play."
                let clickLabel = isSelected ? "Stop \(noteName)" : "Play \(noteName)"

                Color.clear
                    .contentShape(Rectangle())
                    .accessibilityElement()
                    .accessibilityLabel(description)
                    .accessibilityAddTraits(.isButton)
                    .accessibilityAddTraits(isSelected ? .isSelected : [])
                    .accessibilityAction(named: clickLabel) {
                        onStringSelected(isSelected ? -1 : i)
                    }
                    .allowsHitTesting(false)
            }
        }
        .padding(.horizontal, safePaddingDp)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        // Overlay for a11y only; canvas receives all real touches
    }
}


// MARK: - Helpers

private func lerpColor(_ a: Color, _ b: Color, _ t: CGFloat) -> Color {
    let f = Double(t.clamped(to: 0...1))
    let ua = UIColor(a)
    let ub = UIColor(b)
    var ar: CGFloat = 0, ag: CGFloat = 0, ab: CGFloat = 0, aa: CGFloat = 0
    var br: CGFloat = 0, bg: CGFloat = 0, bb: CGFloat = 0, ba: CGFloat = 0
    ua.getRed(&ar, green: &ag, blue: &ab, alpha: &aa)
    ub.getRed(&br, green: &bg, blue: &bb, alpha: &ba)
    return Color(
        red: Double(ar) + (Double(br) - Double(ar)) * f,
        green: Double(ag) + (Double(bg) - Double(ag)) * f,
        blue: Double(ab) + (Double(bb) - Double(ab)) * f,
        opacity: Double(aa) + (Double(ba) - Double(aa)) * f
    )
}

private extension Comparable {
    func clamped(to range: ClosedRange<Self>) -> Self {
        min(max(self, range.lowerBound), range.upperBound)
    }
}


// MARK: - Previews

#Preview("4-string DGBD") {
    let notes = [
        Note("D3", 146.83),
        Note("G3", 196.00),
        Note("B3", 246.94),
        Note("D4", 293.66),
    ]
    BanjoStringCanvas(notes: notes, selectedString: 1) { _ in }
        .frame(height: 500)
        .background(Color.black)
}

#Preview("5-string Open G") {
    let notes = [
        Note("g4", 392.00),
        Note("D3", 146.83),
        Note("G3", 196.00),
        Note("B3", 246.94),
        Note("D4", 293.66),
    ]
    BanjoStringCanvas(notes: notes, selectedString: -1) { _ in }
        .frame(height: 500)
        .background(Color.black)
}
