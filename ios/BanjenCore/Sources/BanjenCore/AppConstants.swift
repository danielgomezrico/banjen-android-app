import Foundation

public let DEFAULT_PITCH: Int = 440
public let MIN_PITCH: Int = 432
public let MAX_PITCH: Int = 446
public let PREFS_NAME: String = "banjen_prefs"
public let KEY_REFERENCE_PITCH: String = "reference_pitch"
public let KEY_INSTRUMENT_INDEX: String = "instrument_index"
public let KEY_TUNING_INDEX: String = "tuning_index"

public func calculatePitchRatio(_ referencePitch: Int) -> Float {
    return Float(referencePitch) / 440.0
}

public func clampPitch(_ pitch: Int) -> Int {
    return min(MAX_PITCH, max(MIN_PITCH, pitch))
}

public func canDecreasePitch(_ pitch: Int) -> Bool {
    return pitch > MIN_PITCH
}

public func canIncreasePitch(_ pitch: Int) -> Bool {
    return pitch < MAX_PITCH
}

public let SECONDS_PER_STRING: Int = 5

public func clampVolume(_ v: Float) -> Float {
    return min(1.0, max(0.0, v))
}

public func autoAdvanceNextIndex(_ current: Int) -> Int? {
    return current < 3 ? current + 1 : nil
}
