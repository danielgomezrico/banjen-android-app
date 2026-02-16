package com.makingiants.android.banjotuner

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class EarActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<EarActivity>()

    fun test_isPlaying(index: Int) = withEarRobot(composeTestRule) {
        click(index)
    }.assert {
        checkIsPlaying()
    }

    fun test_stopsPlaying(index: Int) = withEarRobot(composeTestRule) {
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
