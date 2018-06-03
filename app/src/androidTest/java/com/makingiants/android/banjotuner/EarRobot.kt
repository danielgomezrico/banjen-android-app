package com.makingiants.android.banjotuner


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.jraska.falcon.FalconSpoonRule
import org.hamcrest.Matchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule



class EarRobot(val activity: Activity) {

  @Rule
  val falconSpoonRule = FalconSpoonRule()

  val audioService by lazy { activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

  init {
    // animations that repeat forever break espresso
    (activity as EarActivity).clickAnimation.repeatCount = 0
  }

  fun click(buttonIndex: Int) {
    falconSpoonRule.screenshot(activity, "before-click-$buttonIndex")
    onView(withText(Matchers.startsWith("$buttonIndex"))).perform(ViewActions.click())
    falconSpoonRule.screenshot(activity, "after-click-$buttonIndex")
  }

  fun checkIsPlaying() {
    falconSpoonRule.screenshot(activity, "isPLaying")
    assertTrue(audioService.isMusicActive)
  }

  fun checkIsNotPlaying() {
    falconSpoonRule.screenshot(activity, "checkIsNotPlaying")
    assertFalse(audioService.isMusicActive)
  }

}

fun withEarRobot(activity: Activity, func: EarRobot.() -> Unit) = EarRobot(activity).apply { func() }