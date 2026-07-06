import SwiftUI

extension Color {
    init(hex: UInt32, alpha: Double = 1.0) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b, opacity: alpha)
    }
}

enum AppColors {
    // Background
    static let background = Color(hex: 0xFFFBE9)
    static let settingsBackground = Color(hex: 0xFFFBE9)

    // Chrome
    static let accent = Color(hex: 0x6B9AB8)
    static let wordmark = Color(hex: 0xFFFBE9)
    static let pillBg = Color(hex: 0x49251E)
    static let pillBorder = Color(hex: 0x6B4035)
    static let dropdownLabel = Color(hex: 0xFFFBE9)
    static let divider = Color(hex: 0x49251E)

    // String palette (idle / active / label) — 5 entries indexed by paletteIdx
    // paletteIdx = (i + (5 - numStrings)) % 5
    static let stringIdle: [Color] = [
        Color(hex: 0x4F6B7A),
        Color(hex: 0x49251E),
        Color(hex: 0x5A7C90),
        Color(hex: 0x8C6640),
        Color(hex: 0x9F3A0A),
    ]
    static let stringActive: [Color] = [
        Color(hex: 0x9BC8DC),
        Color(hex: 0x8D5746),
        Color(hex: 0xCBE6F7),
        Color(hex: 0xE8B36B),
        Color(hex: 0xFB4F00),
    ]
    static let stringLabel: [Color] = [
        Color(hex: 0x6B8A9A),
        Color(hex: 0xFFFBE9),
        Color(hex: 0x2E2420),
        Color(hex: 0x3D322A),
        Color(hex: 0xFFFBE9),
    ]

    // TuningAnimationView accent colors per string note
    static let animAccentD3 = Color(hex: 0x8D5746)
    static let animAccentG3 = Color(hex: 0xCBE6F7)
    static let animAccentB3 = Color(hex: 0xE8B36B)
    static let animAccentD4 = Color(hex: 0xFB4F00)
    static let animNeutral = Color(hex: 0xFFFBE9)
}
