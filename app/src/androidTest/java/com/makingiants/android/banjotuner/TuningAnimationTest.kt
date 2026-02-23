package com.makingiants.android.banjotuner

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TuningAnimation composable.
 * Tests rendering in all 7 animation states, state transitions, and string selection.
 *
 * The composable accepts State<> parameters and a Modifier. We pass
 * Modifier.testTag(ANIMATION_TAG) to make the Canvas findable in the semantic tree.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class TuningAnimationTest {

    companion object {
        private const val ANIMATION_TAG = "tuning_animation"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Helper: render animation with given state inputs ---

    private fun renderAnimationInState(
        selectedOption: Int = -1,
        pitchCheckMode: Boolean = false,
        pitchResult: PitchResult? = null,
        isVolumeLow: Boolean = false,
    ) {
        composeTestRule.setContent {
            TuningAnimation(
                selectedOption = mutableIntStateOf(selectedOption),
                pitchCheckMode = mutableStateOf(pitchCheckMode),
                pitchResult = mutableStateOf(pitchResult),
                isVolumeLow = mutableStateOf(isVolumeLow),
                modifier = Modifier.testTag(ANIMATION_TAG),
            )
        }
        composeTestRule.waitForIdle()
    }

    // --- Render smoke tests: each state renders without crash ---

    @Test
    fun renders_idle_state_without_crash() {
        renderAnimationInState(selectedOption = -1)
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    @Test
    fun renders_stringSelected_state_without_crash() {
        renderAnimationInState(selectedOption = 0)
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    @Test
    fun renders_noSignal_state_without_crash() {
        renderAnimationInState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 0.0,
                targetHz = 146.83,
                centDeviation = 0.0,
                status = TuningStatus.NO_SIGNAL,
            ),
        )
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    @Test
    fun renders_flat_state_without_crash() {
        renderAnimationInState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 130.0,
                targetHz = 146.83,
                centDeviation = -30.0,
                status = TuningStatus.FLAT,
            ),
        )
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    @Test
    fun renders_sharp_state_without_crash() {
        renderAnimationInState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 165.0,
                targetHz = 146.83,
                centDeviation = 30.0,
                status = TuningStatus.SHARP,
            ),
        )
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    @Test
    fun renders_close_state_without_crash() {
        renderAnimationInState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 149.0,
                targetHz = 146.83,
                centDeviation = 15.0,
                status = TuningStatus.CLOSE,
            ),
        )
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    @Test
    fun renders_inTune_state_without_crash() {
        renderAnimationInState(
            selectedOption = 0,
            pitchCheckMode = true,
            pitchResult = PitchResult(
                detectedHz = 147.0,
                targetHz = 146.83,
                centDeviation = 5.0,
                status = TuningStatus.IN_TUNE,
            ),
        )
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    // --- String selection tests: all 4 strings render without crash ---

    @Test
    fun renders_eachStringIndex_without_crash() {
        for (stringIndex in 0..3) {
            composeTestRule.setContent {
                TuningAnimation(
                    selectedOption = mutableIntStateOf(stringIndex),
                    pitchCheckMode = mutableStateOf(false),
                    pitchResult = mutableStateOf(null),
                    isVolumeLow = mutableStateOf(false),
                    modifier = Modifier.testTag(ANIMATION_TAG),
                )
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
        }
    }

    // --- String transition test: switching strings doesn't crash ---

    @Test
    fun stringTransition_switchBetweenStrings_noCrash() {
        val selectedOption = mutableIntStateOf(0)

        composeTestRule.setContent {
            TuningAnimation(
                selectedOption = selectedOption,
                pitchCheckMode = mutableStateOf(false),
                pitchResult = mutableStateOf(null),
                isVolumeLow = mutableStateOf(false),
                modifier = Modifier.testTag(ANIMATION_TAG),
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()

        // Transition through all strings
        for (stringIndex in 1..3) {
            composeTestRule.runOnUiThread {
                selectedOption.intValue = stringIndex
            }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
        }

        // Back to idle
        composeTestRule.runOnUiThread {
            selectedOption.intValue = -1
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    // --- State transition test: cycle through all states ---

    @Test
    fun stateTransitions_fullCycle_noCrash() {
        val selectedOption = mutableIntStateOf(-1)
        val pitchCheckMode = mutableStateOf(false)
        val pitchResult = mutableStateOf<PitchResult?>(null)

        composeTestRule.setContent {
            TuningAnimation(
                selectedOption = selectedOption,
                pitchCheckMode = pitchCheckMode,
                pitchResult = pitchResult,
                isVolumeLow = mutableStateOf(false),
                modifier = Modifier.testTag(ANIMATION_TAG),
            )
        }

        // IDLE
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()

        // → STRING_SELECTED
        composeTestRule.runOnUiThread { selectedOption.intValue = 0 }
        composeTestRule.waitForIdle()

        // → NO_SIGNAL
        composeTestRule.runOnUiThread {
            pitchCheckMode.value = true
            pitchResult.value = PitchResult(0.0, 146.83, 0.0, TuningStatus.NO_SIGNAL)
        }
        composeTestRule.waitForIdle()

        // → FLAT
        composeTestRule.runOnUiThread {
            pitchResult.value = PitchResult(130.0, 146.83, -30.0, TuningStatus.FLAT)
        }
        composeTestRule.waitForIdle()

        // → CLOSE
        composeTestRule.runOnUiThread {
            pitchResult.value = PitchResult(145.0, 146.83, -15.0, TuningStatus.CLOSE)
        }
        composeTestRule.waitForIdle()

        // → IN_TUNE
        composeTestRule.runOnUiThread {
            pitchResult.value = PitchResult(147.0, 146.83, 5.0, TuningStatus.IN_TUNE)
        }
        composeTestRule.waitForIdle()

        // → SHARP (pitch worsens)
        composeTestRule.runOnUiThread {
            pitchResult.value = PitchResult(165.0, 146.83, 30.0, TuningStatus.SHARP)
        }
        composeTestRule.waitForIdle()

        // → back to IDLE
        composeTestRule.runOnUiThread {
            selectedOption.intValue = -1
            pitchCheckMode.value = false
            pitchResult.value = null
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }

    // --- Volume low state test ---

    @Test
    fun renders_withVolumeLow_noCrash() {
        renderAnimationInState(
            selectedOption = 0,
            isVolumeLow = true,
        )
        composeTestRule.onNodeWithTag(ANIMATION_TAG).assertExists()
    }
}

/**
 * Integration tests that verify the TuningAnimation doesn't break existing EarActivity layout.
 * Tests layout stability (DEALBREAKER #1) and button accessibility.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class TuningAnimationLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<EarActivity>()

    // --- DEALBREAKER #1: Layout stability — buttons don't move when animation changes state ---

    @Test
    fun buttons_positionUnchanged_afterStringSelection() {
        composeTestRule.waitForIdle()

        // Capture button bounds in IDLE state
        val button1BoundsIdle = composeTestRule.onNodeWithTag("button_1").getBoundsInRoot()
        val button2BoundsIdle = composeTestRule.onNodeWithTag("button_2").getBoundsInRoot()
        val button3BoundsIdle = composeTestRule.onNodeWithTag("button_3").getBoundsInRoot()
        val button4BoundsIdle = composeTestRule.onNodeWithTag("button_4").getBoundsInRoot()

        // Select string 1 → animation transitions to STRING_SELECTED
        withEarRobot(composeTestRule) {
            click(1)
        }
        composeTestRule.waitForIdle()

        // Re-capture button bounds
        val button1BoundsAfter = composeTestRule.onNodeWithTag("button_1").getBoundsInRoot()
        val button2BoundsAfter = composeTestRule.onNodeWithTag("button_2").getBoundsInRoot()
        val button3BoundsAfter = composeTestRule.onNodeWithTag("button_3").getBoundsInRoot()
        val button4BoundsAfter = composeTestRule.onNodeWithTag("button_4").getBoundsInRoot()

        // Assert layout positions unchanged (top and left are the layout-relevant values)
        assertEquals(button1BoundsIdle.top, button1BoundsAfter.top)
        assertEquals(button1BoundsIdle.left, button1BoundsAfter.left)
        assertEquals(button2BoundsIdle.top, button2BoundsAfter.top)
        assertEquals(button2BoundsIdle.left, button2BoundsAfter.left)
        assertEquals(button3BoundsIdle.top, button3BoundsAfter.top)
        assertEquals(button3BoundsIdle.left, button3BoundsAfter.left)
        assertEquals(button4BoundsIdle.top, button4BoundsAfter.top)
        assertEquals(button4BoundsIdle.left, button4BoundsAfter.left)
    }

    @Test
    fun buttons_positionUnchanged_afterStringTransition() {
        composeTestRule.waitForIdle()

        // Select string 1
        withEarRobot(composeTestRule) { click(1) }
        composeTestRule.waitForIdle()

        // Capture bounds with string 1 selected
        val button1BoundsBefore = composeTestRule.onNodeWithTag("button_1").getBoundsInRoot()
        val button2BoundsBefore = composeTestRule.onNodeWithTag("button_2").getBoundsInRoot()

        // Switch to string 2 → animation transitions between STRING_SELECTED states
        withEarRobot(composeTestRule) { click(2) }
        composeTestRule.waitForIdle()

        val button1BoundsAfter = composeTestRule.onNodeWithTag("button_1").getBoundsInRoot()
        val button2BoundsAfter = composeTestRule.onNodeWithTag("button_2").getBoundsInRoot()

        assertEquals(button1BoundsBefore.top, button1BoundsAfter.top)
        assertEquals(button1BoundsBefore.left, button1BoundsAfter.left)
        assertEquals(button2BoundsBefore.top, button2BoundsAfter.top)
        assertEquals(button2BoundsBefore.left, button2BoundsAfter.left)
    }

    // --- Button test tags still accessible with animation present ---

    @Test
    fun allButtonTestTags_stillExist() {
        composeTestRule.waitForIdle()
        for (i in 1..4) {
            composeTestRule.onNodeWithTag("button_$i").assertExists()
        }
    }

    // --- Existing EarRobot interaction still works ---

    @Test
    fun existingInteraction_selectAndDeselectString_works() {
        withEarRobot(composeTestRule) {
            click(1)
        }.assert {
            checkIsPlaying()
        }

        withEarRobot(composeTestRule) {
            click(1)
        }.assert {
            checkIsNotPlaying()
        }
    }

    @Test
    fun existingInteraction_allStringsPlayable() {
        for (i in 1..4) {
            withEarRobot(composeTestRule) {
                click(i)
            }.assert {
                checkIsPlaying()
            }
            // Deselect before next string
            withEarRobot(composeTestRule) {
                click(i)
            }.assert {
                checkIsNotPlaying()
            }
        }
    }
}
