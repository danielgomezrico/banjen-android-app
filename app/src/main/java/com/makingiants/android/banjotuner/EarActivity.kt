package com.makingiants.android.banjotuner

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch
import java.io.IOException

class EarActivity : AppCompatActivity() {
    @VisibleForTesting
    internal val player by lazy { SoundPlayer(this) }

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private var savedPitch: Int = DEFAULT_PITCH

    private val buttonsText =
        listOf(
            R.string.ear_button_4_text,
            R.string.ear_button_3_text,
            R.string.ear_button_2_text,
            R.string.ear_button_1_text,
        )

    @VisibleForTesting
    internal val clickAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.shake_animation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedPitch = prefs.getInt(KEY_REFERENCE_PITCH, DEFAULT_PITCH)
        player.pitchRatio = calculatePitchRatio(savedPitch)

        MobileAds.initialize(this)
        setContent { Contents() }
    }

    override fun onPause() {
        player.stop()
        super.onPause()
    }

    @Composable
    @Preview
    fun Contents() {
        MaterialTheme(
            colorScheme =
                lightColorScheme(
                    primary = colorResource(id = R.color.banjen_primary),
                    onPrimary = colorResource(id = R.color.banjen_accent),
                    secondary = colorResource(id = R.color.banjen_gray),
                    onSecondary = colorResource(id = R.color.banjen_accent),
                    background = colorResource(id = R.color.banjen_background),
                    onBackground = colorResource(id = R.color.banjen_accent),
                    surface = colorResource(id = R.color.banjen_gray_light),
                    onSurface = colorResource(id = R.color.banjen_accent),
                    error = Color.Blue,
                    onError = Color.White,
                ),
        ) {
            MainLayout()
        }
    }

    @Composable
    fun MainLayout() {
        val snackbarHostState = remember { SnackbarHostState() }
        val referencePitch = remember { mutableIntStateOf(savedPitch) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = colorResource(id = R.color.banjen_background),
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val selectedOption = remember { mutableIntStateOf(-1) }
                    val isVolumeLow = remember { mutableStateOf(false) }

                    PitchControl(referencePitch) { newPitch ->
                        prefs.edit().putInt(KEY_REFERENCE_PITCH, newPitch).apply()
                        player.pitchRatio = calculatePitchRatio(newPitch)
                        if (player.isPlaying) {
                            val currentIndex = buttonsText.indexOf(selectedOption.value)
                            if (currentIndex >= 0) {
                                try {
                                    player.playWithLoop(currentIndex)
                                } catch (e: IOException) {
                                    Log.e("EarActivity", "Restarting sound with new pitch", e)
                                }
                            }
                        }
                    }

                    buttonsText.forEachIndexed { index, text ->
                        Button(index, text, selectedOption, isVolumeLow, snackbarHostState)
                    }

                    AdView()
                }
            }
        }
    }

    @Composable
    private fun PitchControl(
        referencePitch: MutableState<Int>,
        onPitchChanged: (Int) -> Unit,
    ) {
        val pitchLabel = stringResource(id = R.string.reference_pitch_label)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
        ) {
            IconButton(
                onClick = {
                    val newPitch = clampPitch(referencePitch.value - 1)
                    referencePitch.value = newPitch
                    onPitchChanged(newPitch)
                },
                enabled = canDecreasePitch(referencePitch.value),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "$pitchLabel -1",
                    tint = colorResource(id = R.color.banjen_accent),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "A=${referencePitch.value}",
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.banjen_accent),
                        textAlign = TextAlign.Center,
                    ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val newPitch = clampPitch(referencePitch.value + 1)
                    referencePitch.value = newPitch
                    onPitchChanged(newPitch)
                },
                enabled = canIncreasePitch(referencePitch.value),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "$pitchLabel +1",
                    tint = colorResource(id = R.color.banjen_accent),
                )
            }
        }
    }

    @Composable
    private fun AdView() {
        val adsAppId = stringResource(id = R.string.ads_unit_id_banner)

        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    adUnitId = adsAppId
                    setAdSize(AdSize.BANNER)
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier.wrapContentSize(),
        )
    }

    @Composable
    private fun ColumnScope.Button(
        index: Int,
        text: Int,
        selectedOption: MutableState<Int>,
        isVolumeLow: MutableState<Boolean>,
        snackbarHostState: SnackbarHostState,
    ) {
        val isSelected = selectedOption.value == text
        val scope = rememberCoroutineScope()
        val volumeLowMessage = stringResource(id = R.string.volume_low_message)

        val scaleAnimation by animateFloatAsState(
            targetValue = if (isSelected) 3f else 1f,
            label = "scale animation",
        )
        val shakeAnimation by rememberInfiniteTransition(label = "infinite").animateFloat(
            initialValue = if (isSelected) -10f else 0f,
            targetValue = if (isSelected) 10f else 0f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(100, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "shake animation",
        )

        val showVolumeIcon = isSelected && isVolumeLow.value
        val iconShakeAnimation =
            if (showVolumeIcon) {
                rememberInfiniteTransition(label = "icon-infinite")
                    .animateFloat(
                        initialValue = -5f,
                        targetValue = 5f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(100, easing = FastOutLinearInEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                        label = "icon shake animation",
                    ).value
            } else {
                0f
            }

        TextButton(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scaleAnimation,
                        scaleY = scaleAnimation,
                        translationX = shakeAnimation,
                    ),
            onClick = {
                val selectedValue = selectedOption.value

                if (selectedValue != text) {
                    selectedOption.value = text
                    isVolumeLow.value = player.isVolumeLow()

                    try {
                        player.playWithLoop(index)
                    } catch (e: IOException) {
                        Log.e("EarActivity", "Playing sound", e)
                    }
                } else {
                    player.stop()
                    selectedOption.value = -1
                    isVolumeLow.value = false
                }
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showVolumeIcon) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(volumeLowMessage)
                            }
                        },
                        modifier =
                            Modifier
                                .size(48.dp)
                                .graphicsLayer(translationX = iconShakeAnimation),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = volumeLowMessage,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = getString(text),
                    style =
                        TextStyle(
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.banjen_accent),
                            textAlign = TextAlign.Center,
                        ),
                )
            }
        }
    }
}
