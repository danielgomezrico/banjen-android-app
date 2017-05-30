package com.makingiants.android.banjotuner


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.jraska.falcon.FalconSpoon
import org.hamcrest.Matchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class EarRobot(val activity: Activity) {

  val audioService = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

  fun click(buttonIndex: Int) {
    FalconSpoon.screenshot(activity, "before-click-$buttonIndex")
    onView(withText(Matchers.startsWith("$buttonIndex"))).perform(ViewActions.click())
    FalconSpoon.screenshot(activity, "after-click-$buttonIndex")
  }

  fun isPlaying() {
    FalconSpoon.screenshot(activity, "isPLaying")
    assertTrue(audioService.isMusicActive)
  }

  fun isNotPlaying() {
    FalconSpoon.screenshot(activity, "isNotPlaying")
    assertFalse(audioService.isMusicActive)
  }

}

fun withEarRobot(activity: Activity, func: EarRobot.() -> Unit) = EarRobot(activity).apply { func() }