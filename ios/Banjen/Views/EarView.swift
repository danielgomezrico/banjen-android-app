import SwiftUI
import BanjenCore

struct EarView: View {
    @State private var viewModel = EarViewModel()
    @State private var showSettings = false
    @State private var pitchCaptureEngine: PitchCaptureEngine?
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        VStack(spacing: 0) {
            // ── Tuning surface: canvas + all overlays, fills remaining height ──
            ZStack {
                // Background extends under status bar
                AppColors.background.ignoresSafeArea()

                // Canvas fills the full ZStack area
                BanjoStringCanvas(
                    notes: viewModel.currentTuning.notes,
                    selectedString: viewModel.selectedStringIndex,
                    onStringSelected: { index in
                        guard !viewModel.sessionModeActive else { return }
                        if viewModel.pitchCheckMode {
                            viewModel.pitchCheckMode = false
                            viewModel.pitchResult = nil
                            pitchCaptureEngine?.stop()
                            pitchCaptureEngine = nil
                        }
                        viewModel.selectString(index)
                    }
                )
                .ignoresSafeArea() // strings bleed full-screen; overlay row below stays inset

                // Top overlay row + volume-low banner, stacked inside the canvas ZStack
                VStack(spacing: 0) {
                    CanvasOverlayRow(
                        isSessionActive: viewModel.sessionModeActive,
                        isStringActive: viewModel.selectedStringIndex >= 0,
                        onSessionClick: { viewModel.startSessionMode() },
                        onSettingsClick: { showSettings = true }
                    )
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)

                    Spacer()

                    if viewModel.isVolumeLow {
                        VolumeLowBanner()
                            .padding(.bottom, 12)
                    }
                }

                // Session stop FAB — bottom trailing, inside canvas area
                if viewModel.sessionModeActive {
                    VStack {
                        Spacer()
                        HStack {
                            Spacer()
                            StopSessionButton { viewModel.exitSessionMode() }
                                .padding(.trailing, 16)
                                .padding(.bottom, 20)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // ── Ad banner: fixed 50pt slot BELOW the canvas, never overlaps it ──
            AdBannerView()
                .frame(height: 50)
                // Keep height reserved at all times (no layout shift); just fade content
                .opacity(viewModel.selectedStringIndex == -1 ? 1 : 0)
        }
        // Dark background behind EVERYTHING (incl. home-indicator area and the ad slot)
        // so no white shows, and fading the ad while playing reveals dark, not white.
        .background(AppColors.background.ignoresSafeArea())
        .sheet(isPresented: $showSettings) {
            SettingsSheet(viewModel: viewModel)
                .presentationDetents([.medium])
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .background || newPhase == .inactive {
                viewModel.onBackground()
                pitchCaptureEngine?.stop()
                pitchCaptureEngine = nil
            }
        }
        .onChange(of: viewModel.pitchCheckMode) { _, active in
            if active && viewModel.selectedStringIndex >= 0 {
                let freq = viewModel.currentTuning.notes[safe: viewModel.selectedStringIndex]?.frequency ?? 0
                let engine = PitchCaptureEngine(targetFrequency: Double(freq)) { [weak viewModel] result in
                    viewModel?.pitchResult = result
                }
                pitchCaptureEngine = engine
                engine.start()
            } else {
                pitchCaptureEngine?.stop()
                pitchCaptureEngine = nil
                viewModel.pitchResult = nil
            }
        }
        .onOpenURL { url in
            handleDeepLink(url)
        }
        .onDisappear {
            viewModel.release()
        }
    }

    // MARK: - Deep link

    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "banjen", url.host == "play",
              let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              let indexStr = components.queryItems?.first(where: { $0.name == "string" })?.value,
              let index = Int(indexStr)
        else { return }
        viewModel.onAutoplay(stringIndex: index)
    }
}

// MARK: - CanvasOverlayRow

private struct CanvasOverlayRow: View {
    let isSessionActive: Bool
    let isStringActive: Bool
    let onSessionClick: () -> Void
    let onSettingsClick: () -> Void

    var body: some View {
        HStack {
            if !isSessionActive {
                PillIconButton(
                    image: "PlayIcon",
                    label: String(localized: "session_mode_label"),
                    action: onSessionClick
                )
            } else {
                Color.clear.frame(width: 40, height: 40)
            }

            Spacer()

            Text("BANJEN")
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(AppColors.wordmark)
                .tracking(4)

            Spacer()

            PillIconButton(
                image: "GearIcon",
                label: String(localized: "settings_label"),
                action: onSettingsClick
            )
        }
        .opacity(isStringActive ? 0.35 : 1.0)
        .animation(.easeInOut(duration: 0.3), value: isStringActive)
    }
}

// MARK: - PillIconButton

private struct PillIconButton: View {
    let image: String
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(image)
                .resizable()
                .scaledToFit()
                .frame(width: 20, height: 20)
                .foregroundColor(AppColors.wordmark)
                .frame(width: 40, height: 40)
                .background(AppColors.pillBg)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(AppColors.pillBorder, lineWidth: 1))
        }
        .buttonStyle(PressScaleButtonStyle())
        .accessibilityLabel(label)
    }
}

// Press-scale feedback via ButtonStyle (no competing gesture → taps stay reliable).
private struct PressScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.92 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}

// MARK: - VolumeLowBanner

private struct VolumeLowBanner: View {
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "speaker.slash.fill")
                .foregroundColor(AppColors.accent)
            Text(String(localized: "volume_low_message"))
                .font(.caption)
                .foregroundColor(AppColors.wordmark)
                .multilineTextAlignment(.leading)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(AppColors.pillBg)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(AppColors.pillBorder, lineWidth: 1)
        )
        .padding(.horizontal, 16)
    }
}

// MARK: - StopSessionButton

private struct StopSessionButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image("StopIcon")
                .resizable()
                .scaledToFit()
                .frame(width: 20, height: 20)
                .foregroundColor(AppColors.background)
                .frame(width: 56, height: 56)
                .background(AppColors.accent)
                .clipShape(Circle())
                .shadow(color: .black.opacity(0.3), radius: 4, x: 0, y: 2)
        }
        .accessibilityLabel(String(localized: "session_stop"))
    }
}
