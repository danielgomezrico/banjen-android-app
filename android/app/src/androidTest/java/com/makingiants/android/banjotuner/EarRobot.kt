package com.makingiants.android.banjotuner

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class EarRobot(
    private val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<EarActivity>, EarActivity>,
) {
    fun click(buttonIndex: Int) {
        composeTestRule
            .onNodeWithTag("button_$buttonIndex")
            .performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
    }

    fun assert(func: Assert.() -> Unit) = Assert(composeTestRule.activity).apply { func() }

    class Assert(
        private val activity: EarActivity,
    ) {
        fun checkIsPlaying() {
            waitForPlayer(playing = true)
            assertTrue(activity.toneGenerator.isPlaying)
        }

        fun checkIsNotPlaying() {
            waitForPlayer(playing = false)
            assertFalse(activity.toneGenerator.isPlaying)
        }

        private fun waitForPlayer(
            playing: Boolean,
            timeoutMs: Long = 3000,
        ) {
            val start = System.currentTimeMillis()
            while (activity.toneGenerator.isPlaying != playing) {
                if (System.currentTimeMillis() - start > timeoutMs) break
                Thread.sleep(100)
            }
        }
    }
}

fun withEarRobot(
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<EarActivity>, EarActivity>,
    func: EarRobot.() -> Unit,
) = EarRobot(composeTestRule).apply {
    func()
}
