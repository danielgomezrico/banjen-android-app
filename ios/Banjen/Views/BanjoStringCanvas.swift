import SwiftUI
import BanjenCore

// MARK: - Color palette (mirrors BanjoStringCanvas.kt exactly)

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
    StringColors(idle: Color(hex: 0x49251E), active: Color(hex: 0x8D5746), glow: Color(hex: 0x8D5746), label: Color(hex: 0x3D2A25)), // D3
    StringColors(idle: Color(hex: 0x5A7C90), active: Color(hex: 0xCBE6F7), glow: Color(hex: 0xCBE6F7), label: Color(hex: 0x2E2420)), // G3
    StringColors(idle: Color(hex: 0x9C5C2A), active: Color(hex: 0xE89C5A), glow: Color(hex: 0xE89C5A), label: Color(hex: 0x3D322A)), // B3
    StringColors(idle: Color(hex: 0xBF4A1A), active: Color(hex: 0xFB6A2A), glow: Color(hex: 0xFB6A2A), label: Color(hex: 0x5C2F1F)), // D4
]

private let breathingPeriodMs: Double = 2400
private let breathingOpacityMin: Double = 0.70
private let breathingOpacityMax: Double = 1.00
private let dimmedOpacity: Double = 0.35
private let blurWidthRatio: CGFloat = 2.0
private let hazeWidthRatio: CGFloat = 3.5
private let shimmerPeriodMs: Double = 3200

// Android draws strings edge-to-edge with NO horizontal padding (hPad = 0),
// starting 170dp below the top (= (220 - 50) in Kotlin), running to the bottom.
private let stringTopMargin: CGFloat = 170

// MARK: - Per-string physics (mirrors computeStringPhysics in Kotlin)

private struct StringPhysics {
    let idleThickPt: CGFloat
    let activeThickPt: CGFloat
    let vibAmpPt: CGFloat
    let vibFreqHz: Double
    let vibWavePeaks: Double
    let springStiffness: Double
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
            idleThickPt: CGFloat(lerp(2.0, 1.2, fn)),
            activeThickPt: CGFloat(lerp(3.2, 1.8, fn)),
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

private func stringOrdinal(_ n: Int) -> String { "\(n)ª" }

// MARK: - Time-driven tween engine
//
// SwiftUI's `withAnimation` cannot interpolate a stored value that is read
// imperatively inside a Canvas draw closure — it would snap to the target.
// To reproduce Android's `Animatable.animateTo` curves we drive every
// per-string value manually from the TimelineView clock.

private enum Curve {
    case linear, easeOutCubic, easeOutQuad

    func ease(_ t: Double) -> Double {
        let x = min(max(t, 0), 1)
        switch self {
        case .linear: return x
        case .easeOutCubic: return 1 - pow(1 - x, 3)
        case .easeOutQuad: return 1 - pow(1 - x, 2)
        }
    }
}

// Underdamped spring step response (0 → 1), matching Compose `spring(stiffness, dampingRatio)`
// with mass = 1: naturalFreq ωn = sqrt(stiffness). Naturally overshoots then settles at 1.
private func springStep(_ elapsed: Double, stiffness: Double, zeta: Double) -> Double {
    if elapsed <= 0 { return 0 }
    let wn = sqrt(stiffness)
    let z = min(max(zeta, 0.0001), 0.9999)
    let wd = wn * sqrt(1 - z * z)
    let e = exp(-z * wn * elapsed)
    let v = 1 - e * (cos(wd * elapsed) + (z / sqrt(1 - z * z)) * sin(wd * elapsed))
    return v
}

private struct Tween {
    var from: Double
    var to: Double
    var start: Double        // absolute time (timeIntervalSinceReferenceDate)
    var duration: Double     // seconds
    var curve: Curve = .linear
    var spring: (stiffness: Double, zeta: Double)? = nil

    static func constant(_ v: Double) -> Tween {
        Tween(from: v, to: v, start: 0, duration: 0)
    }

    func value(at now: Double) -> Double {
        if let sp = spring {
            let e = now - start
            if e <= 0 { return from }
            return from + (to - from) * springStep(e, stiffness: sp.stiffness, zeta: sp.zeta)
        }
        if duration <= 0 { return now >= start ? to : from }
        let t = (now - start) / duration
        return from + (to - from) * curve.ease(t)
    }
}

private struct StringAnim {
    var vibration = Tween.constant(0)   // 0 = still, 1 = full vibration
    var color = Tween.constant(0)       // 0 = idle color, 1 = active color
    var opacity = Tween.constant(1)     // 1 = full, dimmedOpacity when dimmed
    var sharpness = Tween.constant(3)   // 3 = pure sine; lower = angular
    var attack = Tween.constant(1)      // 0 = biased gaussian, 1 = symmetric
    var reveal = Tween.constant(0)      // 0 = hidden, 1 = fully drawn
    var touchYNorm: Double = 0.5
}

private func nowRef() -> Double { Date().timeIntervalSinceReferenceDate }

// MARK: - BanjoStringCanvas

struct BanjoStringCanvas: View {
    let notes: [Note]
    let selectedString: Int
    let onStringSelected: (Int) -> Void

    @State private var anims: [StringAnim]
    @State private var physics: [StringPhysics]
    // Tap zone: 0 = nut, 1 = center, 2 = bridge
    @State private var tapZone: Int = 1
    // String geometry (top/bottom Y in points) — updated during draw, read during tap
    @State private var stringTopPt: CGFloat = 0
    @State private var stringBottomPt: CGFloat = 0
    // Real view size captured by GeometryReader; used in tap band computation
    @State private var viewSize: CGSize = .zero

    init(notes: [Note], selectedString: Int, onStringSelected: @escaping (Int) -> Void) {
        self.notes = notes
        self.selectedString = selectedString
        self.onStringSelected = onStringSelected
        let n = notes.count
        _anims = State(initialValue: Array(repeating: StringAnim(), count: n))
        _physics = State(initialValue: computeStringPhysics(notes))
    }

    var body: some View {
        TimelineView(.animation) { timeline in
            let now = timeline.date.timeIntervalSinceReferenceDate
            Canvas { ctx, size in
                drawFrame(ctx: &ctx, size: size, now: now)
            }
            .onChange(of: notes) { _, newNotes in
                physics = computeStringPhysics(newNotes)
                let n = newNotes.count
                if n != anims.count {
                    // Instrument switch (string count changed): reset + replay reveal.
                    anims = Array(repeating: StringAnim(), count: n)
                    triggerReveal(count: n)
                }
            }
            .onChange(of: selectedString) { _, newVal in
                handleSelectionChange(newSelected: newVal)
            }
            .onAppear {
                triggerReveal(count: notes.count)
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onEnded { value in handleTap(at: value.location) }
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

    // MARK: - Reveal (initial micro-interaction)
    // Staggered left-to-right (150ms apart), each drawing top-to-bridge over 380ms EaseOutCubic.
    private func triggerReveal(count n: Int) {
        guard n > 0 else { return }
        let base = nowRef()
        for i in 0..<min(n, anims.count) {
            anims[i].reveal = Tween(
                from: 0, to: 1,
                start: base + Double(i) * 0.15,
                duration: 0.38,
                curve: .easeOutCubic
            )
        }
    }

    // MARK: - Tap handling

    private func handleTap(at location: CGPoint) {
        let n = notes.count
        guard n > 0, viewSize.width > 0 else { return }

        // Snap reveal to full if mid-animation so the tone starts immediately.
        let t = nowRef()
        let anyRevealing = anims.contains { $0.reveal.value(at: t) < 1 }
        if anyRevealing {
            for i in 0..<anims.count { anims[i].reveal = .constant(1) }
        }

        // Band index from X (edge-to-edge, hPad = 0; mirrors Kotlin relX / bandWidth clamped)
        let avail = viewSize.width
        let relX = location.x
        guard relX >= 0, relX <= avail else { return }
        let band = Int(relX / (avail / CGFloat(n))).clamped(to: 0...(n - 1))

        // Tap zone from Y (nut <0.3, bridge >0.7, center else)
        let sTop = stringTopPt
        let sBottom = stringBottomPt
        let sLen = sBottom - sTop
        if sLen > 0 {
            let yNorm = ((location.y - sTop) / sLen).clamped(to: 0.05...0.95)
            tapZone = yNorm < 0.3 ? 0 : yNorm > 0.7 ? 2 : 1
            if band < anims.count {
                anims[band].touchYNorm = Double(yNorm)
            }
        } else if band < anims.count {
            anims[band].touchYNorm = 0.5
        }

        // Toggle off if tapping active string, else select
        onStringSelected(band == selectedString ? -1 : band)
    }

    // MARK: - Selection change (mirrors Kotlin LaunchedEffect(selectedString, i))

    private func handleSelectionChange(newSelected: Int) {
        let now = nowRef()
        for i in 0..<anims.count {
            let p = physics[i]
            if newSelected == i {
                // Active — spring onset + color/sharpness/attack attack.
                let sharpnessOffset: Double = tapZone == 0 ? 0.05 : tapZone == 2 ? 0.10 : 0
                anims[i].vibration = Tween(
                    from: anims[i].vibration.value(at: now), to: 1,
                    start: now, duration: 0,
                    spring: (p.springStiffness, p.springDamping)
                )
                // Sharpness snaps to initial (+zone offset) then eases to 3.0.
                anims[i].sharpness = Tween(
                    from: p.initialSharpness + sharpnessOffset, to: 3.0,
                    start: now, duration: p.sharpnessTransMs / 1000, curve: .easeOutCubic
                )
                // Attack snaps to 0 then eases to 1.
                anims[i].attack = Tween(from: 0, to: 1, start: now, duration: 0.3, curve: .easeOutCubic)
                anims[i].color = Tween(from: anims[i].color.value(at: now), to: 1, start: now, duration: 0.2, curve: .easeOutCubic)
                anims[i].opacity = Tween(from: anims[i].opacity.value(at: now), to: 1, start: now, duration: 0.2, curve: .easeOutCubic)
            } else if newSelected >= 0 {
                // Another string active — decay and dim.
                let decayCurve: Curve = i < anims.count / 2 ? .easeOutCubic : .easeOutQuad
                anims[i].vibration = Tween(from: anims[i].vibration.value(at: now), to: 0, start: now, duration: p.releaseDurationMs / 1000, curve: decayCurve)
                anims[i].color = Tween(from: anims[i].color.value(at: now), to: 0, start: now, duration: 0.35, curve: .easeOutCubic)
                anims[i].opacity = Tween(from: anims[i].opacity.value(at: now), to: dimmedOpacity, start: now, duration: 0.3, curve: .easeOutCubic)
            } else {
                // None selected — return to idle.
                let decayCurve: Curve = i < anims.count / 2 ? .easeOutCubic : .easeOutQuad
                anims[i].vibration = Tween(from: anims[i].vibration.value(at: now), to: 0, start: now, duration: p.releaseDurationMs / 1000, curve: decayCurve)
                anims[i].color = Tween(from: anims[i].color.value(at: now), to: 0, start: now, duration: 0.35, curve: .easeOutCubic)
                anims[i].opacity = Tween(from: anims[i].opacity.value(at: now), to: 1, start: now, duration: 0.3, curve: .easeOutCubic)
            }
        }
    }

    // MARK: - Draw

    private func drawFrame(ctx: inout GraphicsContext, size: CGSize, now: Double) {
        let n = notes.count
        guard n > 0, anims.count == n, physics.count == n else { return }

        let w = size.width
        let h = size.height

        // Global time-based breathing (0.70↔1.00 over 2400ms, EaseInOutSine-ish).
        let breathePhase = (now.truncatingRemainder(dividingBy: breathingPeriodMs / 1000)) / (breathingPeriodMs / 1000)
        let breatheOscillator = sin(.pi * breathePhase) // 0→1→0
        let breatheAlpha = breathingOpacityMin + (breathingOpacityMax - breathingOpacityMin) * breatheOscillator

        let shimmerPhase = (now.truncatingRemainder(dividingBy: shimmerPeriodMs / 1000)) / (shimmerPeriodMs / 1000) * (2 * .pi)

        // --- Background gradient ---
        let gradient = Gradient(stops: bgGradientStops.map { Gradient.Stop(color: $0.1, location: $0.0) })
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .linearGradient(gradient, startPoint: .zero, endPoint: CGPoint(x: 0, y: h))
        )

        // --- Vignette (subtle warm falloff toward the edges; kept light to match design) ---
        let vigRadius = max(w, h) * 0.85
        let vigGradient = Gradient(colors: [.clear, Color.black.opacity(0.15)])
        ctx.fill(
            Path(CGRect(origin: .zero, size: size)),
            with: .radialGradient(vigGradient, center: CGPoint(x: w / 2, y: h / 2), startRadius: 0, endRadius: vigRadius)
        )

        // --- Strings area: edge-to-edge, 170pt top margin, to the bottom (Kotlin parity) ---
        let stringTop: CGFloat = stringTopMargin
        let stringBottom = h
        if stringTopPt != stringTop || stringBottomPt != stringBottom {
            let top = stringTop
            let bot = stringBottom
            DispatchQueue.main.async {
                stringTopPt = top
                stringBottomPt = bot
            }
        }

        // Labels float in the gap around 55% height; strings split into upper + lower tail.
        let labelY = h * 0.55
        let gapTop = labelY - 8
        let gapBottom = labelY + 65
        let upperLen = max(0.0, gapTop - stringTop)
        let lowerLen = max(0.0, stringBottom - gapBottom)

        let bandWidth = w / CGFloat(n)

        for i in 0..<n {
            let anim = anims[i]
            let p = physics[i]
            let paletteIdx = (i + (5 - n)) % 5
            let palette = stringPalette[paletteIdx]

            let centerX = bandWidth * (CGFloat(i) + 0.5)

            let vibrationAmp = anim.vibration.value(at: now)
            let colorFactor = anim.color.value(at: now)
            let opacityFactor = anim.opacity.value(at: now)
            let sharpness = anim.sharpness.value(at: now)
            let attack = anim.attack.value(at: now)
            let reveal = anim.reveal.value(at: now)

            let stringColor = lerpColor(palette.idle, palette.active, CGFloat(colorFactor))
            let glowColor = palette.glow

            let isActive = vibrationAmp > 0.01
            let effectiveAlpha: CGFloat = isActive
                ? CGFloat(opacityFactor)
                : CGFloat(opacityFactor) * CGFloat(breatheAlpha)

            let currentThick = lerpF(p.idleThickPt, p.activeThickPt, CGFloat(colorFactor))
            let maxAmpPx = p.vibAmpPt * CGFloat(vibrationAmp)
            let shimmerAmpPx: CGFloat = 0.3

            let breathFactor: CGFloat = isActive
                ? CGFloat(1 + p.breathingAmplitude * sin(2 * .pi * now / p.breathingPeriodS))
                : 1

            let glowAlpha: CGFloat = isActive ? 0.45 : 0.08
            let coreWidth = currentThick * breathFactor
            let blurWidth = coreWidth * (isActive ? blurWidthRatio : 1.15)
            let hazeWidth = coreWidth * (isActive ? hazeWidthRatio : 1.5)

            // Upper segment: haze, blur, core
            if upperLen > 0.0 {
                drawStringPath(ctx: &ctx, centerX: centerX, stringTop: stringTop, stringLength: upperLen,
                               amplitude: maxAmpPx, shimmerAmp: isActive ? 0 : shimmerAmpPx, shimmerPhase: shimmerPhase,
                               wavePhase: now, wavePeaks: p.vibWavePeaks, waveFreqHz: p.vibFreqHz,
                               color: glowColor, alpha: glowAlpha * 0.15 * effectiveAlpha, strokeWidth: hazeWidth,
                               revealProgress: CGFloat(reveal))
                drawStringPath(ctx: &ctx, centerX: centerX, stringTop: stringTop, stringLength: upperLen,
                               amplitude: maxAmpPx, shimmerAmp: isActive ? 0 : shimmerAmpPx, shimmerPhase: shimmerPhase,
                               wavePhase: now, wavePeaks: p.vibWavePeaks, waveFreqHz: p.vibFreqHz,
                               color: glowColor, alpha: glowAlpha * 0.40 * effectiveAlpha, strokeWidth: blurWidth,
                               revealProgress: CGFloat(reveal))
                drawStringPath(ctx: &ctx, centerX: centerX, stringTop: stringTop, stringLength: upperLen,
                               amplitude: maxAmpPx, shimmerAmp: isActive ? 0 : shimmerAmpPx, shimmerPhase: shimmerPhase,
                               wavePhase: now, wavePeaks: p.vibWavePeaks, waveFreqHz: p.vibFreqHz,
                               color: stringColor, alpha: effectiveAlpha, strokeWidth: coreWidth,
                               sharpness: sharpness, touchYNorm: anim.touchYNorm, attackProgress: attack,
                               revealProgress: CGFloat(reveal), isWound: p.isWound)
            }

            // Lower tail: haze, blur, core
            if lowerLen > 0.0 {
                drawStringPath(ctx: &ctx, centerX: centerX, stringTop: gapBottom, stringLength: lowerLen,
                               amplitude: maxAmpPx, shimmerAmp: isActive ? 0 : shimmerAmpPx, shimmerPhase: shimmerPhase,
                               wavePhase: now, wavePeaks: p.vibWavePeaks, waveFreqHz: p.vibFreqHz,
                               color: glowColor, alpha: glowAlpha * 0.15 * effectiveAlpha, strokeWidth: hazeWidth,
                               revealProgress: CGFloat(reveal))
                drawStringPath(ctx: &ctx, centerX: centerX, stringTop: gapBottom, stringLength: lowerLen,
                               amplitude: maxAmpPx, shimmerAmp: isActive ? 0 : shimmerAmpPx, shimmerPhase: shimmerPhase,
                               wavePhase: now, wavePeaks: p.vibWavePeaks, waveFreqHz: p.vibFreqHz,
                               color: glowColor, alpha: glowAlpha * 0.40 * effectiveAlpha, strokeWidth: blurWidth,
                               revealProgress: CGFloat(reveal))
                drawStringPath(ctx: &ctx, centerX: centerX, stringTop: gapBottom, stringLength: lowerLen,
                               amplitude: maxAmpPx, shimmerAmp: isActive ? 0 : shimmerAmpPx, shimmerPhase: shimmerPhase,
                               wavePhase: now, wavePeaks: p.vibWavePeaks, waveFreqHz: p.vibFreqHz,
                               color: stringColor, alpha: effectiveAlpha, strokeWidth: coreWidth,
                               sharpness: sharpness, touchYNorm: anim.touchYNorm, attackProgress: attack,
                               revealProgress: CGFloat(reveal), isWound: p.isWound)
            }

            // --- Label (floating on strings, ª style to match target) ---
            let labelColor = palette.label
            let labelAlpha = (isActive ? 1.0 : Double(effectiveAlpha) * 0.85) * reveal
            let ordinal = stringOrdinal(n - i)

            drawStringLabel(ctx: &ctx, primary: notes[i].name, secondary: ordinal,
                            centerX: centerX, primaryY: labelY, color: labelColor,
                            alpha: CGFloat(labelAlpha), colorFactor: CGFloat(colorFactor))
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

            // Amplitude envelope with gaussian bias during attack
            let symEnv = sin(.pi * yNorm)
            let gaussExp = -((yNorm - touchYNorm) * (yNorm - touchYNorm)) / (2 * 0.35 * 0.35)
            let gaussianBias = max(0, symEnv * exp(gaussExp))
            let envelope = symEnv * attackProgress + gaussianBias * (1 - attackProgress)

            // Sine vibration with sharpness shaping
            let vibOffset: CGFloat
            if amplitude > 0.01 {
                let rawSine = sin(2 * .pi * wavePeaks * yNorm + wavePhase * waveFreqHz * 2 * .pi)
                let shapedSine: Double = sharpness >= 2.9
                    ? rawSine
                    : (rawSine >= 0 ? 1 : -1) * pow(abs(rawSine), 1 / sharpness)
                vibOffset = amplitude * CGFloat(envelope) * CGFloat(shapedSine)
            } else {
                vibOffset = 0
            }

            // Wound-string micro-texture (D3/G3 only)
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

        ctx.stroke(
            path,
            with: .color(color.opacity(Double(alpha.clamped(to: 0...1)))),
            style: StrokeStyle(lineWidth: strokeWidth, lineJoin: .round)
        )
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

        let primarySp: CGFloat = 20 + (24 - 20) * colorFactor.clamped(to: 0...1)
        let secondarySp: CGFloat = 13 + (15 - 13) * colorFactor.clamped(to: 0...1)

        let primaryText = Text(primary)
            .font(.system(size: primarySp, weight: .medium))
            .foregroundColor(color.opacity(Double(alpha.clamped(to: 0...1))))
            .kerning(0.8)

        let secondaryText = Text(secondary)
            .font(.system(size: secondarySp, weight: .regular))
            .foregroundColor(color.opacity(Double((alpha * 0.85).clamped(to: 0...1))))
            .kerning(0.3)

        let resolvedPrimary = ctx.resolve(primaryText)
        let resolvedSecondary = ctx.resolve(secondaryText)

        let primarySize = resolvedPrimary.measure(in: CGSize(width: 200, height: 200))
        let secondarySize = resolvedSecondary.measure(in: CGSize(width: 200, height: 200))

        ctx.draw(resolvedPrimary, at: CGPoint(x: centerX - primarySize.width / 2, y: primaryY), anchor: .topLeading)
        ctx.draw(resolvedSecondary, at: CGPoint(x: centerX - secondarySize.width / 2, y: primaryY + primarySize.height + 1.5), anchor: .topLeading)
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
        .frame(maxWidth: .infinity, maxHeight: .infinity)
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
