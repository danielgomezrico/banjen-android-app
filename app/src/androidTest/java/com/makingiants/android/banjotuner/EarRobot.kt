package com.makingiants.android.banjotuner


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


class EarRobot(val activity: Activity) {

    init {
        // animations that repeat forever break espresso
        (activity as EarActivity).clickAnimation.repeatCount = 0
    }

    fun click(buttonIndex: Int) {
        onView(withText(Matchers.startsWith("$buttonIndex"))).perform(ViewActions.click())
    }

    fun assert(func: Assert.() -> Unit) = Assert().apply { func() }

    class Assert {
        private val context: Context get() = ApplicationProvider.getApplicationContext()
        private val audioService by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

        fun checkIsPlaying() {
            assertTrue(audioService.isMusicActive)
        }

        fun checkIsNotPlaying() {
            assertFalse(audioService.isMusicActive)
        }
    }

}

fun withEarRobot(activity: Activity, func: EarRobot.() -> Unit) = EarRobot(activity).apply { func() }