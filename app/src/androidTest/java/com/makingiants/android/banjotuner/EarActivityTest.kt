package com.makingiants.android.banjotuner

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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

    fun test_isPlaying(index: Int) =
        withEarRobot(composeTestRule) {
            click(index)
        }.assert {
            checkIsPlaying()
        }

    fun test_stopsPlaying(index: Int) =
        withEarRobot(composeTestRule) {
            click(index)
            click(index)
        }.assert {
            checkIsNotPlaying()
        }

    @Test
    fun test_onClick_ifUnselected_playSound() =
        (1..4).forEach {
            test_isPlaying(it)
        }

    @Test
    fun test_onClick_ifSelected_stopSound() =
        (1..4).forEach {
            test_stopsPlaying(it)
        }

    @Test
    fun test_buttonsShowSubtitleText() {
        val subtitles =
            listOf(
                "String 4 (thickest)",
                "String 3",
                "String 2",
                "String 1 (thinnest)",
            )
        subtitles.forEach { subtitle ->
            composeTestRule.onNodeWithText(subtitle).assertExists()
        }
    }

    @Test
    fun test_buttonsHaveContentDescriptions() {
        val descriptions =
            listOf(
                "String 4, note D, thickest. Tap to play.",
                "String 3, note G. Tap to play.",
                "String 2, note B. Tap to play.",
                "String 1, note D, thinnest. Tap to play.",
            )
        descriptions.forEach { description ->
            composeTestRule.onNode(hasContentDescription(description)).assertExists()
        }
    }
}
