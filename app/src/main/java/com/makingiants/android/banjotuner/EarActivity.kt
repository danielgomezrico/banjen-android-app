package com.makingiants.android.banjotuner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.abs
import kotlin.math.roundToInt

private const val KEY_INSTRUMENT_INDEX = "instrument_index"
private const val KEY_TUNING_INDEX = "tuning_index"

class EarActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_STRING_INDEX = "string_index"
        private const val MAX_STRING_INDEX = 4  // 5-string banjo has 5 strings (0..4)
        private const val KEY_TUNING = "banjen_selected_tuning"

        fun parseStringIndex(intent: Intent?): Int {
            val index = intent?.getIntExtra(EXTRA_STRING_INDEX, -1) ?: -1
            return validateStringIndex(index)
        }

        fun validateStringIndex(index: Int): Int = if (index in 0..MAX_STRING_INDEX) index else -1
    }

    @VisibleForTesting
    internal val player by lazy { SoundPlayer(this) }

    @VisibleForTesting
    internal val toneGenerator by lazy { ToneGenerator() }

    private val pitchDetector by lazy { PitchDetector() }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var savedPitch: Int = DEFAULT_PITCH

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

    private val sessionStringNames =
        listOf(
            R.string.session_string_1,
            R.string.session_string_2,
            R.string.session_string_3,
            R.string.session_string_4,
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

    private var sessionModeActive = mutableStateOf(false)
    private var audioRecord: AudioRecord? = null

    private fun loadSavedTuning(): TuningPreset {
        val savedName = prefs.getString(KEY_TUNING, null) ?: return TuningPreset.STANDARD
        return try {
            TuningPreset.valueOf(savedName)
        } catch (_: IllegalArgumentException) {
            TuningPreset.STANDARD
        }
    }

    private fun saveTuning(preset: TuningPreset) {
        prefs.edit().putString(KEY_TUNING, preset.name).apply()
    }

    private fun stopAudioCapture() {
        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {
        }
        audioRecord?.release()
        audioRecord = null
    }

    private fun exitSessionMode() {
        sessionModeActive.value = false
        player.stop()
        player.volume = 1.0f
    }

    private fun shareTuning(tuning: Tuning) {
        val encoded = encodeTuning(tuning)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Banjen Tuning: ${tuning.name}")
            putExtra(Intent.EXTRA_TEXT, "Banjen Tuning: $encoded")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tuning)))
    }

    private fun isVolumeLow(): Boolean {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return current < max / 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedPitch = prefs.getInt(KEY_REFERENCE_PITCH, DEFAULT_PITCH)
        player.pitchRatio = calculatePitchRatio(savedPitch)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (sessionModeActive.value) {
                        exitSessionMode()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        MobileAds.initialize(this)
        val autoPlayIndex = parseStringIndex(intent)
        setContent { Contents(autoPlayIndex) }
    }

    override fun onPause() {
        if (sessionModeActive.value) {
            exitSessionMode()
        }
        player.stop()
        toneGenerator.stop()
        stopAudioCapture()
        super.onPause()
    }

    @Composable
    @Preview
    fun Contents(autoPlayIndex: Int = -1) {
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
            MainLayout(autoPlayIndex)
        }
    }

    @Composable
    fun MainLayout(autoPlayIndex: Int = -1) {
        val snackbarHostState = remember { SnackbarHostState() }
        val referencePitch = remember { mutableIntStateOf(savedPitch) }
        val selectedTuning = remember { mutableStateOf(loadSavedTuning()) }

        if (sessionModeActive.value) {
            SessionModeLayout(snackbarHostState, selectedTuning)
        } else {
            NormalLayout(snackbarHostState, referencePitch, selectedTuning, autoPlayIndex)
        }
    }

    @Composable
    private fun NormalLayout(
        snackbarHostState: SnackbarHostState,
        referencePitch: MutableIntState,
        selectedTuning: MutableState<TuningPreset>,
        autoPlayIndex: Int = -1,
    ) {
        val scope = rememberCoroutineScope()
        val noHeadphonesMsg = stringResource(id = R.string.session_no_headphones)
        val pitchCheckMode = remember { mutableStateOf(false) }
        val pitchResult = remember { mutableStateOf<PitchResult?>(null) }
        val selectedStringIndex = remember { mutableIntStateOf(-1) }

        val savedInstrumentIndex = prefs.getInt(KEY_INSTRUMENT_INDEX, 0)
            .coerceIn(0, ALL_INSTRUMENTS.size - 1)
        val savedTuningModelIndex = prefs.getInt(KEY_TUNING_INDEX, 0)

        var instrumentIndex by remember { mutableIntStateOf(savedInstrumentIndex) }
        var tuningModelIndex by remember {
            mutableIntStateOf(
                savedTuningModelIndex.coerceIn(0, ALL_INSTRUMENTS[savedInstrumentIndex].tunings.size - 1),
            )
        }

        val currentInstrument = ALL_INSTRUMENTS[instrumentIndex]
        val currentTuningModel = currentInstrument.tunings[tuningModelIndex]

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
                    val selectedOption = remember { mutableIntStateOf(if (autoPlayIndex in 0..3) autoPlayIndex else -1) }
                    val isVolumeLow = remember { mutableStateOf(false) }

                    if (autoPlayIndex in 0..3) {
                        LaunchedAutoPlay(autoPlayIndex, isVolumeLow, selectedTuning)
                    }

                    // Instrument/Tuning selector row (5-string support)
                    SelectorRow(
                        instrumentName = currentInstrument.name,
                        tuningName = currentTuningModel.name,
                        instruments = ALL_INSTRUMENTS,
                        onInstrumentSelected = { newIndex ->
                            toneGenerator.stop()
                            player.stop()
                            selectedOption.intValue = -1
                            isVolumeLow.value = false
                            instrumentIndex = newIndex
                            tuningModelIndex = 0
                            prefs.edit()
                                .putInt(KEY_INSTRUMENT_INDEX, newIndex)
                                .putInt(KEY_TUNING_INDEX, 0)
                                .apply()
                        },
                        tunings = currentInstrument.tunings,
                        onTuningSelected = { newIndex ->
                            toneGenerator.stop()
                            player.stop()
                            selectedOption.intValue = -1
                            isVolumeLow.value = false
                            tuningModelIndex = newIndex
                            prefs.edit()
                                .putInt(KEY_TUNING_INDEX, newIndex)
                                .apply()
                        },
                        onShareTuning = { shareTuning(currentTuningModel) },
                    )

                    // 4-string TuningPreset selector (for MP3-based reference tones)
                    TuningSelector(selectedTuning, selectedOption)

                    PitchControl(referencePitch) { newPitch ->
                        prefs.edit().putInt(KEY_REFERENCE_PITCH, newPitch).apply()
                        player.pitchRatio = calculatePitchRatio(newPitch)
                        if (player.isPlaying) {
                            val currentIndex = selectedOption.intValue
                            if (currentIndex >= 0) {
                                try {
                                    player.playWithLoop(selectedTuning.value.assetFiles[currentIndex])
                                } catch (e: IOException) {
                                    Log.e("EarActivity", "Restarting sound with new pitch", e)
                                }
                            }
                        }
                    }

                    SessionModeToggle {
                        if (!player.isHeadphoneConnected()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(noHeadphonesMsg)
                            }
                        }
                        val savedVol = prefs.getFloat(KEY_SESSION_VOLUME, DEFAULT_SESSION_VOLUME)
                        player.volume = savedVol
                        player.stop()
                        selectedOption.intValue = -1
                        sessionModeActive.value = true
                    }

                    // Dynamic note buttons from TuningModel (supports 4-string and 5-string)
                    currentTuningModel.notes.forEachIndexed { index, note ->
                        val subtitle = if (index < buttonsSubtitle.size) buttonsSubtitle[index] else R.string.ear_button_1_subtitle
                        val description = if (index < buttonsDescription.size) buttonsDescription[index] else R.string.ear_button_1_description
                        NoteButton(
                            index = index,
                            note = note,
                            subtitle = subtitle,
                            description = description,
                            selectedOption = selectedOption,
                            isVolumeLow = isVolumeLow,
                            snackbarHostState = snackbarHostState,
                            pitchCheckMode = pitchCheckMode,
                            pitchResult = pitchResult,
                            selectedStringIndex = selectedStringIndex,
                        )
                    }

                    if (pitchCheckMode.value) {
                        TuningIndicator(pitchResult.value)
                    }

                    AdBanner()
                }
            }
        }

        // Audio capture effect for visual tuning feedback
        if (pitchCheckMode.value && selectedStringIndex.intValue >= 0) {
            val stringIndex = selectedStringIndex.intValue
            val targetString = if (stringIndex < banjoStrings.size) banjoStrings[stringIndex] else banjoStrings[0]
            AudioCaptureEffect(targetString, pitchResult)
        }
    }

    @Composable
    private fun LaunchedAutoPlay(
        index: Int,
        isVolumeLow: MutableState<Boolean>,
        selectedTuning: MutableState<TuningPreset>,
    ) {
        LaunchedEffect(Unit) {
            isVolumeLow.value = player.isVolumeLow()
            try {
                player.volume = 1.0f
                player.playWithLoop(selectedTuning.value.assetFiles[index])
            } catch (e: IOException) {
                Log.e("EarActivity", "Auto-playing sound", e)
            }
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
                val actualBufferSize = maxOf(minBufferSize, bufferSize * 4)

                val record =
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                        actualBufferSize,
                    )
                audioRecord = record
                record.startRecording()

                val audioBuffer = FloatArray(bufferSize)
                while (isActive) {
                    val read = record.read(audioBuffer, 0, bufferSize, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        val detected = pitchDetector.detectPitch(audioBuffer)
                        val result =
                            if (detected > 0) {
                                val cents = pitchDetector.centsFromTarget(detected, targetString.frequencyHz)
                                PitchResult(
                                    detectedHz = detected,
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

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .background(indicatorColor, RoundedCornerShape(6.dp)),
                )

                Spacer(modifier = Modifier.height(4.dp))

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
    private fun SelectorRow(
        instrumentName: String,
        tuningName: String,
        instruments: List<Instrument>,
        onInstrumentSelected: (Int) -> Unit,
        tunings: List<Tuning>,
        onTuningSelected: (Int) -> Unit,
        onShareTuning: () -> Unit,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DropdownSelector(
                label = instrumentName,
                items = instruments.map { it.name },
                onSelected = onInstrumentSelected,
            )
            DropdownSelector(
                label = tuningName,
                items = tunings.map { it.name },
                onSelected = onTuningSelected,
            )
            IconButton(onClick = onShareTuning) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.share_tuning),
                    tint = colorResource(id = R.color.banjen_accent),
                )
            }
        }
    }

    @Composable
    private fun DropdownSelector(
        label: String,
        items: List<String>,
        onSelected: (Int) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(
                    text = label,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.banjen_accent),
                        ),
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = colorResource(id = R.color.banjen_accent),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            expanded = false
                            onSelected(index)
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.TuningSelector(
        selectedTuning: MutableState<TuningPreset>,
        selectedOption: MutableState<Int>,
    ) {
        val expanded = remember { mutableStateOf(false) }
        val accentColor = colorResource(id = R.color.banjen_accent)

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(
                onClick = { expanded.value = true },
            ) {
                Text(
                    text = "${stringResource(R.string.tuning_selector_label)}: ${selectedTuning.value.displayName}",
                    style = TextStyle(fontSize = 16.sp, color = accentColor),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = accentColor,
                )
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
            ) {
                TuningPreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.displayName) },
                        onClick = {
                            if (selectedTuning.value != preset) {
                                player.stop()
                                selectedOption.value = -1
                                selectedTuning.value = preset
                                saveTuning(preset)
                            }
                            expanded.value = false
                        },
                    )
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
    private fun SessionModeToggle(onActivate: () -> Unit) {
        val label = stringResource(id = R.string.session_mode_label)

        TextButton(
            onClick = onActivate,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = label,
                    tint = colorResource(id = R.color.banjen_accent),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.banjen_accent),
                        ),
                )
            }
        }
    }

    @Composable
    private fun SessionModeLayout(
        snackbarHostState: SnackbarHostState,
        selectedTuning: MutableState<TuningPreset>,
    ) {
        val currentStringIndex = remember { mutableIntStateOf(0) }
        val sessionRunning = remember { mutableStateOf(true) }
        val savedVol = prefs.getFloat(KEY_SESSION_VOLUME, DEFAULT_SESSION_VOLUME)
        val sessionVolume = remember { mutableFloatStateOf(savedVol) }
        val stopLabel = stringResource(id = R.string.session_stop)

        LaunchedEffect(sessionRunning.value) {
            if (!sessionRunning.value) return@LaunchedEffect

            var index = 0
            while (index <= 3 && sessionRunning.value) {
                currentStringIndex.intValue = index
                player.volume = sessionVolume.floatValue
                try {
                    player.playWithLoop(selectedTuning.value.assetFiles[index])
                } catch (e: IOException) {
                    Log.e("EarActivity", "Session mode playback", e)
                }
                delay(SECONDS_PER_STRING * 1000L)
                val next = autoAdvanceNextIndex(index)
                if (next != null) {
                    index = next
                } else {
                    sessionRunning.value = false
                    player.stop()
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Black,
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = getString(sessionStringNames[currentStringIndex.intValue]),
                    style =
                        TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        ),
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProgressDots(currentStringIndex)

                Spacer(modifier = Modifier.height(32.dp))

                VolumeSlider(sessionVolume)

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = {
                        exitSessionMode()
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = stopLabel,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stopLabel,
                            style =
                                TextStyle(
                                    fontSize = 18.sp,
                                    color = Color.White,
                                ),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ProgressDots(currentStringIndex: MutableIntState) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (i in 0..3) {
                val isActive = i <= currentStringIndex.intValue
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color.White else Color.DarkGray),
                )
            }
        }
    }

    @Composable
    private fun VolumeSlider(sessionVolume: MutableFloatState) {
        val volumeLabel = stringResource(id = R.string.session_volume_label)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp),
        ) {
            Text(
                text = volumeLabel,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray,
                    ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = sessionVolume.floatValue,
                onValueChange = { newVolume ->
                    val clamped = clampVolume(newVolume)
                    sessionVolume.floatValue = clamped
                    player.volume = clamped
                    prefs.edit().putFloat(KEY_SESSION_VOLUME, clamped).apply()
                },
                valueRange = 0.05f..1.0f,
                colors =
                    SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.DarkGray,
                    ),
            )
        }
    }

    @Composable
    private fun AdBanner() {
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
    private fun ColumnScope.NoteButton(
        index: Int,
        note: Note,
        subtitle: Int,
        description: Int,
        selectedOption: MutableState<Int>,
        isVolumeLow: MutableState<Boolean>,
        snackbarHostState: SnackbarHostState,
        pitchCheckMode: MutableState<Boolean>,
        pitchResult: MutableState<PitchResult?>,
        selectedStringIndex: MutableState<Int>,
    ) {
        val isSelected = selectedOption.value == index
        val scope = rememberCoroutineScope()
        val volumeLowMessage = stringResource(id = R.string.volume_low_message)
        val buttonDescription = stringResource(id = description)

        val permissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                if (granted) {
                    toneGenerator.stop()
                    pitchCheckMode.value = true
                    pitchResult.value = null
                    selectedStringIndex.value = index
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
                    .defaultMinSize(minHeight = 48.dp)
                    .graphicsLayer(
                        scaleX = scaleAnimation,
                        scaleY = scaleAnimation,
                        translationX = shakeAnimation,
                    ).semantics { contentDescription = buttonDescription },
            onClick = {
                // If in pitch check mode, exit it and resume tone
                if (pitchCheckMode.value) {
                    pitchCheckMode.value = false
                    pitchResult.value = null
                    stopAudioCapture()

                    if (selectedOption.value != index) {
                        selectedOption.value = index
                        selectedStringIndex.value = index
                        isVolumeLow.value = isVolumeLow()
                        toneGenerator.play(note.frequency)
                    } else {
                        toneGenerator.play(note.frequency)
                    }
                    return@TextButton
                }

                if (selectedOption.value != index) {
                    selectedOption.value = index
                    selectedStringIndex.value = index
                    isVolumeLow.value = isVolumeLow()
                    toneGenerator.play(note.frequency)
                } else {
                    toneGenerator.stop()
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = note.name,
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
                                toneGenerator.stop()
                                pitchCheckMode.value = true
                                pitchResult.value = null
                                selectedStringIndex.value = index
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
