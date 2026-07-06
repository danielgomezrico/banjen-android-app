import Foundation

public let TONE_SAMPLE_RATE: Int = 44100
public let AMPLITUDE_SCALE: Float = 0.7

public func generateSineWaveSamples(frequency: Float, sampleRate: Int, numSamples: Int) -> [Int16] {
    var samples = [Int16](repeating: 0, count: numSamples)
    let twoPiF = 2.0 * Double.pi * Double(frequency)
    for i in 0 ..< numSamples {
        let t = Double(i) / Double(sampleRate)
        let value = sin(twoPiF * t)
        samples[i] = Int16(Int(value * Double(Int16.max) * Double(AMPLITUDE_SCALE)))
    }
    return samples
}

public func calculateLoopSampleCount(frequency: Float, sampleRate: Int) -> Int {
    if frequency <= 0 { return sampleRate }
    let samplesPerCycle = Float(sampleRate) / frequency
    let cycles = Int((Float(sampleRate) / samplesPerCycle).rounded())
    let nominal = Int((Float(cycles) * samplesPerCycle).rounded())
    let halfPeriod = max(1, Int(samplesPerCycle / 2))
    let twoPiFOverFs = 2.0 * Double.pi * Double(frequency) / Double(sampleRate)
    // Minimize 1-cos instead of |sin|: cos=1 only at complete cycles, while |sin|=0
    // at both complete AND half cycles. A half-cycle boundary reverses waveform phase
    // on every loop restart, causing an audible thump.
    let range = (nominal - halfPeriod) ... (nominal + halfPeriod)
    let best = range.min { a, b in
        let va = a <= 0 ? Double.greatestFiniteMagnitude : (1.0 - cos(twoPiFOverFs * Double(a)))
        let vb = b <= 0 ? Double.greatestFiniteMagnitude : (1.0 - cos(twoPiFOverFs * Double(b)))
        return va < vb
    }
    return best ?? nominal
}
