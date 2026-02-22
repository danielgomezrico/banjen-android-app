package com.makingiants.android.banjotuner

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.abs
import kotlin.math.roundToInt

class EarActivity : AppCompatActivity() {
    @VisibleForTesting
    internal val player by lazy { SoundPlayer(this) }

    private val pitchDetector by lazy { PitchDetector() }

    private val buttonsText =
        listOf(
            R.string.ear_button_4_text,
            R.string.ear_button_3_text,
            R.string.ear_button_2_text,
            R.string.ear_button_1_text,
        )

    private val banjoStrings =
        listOf(
            BanjoString.D3,
            BanjoString.G3,
            BanjoString.B3,
            BanjoString.D4,
        )

    @VisibleForTesting
    internal val clickAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.shake_animation)
    }

    private var audioRecord: AudioRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        setContent { Contents() }
    }

    override fun onPause() {
        player.stop()
        stopAudioCapture()
        super.onPause()
    }

    private fun stopAudioCapture() {
        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {
        }
        audioRecord?.release()
        audioRecord = null
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
        val pitchCheckMode = remember { mutableStateOf(false) }
        val pitchResult = remember { mutableStateOf<PitchResult?>(null) }
        val selectedStringIndex = remember { mutableIntStateOf(-1) }

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
                            selectedOption,
                            isVolumeLow,
                            snackbarHostState,
                            pitchCheckMode,
                            pitchResult,
                            selectedStringIndex,
                        )
                    }

                    if (pitchCheckMode.value) {
                        TuningIndicator(pitchResult.value)
                    }

                    AdView()
                }
            }
        }

        // Audio capture effect
        if (pitchCheckMode.value && selectedStringIndex.intValue >= 0) {
            val targetString = banjoStrings[selectedStringIndex.intValue]
            AudioCaptureEffect(targetString, pitchResult)
        }
    }

    @Composable
    private fun AudioCaptureEffect(
        targetString: BanjoString,
        pitchResult: MutableState<PitchResult?>,
    ) {
        val sampleRate = 44100
        val bufferSize = 4096

        DisposableEffect(targetString) {
            onDispose {
                stopAudioCapture()
            }
        }

        LaunchedEffect(targetString) {
            withContext(Dispatchers.IO) {
                val minBufferSize =
                    AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                    )
                val actualBufferSize = maxOf(bufferSize, minBufferSize)

                val recorder =
                    try {
                        AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_FLOAT,
                            actualBufferSize * 4,
                        )
                    } catch (e: SecurityException) {
                        Log.e("EarActivity", "Microphone permission not granted", e)
                        return@withContext
                    }

                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    recorder.release()
                    return@withContext
                }

                audioRecord = recorder
                recorder.startRecording()

                val buffer = FloatArray(bufferSize)
                while (isActive) {
                    val read = recorder.read(buffer, 0, bufferSize, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        val detectedHz = pitchDetector.detectPitch(buffer)
                        val result =
                            if (detectedHz > 0) {
                                val cents = pitchDetector.centsFromTarget(detectedHz, targetString.frequencyHz)
                                PitchResult(
                                    detectedHz = detectedHz,
                                    targetHz = targetString.frequencyHz,
                                    centDeviation = cents,
                                    status = pitchDetector.classifyTuning(cents),
                                )
                            } else {
                                PitchResult(
                                    detectedHz = 0.0,
                                    targetHz = targetString.frequencyHz,
                                    centDeviation = 0.0,
                                    status = TuningStatus.NO_SIGNAL,
                                )
                            }
                        withContext(Dispatchers.Main) {
                            pitchResult.value = result
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TuningIndicator(result: PitchResult?) {
        val accentColor = colorResource(id = R.color.banjen_accent)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (result == null || result.status == TuningStatus.NO_SIGNAL) {
                Text(
                    text = stringResource(R.string.no_signal),
                    style = TextStyle(fontSize = 16.sp, color = accentColor),
                )
            } else {
                val indicatorColor =
                    when (result.status) {
                        TuningStatus.IN_TUNE -> Color(0xFF4CAF50)
                        TuningStatus.CLOSE -> Color(0xFFFFC107)
                        TuningStatus.SHARP, TuningStatus.FLAT -> Color(0xFFF44336)
                        TuningStatus.NO_SIGNAL -> Color.Gray
                    }

                // Direction arrow
                if (result.status == TuningStatus.SHARP || (result.status == TuningStatus.CLOSE && result.centDeviation > 0)) {
                    Text(
                        text = stringResource(R.string.tune_down),
                        style = TextStyle(fontSize = 14.sp, color = indicatorColor),
                    )
                } else if (result.status == TuningStatus.FLAT || (result.status == TuningStatus.CLOSE && result.centDeviation < 0)) {
                    Text(
                        text = stringResource(R.string.tune_up),
                        style = TextStyle(fontSize = 14.sp, color = indicatorColor),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Colored indicator bar
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .background(indicatorColor, RoundedCornerShape(6.dp)),
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status text
                val statusText =
                    when (result.status) {
                        TuningStatus.IN_TUNE -> stringResource(R.string.in_tune)
                        else -> {
                            val cents = abs(result.centDeviation).roundToInt()
                            val direction = if (result.centDeviation > 0) "+" else "-"
                            "${direction}$cents cents"
                        }
                    }

                Text(
                    text = statusText,
                    style =
                        TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = indicatorColor,
                        ),
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
        pitchCheckMode: MutableState<Boolean>,
        pitchResult: MutableState<PitchResult?>,
        selectedStringIndex: MutableState<Int>,
    ) {
        val isSelected = selectedOption.value == text
        val scope = rememberCoroutineScope()
        val volumeLowMessage = stringResource(id = R.string.volume_low_message)

        val permissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                if (granted) {
                    player.stop()
                    pitchCheckMode.value = true
                    pitchResult.value = null
                }
            }

        val scaleAnimation by animateFloatAsState(
            targetValue = if (isSelected && !pitchCheckMode.value) 3f else 1f,
            label = "scale animation",
        )
        val shakeAnimation by rememberInfiniteTransition(label = "infinite").animateFloat(
            initialValue = if (isSelected && !pitchCheckMode.value) -10f else 0f,
            targetValue = if (isSelected && !pitchCheckMode.value) 10f else 0f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(100, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "shake animation",
        )

        val showVolumeIcon = isSelected && isVolumeLow.value && !pitchCheckMode.value
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
                // If in pitch check mode, exit it and resume tone
                if (pitchCheckMode.value) {
                    pitchCheckMode.value = false
                    pitchResult.value = null
                    stopAudioCapture()

                    val selectedValue = selectedOption.value
                    if (selectedValue != text) {
                        selectedOption.value = text
                        selectedStringIndex.value = index
                        isVolumeLow.value = player.isVolumeLow()
                        try {
                            player.playWithLoop(index)
                        } catch (e: IOException) {
                            Log.e("EarActivity", "Playing sound", e)
                        }
                    } else {
                        // Resume playing the same string
                        try {
                            player.playWithLoop(index)
                        } catch (e: IOException) {
                            Log.e("EarActivity", "Playing sound", e)
                        }
                    }
                    return@TextButton
                }

                val selectedValue = selectedOption.value

                if (selectedValue != text) {
                    selectedOption.value = text
                    selectedStringIndex.value = index
                    isVolumeLow.value = player.isVolumeLow()

                    try {
                        player.playWithLoop(index)
                    } catch (e: IOException) {
                        Log.e("EarActivity", "Playing sound", e)
                    }
                } else {
                    player.stop()
                    selectedOption.value = -1
                    selectedStringIndex.value = -1
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
                // Show mic icon for pitch check when this string is selected and playing
                if (isSelected && !pitchCheckMode.value) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val hasPermission =
                                ContextCompat.checkSelfPermission(
                                    this@EarActivity,
                                    Manifest.permission.RECORD_AUDIO,
                                ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                player.stop()
                                pitchCheckMode.value = true
                                pitchResult.value = null
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = stringResource(R.string.check_tuning_button),
                            tint = colorResource(id = R.color.banjen_accent),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                // Show stop checking button when in pitch check mode for this string
                if (isSelected && pitchCheckMode.value) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stop_checking),
                        style =
                            TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFFF44336),
                            ),
                    )
                }
            }
        }
    }
}
