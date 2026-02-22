package com.makingiants.android.banjotuner

import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
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

    private val buttonsText =
        listOf(
            R.string.ear_button_4_text,
            R.string.ear_button_3_text,
            R.string.ear_button_2_text,
            R.string.ear_button_1_text,
        )

    private val buttonsSubtitle =
        listOf(
            R.string.ear_button_4_subtitle,
            R.string.ear_button_3_subtitle,
            R.string.ear_button_2_subtitle,
            R.string.ear_button_1_subtitle,
        )

    private val buttonsDescription =
        listOf(
            R.string.ear_button_4_description,
            R.string.ear_button_3_description,
            R.string.ear_button_2_description,
            R.string.ear_button_1_description,
        )

    @VisibleForTesting
    internal val clickAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.shake_animation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    buttonsText.forEachIndexed { index, text ->
                        Button(
                            index,
                            text,
                            buttonsSubtitle[index],
                            buttonsDescription[index],
                            selectedOption,
                            isVolumeLow,
                            snackbarHostState,
                        )
                    }

                    AdView()
                }
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
        subtitle: Int,
        description: Int,
        selectedOption: MutableState<Int>,
        isVolumeLow: MutableState<Boolean>,
        snackbarHostState: SnackbarHostState,
    ) {
        val isSelected = selectedOption.value == text
        val scope = rememberCoroutineScope()
        val volumeLowMessage = stringResource(id = R.string.volume_low_message)
        val buttonDescription = stringResource(id = description)

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
                    .defaultMinSize(minHeight = 48.dp)
                    .graphicsLayer(
                        scaleX = scaleAnimation,
                        scaleY = scaleAnimation,
                        translationX = shakeAnimation,
                    ).semantics { contentDescription = buttonDescription },
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = text),
                        style =
                            TextStyle(
                                fontSize = 24.sp,
                                color = colorResource(id = R.color.banjen_accent),
                                textAlign = TextAlign.Center,
                            ),
                        maxLines = 1,
                    )
                    AnimatedVisibility(visible = !isSelected) {
                        Text(
                            text = stringResource(id = subtitle),
                            style =
                                TextStyle(
                                    fontSize = 14.sp,
                                    color = colorResource(id = R.color.banjen_gray_light),
                                    textAlign = TextAlign.Center,
                                ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
