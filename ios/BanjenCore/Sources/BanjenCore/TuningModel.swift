import Foundation

public struct Note: Sendable, Equatable {
    public let name: String
    public let frequency: Float

    public init(_ name: String, _ frequency: Float) {
        self.name = name
        self.frequency = frequency
    }
}

public struct Tuning: Sendable, Equatable {
    public let name: String
    public let notes: [Note]

    public init(name: String, notes: [Note]) {
        self.name = name
        self.notes = notes
    }
}

public struct Instrument: Sendable, Equatable {
    public let name: String
    public let tunings: [Tuning]

    public init(name: String, tunings: [Tuning]) {
        self.name = name
        self.tunings = tunings
    }
}

public func noteFrequency(_ midiNote: Int) -> Float {
    return 440.0 * pow(2.0, Float(midiNote - 69) / 12.0)
}

public func encodeTuning(_ tuning: Tuning) -> String {
    let notesPart = tuning.notes.map { "\($0.name):\($0.frequency)" }.joined(separator: ",")
    return "\(tuning.name)|\(notesPart)"
}

public func decodeTuning(_ encoded: String) -> Tuning? {
    let parts = encoded.split(separator: "|", maxSplits: 1).map(String.init)
    guard parts.count == 2 else { return nil }
    let name = parts[0]
    let noteStrings = parts[1].split(separator: ",").map(String.init)
    let notes: [Note] = noteStrings.compactMap { noteStr in
        let noteParts = noteStr.split(separator: ":", maxSplits: 1).map(String.init)
        guard noteParts.count == 2, let freq = Float(noteParts[1]) else { return nil }
        return Note(noteParts[0], freq)
    }
    guard !notes.isEmpty else { return nil }
    return Tuning(name: name, notes: notes)
}

public let FOUR_STRING_BANJO = Instrument(
    name: "4-String Banjo",
    tunings: [
        Tuning(name: "Standard DGBD", notes: [
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        ]),
        Tuning(name: "Irish GDAE", notes: [
            Note("G3", noteFrequency(55)),
            Note("D4", noteFrequency(62)),
            Note("A4", noteFrequency(69)),
            Note("E5", noteFrequency(76)),
        ]),
        Tuning(name: "Chicago DGBE", notes: [
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("E4", noteFrequency(64)),
        ]),
        Tuning(name: "Plectrum CGBD", notes: [
            Note("C3", noteFrequency(48)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        ]),
    ]
)

public let FIVE_STRING_BANJO = Instrument(
    name: "5-String Banjo",
    tunings: [
        Tuning(name: "Open G (gDGBD)", notes: [
            Note("g4", noteFrequency(67)),
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        ]),
        Tuning(name: "Double C (gCGCD)", notes: [
            Note("g4", noteFrequency(67)),
            Note("C3", noteFrequency(48)),
            Note("G3", noteFrequency(55)),
            Note("C4", noteFrequency(60)),
            Note("D4", noteFrequency(62)),
        ]),
        Tuning(name: "Modal (gDGCD)", notes: [
            Note("g4", noteFrequency(67)),
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("C4", noteFrequency(60)),
            Note("D4", noteFrequency(62)),
        ]),
        Tuning(name: "Drop C (gCGBD)", notes: [
            Note("g4", noteFrequency(67)),
            Note("C3", noteFrequency(48)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        ]),
        Tuning(name: "Open D (f#DF#AD)", notes: [
            Note("f#4", noteFrequency(66)),
            Note("D3", noteFrequency(50)),
            Note("F#3", noteFrequency(54)),
            Note("A3", noteFrequency(57)),
            Note("D4", noteFrequency(62)),
        ]),
    ]
)

public let ALL_INSTRUMENTS: [Instrument] = [FOUR_STRING_BANJO, FIVE_STRING_BANJO]
