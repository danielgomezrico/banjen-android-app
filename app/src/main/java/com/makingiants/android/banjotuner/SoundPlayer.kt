package com.makingiants.android.banjotuner

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.PlaybackParams

const val DEFAULT_PITCH = 440
const val MIN_PITCH = 432
const val MAX_PITCH = 446
const val PREFS_NAME = "banjen_prefs"
const val KEY_REFERENCE_PITCH = "reference_pitch"

fun calculatePitchRatio(referencePitch: Int): Float = referencePitch / 440f

fun clampPitch(pitch: Int): Int = pitch.coerceIn(MIN_PITCH, MAX_PITCH)

fun canDecreasePitch(pitch: Int): Boolean = pitch > MIN_PITCH

fun canIncreasePitch(pitch: Int): Boolean = pitch < MAX_PITCH

// SESSION_PREFS_NAME unified with PREFS_NAME above
const val KEY_SESSION_VOLUME = "session_volume"
const val DEFAULT_SESSION_VOLUME = 0.3f
const val SECONDS_PER_STRING = 5

fun clampVolume(v: Float): Float = v.coerceIn(0.0f, 1.0f)

fun autoAdvanceNextIndex(current: Int): Int? = if (current < 3) current + 1 else null

private val HEADPHONE_TYPES =
    setOf(
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_USB_HEADSET,
    )

/**
 * Sound player for android
 */
class SoundPlayer(
    private val context: Context,
) : OnPreparedListener,
    OnCompletionListener {
    private var mediaPlayer: MediaPlayer? = null
    val isPlaying: Boolean get() = mediaPlayer?.isPlaying == true
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var pitchRatio: Float = 1.0f
    var volume: Float = 1.0f

    fun isVolumeLow(): Boolean {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return current < max / 2
    }

    private val soundsPath = "b_sounds"
    private val sounds = arrayOf("1.mp3", "2.mp3", "3.mp3", "4.mp3")

    /**
     * Play the sound in path and if there are any sound playing it will stop
     * and play the new one if there are no sounds playing now, else stop the
     * sound

     * @param index file to play
     * *
     * @throws java.io.IOException
     */
    fun playWithLoop(index: Int) {
        if (mediaPlayer?.isPlaying == true) {
            stop()
        }

        val afd = context.assets.openFd("$soundsPath/${sounds[index]}")

        mediaPlayer =
            MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setVolume(volume, volume)
                setOnPreparedListener(this@SoundPlayer)
                setOnCompletionListener(this@SoundPlayer)

                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

                prepareAsync()
                isLooping = true
            }

        afd.close()
    }

    /**
     * Reset the player with a mute to avoid
     * a cut sound on reset.
     */
    fun stop() {
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)

        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }

        mediaPlayer = null
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
    }

    fun isHeadphoneConnected(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any { it.type in HEADPHONE_TYPES }
    }

    // <editor-fold desc="OnPreparedListener, OnCompletionListener implements">

    override fun onPrepared(player: MediaPlayer) {
        mediaPlayer?.start()
        mediaPlayer?.playbackParams = PlaybackParams().setPitch(pitchRatio)
    }

    override fun onCompletion(arg0: MediaPlayer) {
        //        mediaPlayer.reset();
    }
    // </editor-fold>
}
