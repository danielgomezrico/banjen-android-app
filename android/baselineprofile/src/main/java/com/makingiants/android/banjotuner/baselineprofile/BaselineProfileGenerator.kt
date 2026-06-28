package com.makingiants.android.banjotuner.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Captures the classes/methods used during cold start of [EarActivity] so they
 * are AOT-compiled at install time (ART baseline profile). Regenerate with:
 *
 *   ./gradlew :app:generateBaselineProfile
 *
 * and commit the refreshed app/src/release/generated/baselineProfiles/ output.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() =
        rule.collect("com.makingiants.android.banjotuner") {
            pressHome()
            startActivityAndWait()
            // Let first composition, the tuning animation, and the ad slot settle
            // so their hot paths land in the profile too.
            device.waitForIdle()
            Thread.sleep(3_000)
        }
}
