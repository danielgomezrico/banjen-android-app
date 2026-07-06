import Foundation

public enum TuningAnimationState: Sendable, Equatable {
    case idle
    case stringSelected
    case noSignal
    case flat
    case sharp
    case close
    case inTune
}

public func deriveTuningAnimationState(
    selectedOption: Int,
    pitchCheckMode: Bool,
    pitchResult: PitchResult?
) -> TuningAnimationState {
    if selectedOption < 0 { return .idle }
    if !pitchCheckMode { return .stringSelected }
    guard let result = pitchResult else { return .noSignal }
    switch result.status {
    case .noSignal: return .noSignal
    case .flat: return .flat
    case .sharp: return .sharp
    case .close: return .close
    case .inTune: return .inTune
    }
}

public func beatFrequencyHz(_ centDeviation: Float) -> Float {
    let absCents = abs(centDeviation)
    let beatCentsDivisor: Float = 5.0
    let beatHzMin: Float = 0.5
    let beatHzMax: Float = 6.0
    return min(beatHzMax, max(beatHzMin, absCents / beatCentsDivisor))
}

public func ringAsymmetryOffset(_ centDeviation: Float) -> Float {
    let maxAsymmetryFraction: Float = 0.15
    return min(maxAsymmetryFraction, max(-maxAsymmetryFraction, centDeviation / 50.0))
}
