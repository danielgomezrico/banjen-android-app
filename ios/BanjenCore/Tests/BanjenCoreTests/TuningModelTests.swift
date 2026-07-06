import Testing
import Darwin
@testable import BanjenCore

@Suite("TuningModel")
struct TuningModelTests {

    // MARK: noteFrequency

    @Test func noteFrequency_A4_midi69_is440() {
        #expect(abs(noteFrequency(69) - 440.0) < 0.001)
    }

    @Test func noteFrequency_D3_midi50() {
        // D3 = 440 * 2^((50-69)/12) ≈ 146.83 Hz
        let expected: Float = 440.0 * pow(2.0, Float(50 - 69) / 12.0)
        #expect(abs(noteFrequency(50) - expected) < 0.001)
    }

    @Test func noteFrequency_G3_midi55() {
        let expected: Float = 440.0 * pow(2.0, Float(55 - 69) / 12.0)
        #expect(abs(noteFrequency(55) - expected) < 0.001)
    }

    // MARK: encodeTuning / decodeTuning

    @Test func encodeDecode_roundTrip() {
        let tuning = FOUR_STRING_BANJO.tunings[0]
        let encoded = encodeTuning(tuning)
        let decoded = decodeTuning(encoded)
        #expect(decoded != nil)
        #expect(decoded?.name == tuning.name)
        #expect(decoded?.notes.count == tuning.notes.count)
        for (a, b) in zip(decoded!.notes, tuning.notes) {
            #expect(a.name == b.name)
            #expect(abs(a.frequency - b.frequency) < 0.01)
        }
    }

    @Test func decodeTuning_invalidString_returnsNil() {
        #expect(decodeTuning("") == nil)
        #expect(decodeTuning("NoSeparator") == nil)
        #expect(decodeTuning("|") == nil)
        #expect(decodeTuning("Name|badnote") == nil)
    }

    // MARK: Catalog — 4-string

    @Test func fourStringBanjo_name() {
        #expect(FOUR_STRING_BANJO.name == "4-String Banjo")
    }

    @Test func fourStringBanjo_tuningCount() {
        #expect(FOUR_STRING_BANJO.tunings.count == 4)
    }

    @Test func fourStringBanjo_standardDGBD_noteCount() {
        #expect(FOUR_STRING_BANJO.tunings[0].notes.count == 4)
    }

    @Test func fourStringBanjo_standardDGBD_midiValues() {
        let notes = FOUR_STRING_BANJO.tunings[0].notes
        let expectedMidis = [50, 55, 59, 62]
        for (note, midi) in zip(notes, expectedMidis) {
            let expected = noteFrequency(midi)
            #expect(abs(note.frequency - expected) < 0.001, "note \(note.name) expected freq for midi \(midi)")
        }
    }

    @Test func fourStringBanjo_standardDGBD_noteNames() {
        let notes = FOUR_STRING_BANJO.tunings[0].notes
        #expect(notes[0].name == "D3")
        #expect(notes[1].name == "G3")
        #expect(notes[2].name == "B3")
        #expect(notes[3].name == "D4")
    }

    // MARK: Catalog — 5-string

    @Test func fiveStringBanjo_name() {
        #expect(FIVE_STRING_BANJO.name == "5-String Banjo")
    }

    @Test func fiveStringBanjo_tuningCount() {
        #expect(FIVE_STRING_BANJO.tunings.count == 5)
    }

    @Test func fiveStringBanjo_openG_noteCount() {
        #expect(FIVE_STRING_BANJO.tunings[0].notes.count == 5)
    }

    // MARK: allInstruments

    @Test func allInstruments_count() {
        #expect(ALL_INSTRUMENTS.count == 2)
    }

    @Test func allInstruments_order() {
        #expect(ALL_INSTRUMENTS[0].name == "4-String Banjo")
        #expect(ALL_INSTRUMENTS[1].name == "5-String Banjo")
    }
}
