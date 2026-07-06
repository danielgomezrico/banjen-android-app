import Foundation

public enum TuningStatus: Sendable, Equatable {
    case inTune
    case close
    case sharp
    case flat
    case noSignal
}

public struct PitchResult: Sendable, Equatable {
    public let detectedHz: Double
    public let targetHz: Double
    public let centDeviation: Double
    public let status: TuningStatus

    public init(detectedHz: Double, targetHz: Double, centDeviation: Double, status: TuningStatus) {
        self.detectedHz = detectedHz
        self.targetHz = targetHz
        self.centDeviation = centDeviation
        self.status = status
    }
}

public final class PitchDetector: Sendable {
    private let sampleRate: Int
    private let yinThreshold: Double = 0.15

    public init(sampleRate: Int = 44100) {
        self.sampleRate = sampleRate
    }

    /// Detects the fundamental frequency using the YIN algorithm.
    /// Returns the detected frequency in Hz, or -1.0 if no pitch is detected.
    public func detectPitch(_ samples: [Float]) -> Double {
        if samples.isEmpty { return -1.0 }
        let halfLen = samples.count / 2
        var yinBuffer = [Float](repeating: 0, count: halfLen)

        // Step 1: Difference function
        for tau in 0 ..< halfLen {
            var sum: Float = 0.0
            for i in 0 ..< halfLen {
                let diff = samples[i] - samples[i + tau]
                sum += diff * diff
            }
            yinBuffer[tau] = sum
        }

        // Step 2: Cumulative mean normalized difference
        yinBuffer[0] = 1.0
        var runningSum: Float = 0.0
        for tau in 1 ..< halfLen {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = yinBuffer[tau] * Float(tau) / runningSum
        }

        // Step 3: Absolute threshold — find first dip below threshold, then local minimum
        var tauEstimate = -1
        for tau in 2 ..< halfLen {
            if yinBuffer[tau] < Float(yinThreshold) {
                tauEstimate = tau
                while tauEstimate + 1 < halfLen && yinBuffer[tauEstimate + 1] < yinBuffer[tauEstimate] {
                    tauEstimate += 1
                }
                break
            }
        }

        if tauEstimate == -1 { return -1.0 }

        // Step 4: Parabolic interpolation for sub-sample accuracy
        let refinedTau = parabolicInterpolation(yinBuffer, tau: tauEstimate)

        // Step 5: Convert to frequency
        return Double(sampleRate) / refinedTau
    }

    private func parabolicInterpolation(_ yinBuffer: [Float], tau: Int) -> Double {
        if tau <= 0 || tau >= yinBuffer.count - 1 { return Double(tau) }

        let s0 = Double(yinBuffer[tau - 1])
        let s1 = Double(yinBuffer[tau])
        let s2 = Double(yinBuffer[tau + 1])

        let adjustment = (s2 - s0) / (2.0 * (2.0 * s1 - s2 - s0))
        return Double(tau) + adjustment
    }

    /// Calculates the deviation in cents between detected and target frequencies.
    /// Negative = flat, positive = sharp.
    public func centsFromTarget(detected: Double, target: Double) -> Double {
        if detected <= 0 || target <= 0 { return 0.0 }
        return 1200.0 * (log(detected / target) / log(2.0))
    }

    /// Classifies tuning status based on cent deviation.
    public func classifyTuning(_ cents: Double) -> TuningStatus {
        let absCents = abs(cents)
        if absCents <= 10.0 { return .inTune }
        if absCents <= 25.0 { return .close }
        if cents > 0 { return .sharp }
        return .flat
    }
}
