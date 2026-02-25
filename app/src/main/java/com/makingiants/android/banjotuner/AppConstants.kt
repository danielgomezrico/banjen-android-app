package com.makingiants.android.banjotuner

const val DEFAULT_PITCH = 440
const val MIN_PITCH = 432
const val MAX_PITCH = 446
const val PREFS_NAME = "banjen_prefs"
const val KEY_REFERENCE_PITCH = "reference_pitch"

fun calculatePitchRatio(referencePitch: Int): Float = referencePitch / 440f

fun clampPitch(pitch: Int): Int = pitch.coerceIn(MIN_PITCH, MAX_PITCH)

fun canDecreasePitch(pitch: Int): Boolean = pitch > MIN_PITCH

fun canIncreasePitch(pitch: Int): Boolean = pitch < MAX_PITCH

const val KEY_SESSION_VOLUME = "session_volume"
const val DEFAULT_SESSION_VOLUME = 0.3f
const val SECONDS_PER_STRING = 5

fun clampVolume(v: Float): Float = v.coerceIn(0.0f, 1.0f)

fun autoAdvanceNextIndex(current: Int): Int? = if (current < 3) current + 1 else null
