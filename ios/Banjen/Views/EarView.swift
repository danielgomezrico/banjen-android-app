import SwiftUI
import BanjenCore

struct EarView: View {
    @State private var viewModel = EarViewModel()
    @State private var showSettings = false
    @State private var pitchCaptureEngine: PitchCaptureEngine?
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        // Full-screen ZStack (mirrors Android's Box.fillMaxSize): the canvas bleeds to
        // every edge so the strings reach the very bottom and the vignette radius matches.
        ZStack {
            // Background behind everything (incl. status-bar & home-indicator areas)
            AppColors.background.ignoresSafeArea()

            // Canvas fills the entire screen, edge to edge
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
            .ignoresSafeArea()

            // Top overlay row + volume-low banner (respect safe areas — inset below status bar)
            VStack(spacing: 0) {
                CanvasOverlayRow(
                    isSessionActive: viewModel.sessionModeActive,
                    isStringActive: viewModel.selectedStringIndex >= 0,
                    onSessionClick: { viewModel.startSessionMode() },
                    onSettingsClick: { showSettings = true },
                    onStopClick: { viewModel.exitSessionMode() }
                )
                .padding(.horizontal, 12)
                .padding(.vertical, 8)

                Spacer()

                if viewModel.isVolumeLow {
                    VolumeLowBanner()
                        .padding(.bottom, 12)
                }
            }

            // Ad banner floats over the canvas bottom, only when idle (Android parity:
            // aligned bottom-center, 8pt above the safe area — never a layout-shifting slot).
            if viewModel.selectedStringIndex == -1 {
                VStack {
                    Spacer()
                    AdBannerView()
                        .frame(width: 320, height: 50)
                        .padding(.bottom, 8)
                }
            }
        }
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
    let onStopClick: () -> Void

    var body: some View {
        HStack {
            // Single toggle: Play when idle, Stop while a session is running.
            SessionToggleButton(
                isSessionActive: isSessionActive,
                onSession: onSessionClick,
                onStop: onStopClick
            )

            Spacer()

            Text("BANJEN")
                .font(.system(size: 20, weight: .medium))
                .foregroundColor(AppColors.title)
                .tracking(4)

            Spacer()

            PillIconButton(
                image: "GearIcon",
                label: String(localized: "settings_label"),
                action: onSettingsClick
            )
        }
        // Keep chrome fully visible during a session so Stop stays clear; only dim
        // when a single string is manually being played.
        .opacity(isStringActive && !isSessionActive ? 0.35 : 1.0)
        .animation(.easeInOut(duration: 0.3), value: isStringActive)
    }
}

// MARK: - SessionToggleButton
// Play/Stop toggle pill. Cross-fades, scales and rotates between the two icons
// (micro-interaction) as the session starts/stops.
private struct SessionToggleButton: View {
    let isSessionActive: Bool
    let onSession: () -> Void
    let onStop: () -> Void

    var body: some View {
        Button(action: { isSessionActive ? onStop() : onSession() }) {
            ZStack {
                Image("PlayIcon")
                    .resizable().scaledToFit()
                    .frame(width: 24, height: 24)
                    .foregroundColor(AppColors.wordmark)
                    .opacity(isSessionActive ? 0 : 1)
                    .scaleEffect(isSessionActive ? 0.5 : 1)
                    .rotationEffect(.degrees(isSessionActive ? -90 : 0))

                Image("StopIcon")
                    .resizable().scaledToFit()
                    .frame(width: 24, height: 24)
                    .foregroundColor(AppColors.wordmark)
                    .opacity(isSessionActive ? 1 : 0)
                    .scaleEffect(isSessionActive ? 1 : 0.5)
                    .rotationEffect(.degrees(isSessionActive ? 0 : 90))
            }
            .frame(width: 48, height: 48)
            .background(AppColors.pillBg)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(AppColors.pillBorder, lineWidth: 1))
        }
        .buttonStyle(PressScaleButtonStyle())
        .animation(.spring(response: 0.35, dampingFraction: 0.7), value: isSessionActive)
        .accessibilityLabel(isSessionActive
            ? String(localized: "session_stop")
            : String(localized: "session_mode_label"))
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
                .frame(width: 24, height: 24)
                .foregroundColor(AppColors.wordmark)
                .frame(width: 48, height: 48)
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

