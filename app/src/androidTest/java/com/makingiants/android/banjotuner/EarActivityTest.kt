package com.makingiants.android.banjotuner

import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class EarActivityTest {

  @Rule @JvmField
  var mActivityRule: ActivityTestRule<EarActivity> = ActivityTestRule(EarActivity::class.java)

  fun test_isPlaying(index: Int) {
    withEarRobot(mActivityRule.activity) {
      click(index)
    }.isPlaying()
  }

  fun test_stopsPlaying(index: Int) {
    withEarRobot(mActivityRule.activity) {
      click(index)
      click(index)
    }.isNotPlaying()
  }

  @Test
  fun test_onClick_ifUnselected_playSound() = (1..4).forEach {
    test_isPlaying(it)
  }

  @Test
  fun test_onClick_ifSelected_stopSound() = (1..4).forEach {
    test_stopsPlaying(it)
  }

}

