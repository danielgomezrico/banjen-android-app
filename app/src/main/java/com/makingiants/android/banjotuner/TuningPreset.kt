package com.makingiants.android.banjotuner

enum class TuningPreset(
    val displayName: String,
    val noteNames: List<String>,
    val assetFiles: List<String>,
) {
    STANDARD("Standard (DGBD)", listOf("D", "G", "B", "D"), listOf("1.mp3", "2.mp3", "3.mp3", "4.mp3")),
    IRISH("Irish (GDAE)", listOf("G", "D", "A", "E"), listOf("2.mp3", "1.mp3", "a3.wav", "e4.wav")),
    PLECTRUM("Plectrum (CGBD)", listOf("C", "G", "B", "D"), listOf("c3.wav", "2.mp3", "3.mp3", "4.mp3")),
    CHICAGO("Chicago (DGBE)", listOf("D", "G", "B", "E"), listOf("1.mp3", "2.mp3", "3.mp3", "e4.wav")),
    ;

    fun buttonLabel(index: Int): String = "${4 - index} - ${noteNames[index]}"
}
