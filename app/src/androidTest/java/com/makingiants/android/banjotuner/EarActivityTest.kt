package com.makingiants.android.banjotuner

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class EarActivityTest {

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(EarActivity::class.java)

    fun test_isPlaying(index: Int) = withEarRobot(activityRule.activity) {
        click(index)
    }.assert {
        checkIsPlaying()
    }

    fun test_stopsPlaying(index: Int) = withEarRobot(activityRule.activity) {
        click(index)
        click(index)
    }.assert {
        checkIsNotPlaying()
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

