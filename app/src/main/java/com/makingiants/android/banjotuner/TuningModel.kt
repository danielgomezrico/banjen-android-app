package com.makingiants.android.banjotuner

import kotlin.math.pow

data class Note(val name: String, val frequency: Float)

data class Tuning(val name: String, val notes: List<Note>)

data class Instrument(val name: String, val tunings: List<Tuning>)

fun noteFrequency(midiNote: Int): Float = 440.0f * 2.0f.pow((midiNote - 69) / 12.0f)

fun encodeTuning(tuning: Tuning): String {
    val notesPart = tuning.notes.joinToString(",") { "${it.name}:${it.frequency}" }
    return "${tuning.name}|$notesPart"
}

fun decodeTuning(encoded: String): Tuning? {
    val parts = encoded.split("|")
    if (parts.size != 2) return null
    val name = parts[0]
    val noteStrings = parts[1].split(",")
    val notes = noteStrings.mapNotNull { noteStr ->
        val noteParts = noteStr.split(":")
        if (noteParts.size != 2) return@mapNotNull null
        val freq = noteParts[1].toFloatOrNull() ?: return@mapNotNull null
        Note(noteParts[0], freq)
    }
    if (notes.isEmpty()) return null
    return Tuning(name, notes)
}

val FOUR_STRING_BANJO = Instrument(
    name = "4-String Banjo",
    tunings = listOf(
        Tuning("Standard DGBD", listOf(
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        )),
        Tuning("Irish GDAE", listOf(
            Note("G3", noteFrequency(55)),
            Note("D4", noteFrequency(62)),
            Note("A4", noteFrequency(69)),
            Note("E5", noteFrequency(76)),
        )),
        Tuning("Chicago DGBE", listOf(
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("E4", noteFrequency(64)),
        )),
        Tuning("Plectrum CGBD", listOf(
            Note("C3", noteFrequency(48)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        )),
    ),
)

val FIVE_STRING_BANJO = Instrument(
    name = "5-String Banjo",
    tunings = listOf(
        Tuning("Open G (gDGBD)", listOf(
            Note("g4", noteFrequency(67)),
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        )),
        Tuning("Double C (gCGCD)", listOf(
            Note("g4", noteFrequency(67)),
            Note("C3", noteFrequency(48)),
            Note("G3", noteFrequency(55)),
            Note("C4", noteFrequency(60)),
            Note("D4", noteFrequency(62)),
        )),
        Tuning("Modal (gDGCD)", listOf(
            Note("g4", noteFrequency(67)),
            Note("D3", noteFrequency(50)),
            Note("G3", noteFrequency(55)),
            Note("C4", noteFrequency(60)),
            Note("D4", noteFrequency(62)),
        )),
        Tuning("Drop C (gCGBD)", listOf(
            Note("g4", noteFrequency(67)),
            Note("C3", noteFrequency(48)),
            Note("G3", noteFrequency(55)),
            Note("B3", noteFrequency(59)),
            Note("D4", noteFrequency(62)),
        )),
        Tuning("Open D (f#DF#AD)", listOf(
            Note("f#4", noteFrequency(66)),
            Note("D3", noteFrequency(50)),
            Note("F#3", noteFrequency(54)),
            Note("A3", noteFrequency(57)),
            Note("D4", noteFrequency(62)),
        )),
    ),
)

val ALL_INSTRUMENTS = listOf(FOUR_STRING_BANJO, FIVE_STRING_BANJO)
