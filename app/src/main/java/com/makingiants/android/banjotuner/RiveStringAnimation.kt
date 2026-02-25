package com.makingiants.android.banjotuner

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView

private const val STATE_MACHINE_NAME = "TuningStateMachine"
private const val ARTBOARD_NAME = "TuningRings"

/**
 * Rive-backed tuning animation composable.
 *
 * Wraps [RiveAnimationView] in [AndroidView] and pushes [TuningAnimationState]
 * to the Rive state machine via number/boolean inputs. Falls back gracefully
 * if the .riv resource fails to load.
 *
 * @param riveResId raw resource ID for the .riv file (from [resolveRiveResource])
 * @param animState current tuning animation state (mapped to tuningState input 0-6)
 * @param selectedString currently selected string index (-1 = none, 0-3 = DGBD)
 * @param centDeviation pitch deviation in cents for wobble/beat animations
 * @param isVolumeLow whether device volume is below 50%
 * @param onLoadFailed callback invoked if the .riv file fails to load
 */
@Composable
internal fun RiveTuningAnimation(
    riveResId: Int,
    animState: TuningAnimationState,
    selectedString: Int,
    centDeviation: Float,
    isVolumeLow: Boolean,
    onLoadFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var loaded by remember { mutableStateOf(false) }

    AndroidView(
        factory = { context ->
            RiveAnimationView(context).also { view ->
                try {
                    view.setRiveResource(
                        resId = riveResId,
                        artboardName = ARTBOARD_NAME,
                        stateMachineName = STATE_MACHINE_NAME,
                        autoplay = true,
                    )
                    loaded = true
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    onLoadFailed()
                }
            }
        },
        update = { view ->
            if (loaded) {
                view.setNumberState(
                    STATE_MACHINE_NAME,
                    "tuningState",
                    animState.ordinal.toFloat(),
                )
                view.setNumberState(
                    STATE_MACHINE_NAME,
                    "stringIndex",
                    selectedString.toFloat(),
                )
                view.setNumberState(
                    STATE_MACHINE_NAME,
                    "centDeviation",
                    centDeviation,
                )
                view.setBooleanState(
                    STATE_MACHINE_NAME,
                    "isVolumeLow",
                    isVolumeLow,
                )
            }
        },
        modifier = modifier,
    )
}

/**
 * Resolves the raw resource ID for the Rive tuning animation file.
 * Returns 0 if the file does not exist in res/raw/, allowing the build
 * to compile without a .riv file present.
 */
internal fun resolveRiveResource(context: Context): Int = context.resources.getIdentifier("tuning_animation", "raw", context.packageName)
