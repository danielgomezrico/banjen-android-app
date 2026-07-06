package com.makingiants.android.banjotuner

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val KEY_INSTRUMENT_INDEX = "instrument_index"
private const val KEY_TUNING_INDEX = "tuning_index"

class EarActivity : ComponentActivity() {
    companion object {
        const val EXTRA_STRING_INDEX = "string_index"
        private const val MAX_STRING_INDEX = 4 // 5-string banjo has 5 strings (0..4)

        fun parseStringIndex(intent: Intent?): Int {
            val index = intent?.getIntExtra(EXTRA_STRING_INDEX, -1) ?: -1
            return validateStringIndex(index)
        }

        fun validateStringIndex(index: Int): Int = if (index in 0..MAX_STRING_INDEX) index else -1
    }

    @VisibleForTesting
    internal val toneGenerator by lazy { ToneGenerator() }

    private val pitchDetector by lazy { PitchDetector() }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val analytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    private var savedPitch: Int = DEFAULT_PITCH

    private var sessionModeActive = mutableStateOf(false)
    private var audioRecord: AudioRecord? = null

    private fun stopAudioCapture() {
        Timber.d("audioCapture: stop")
        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {
        }
        audioRecord?.release()
        audioRecord = null
    }

    private fun exitSessionMode() {
        Timber.d("session: exit")
        sessionModeActive.value = false
        toneGenerator.stop()
    }

    private fun shareTuning(tuning: Tuning) {
        val encoded = encodeTuning(tuning)
        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Banjen Tuning: ${tuning.name}")
                putExtra(Intent.EXTRA_TEXT, "Banjen Tuning: $encoded")
            }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_tuning)))
    }

    private fun isVolumeLow(): Boolean {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return isVolumeLow(current, max)
    }

    private fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                null -> {}
                else -> bundle.putString(key, value.toString())
            }
        }
        analytics.logEvent(name, bundle)
        Timber.d("Analytics: $name $params")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))

        // Animate splash exit using the SplashScreen SDK (custom fade + subtle shrink for smooth handoff)
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            splashScreenView.animate()
                .alpha(0f)
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(280)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    splashScreenViewProvider.remove()
                }
                .start()
        }
        volumeControlStream = AudioManager.STREAM_MUSIC

        savedPitch = prefs.getInt(KEY_REFERENCE_PITCH, DEFAULT_PITCH)

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

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        MobileAds.initialize(this)
        val autoPlayIndex = parseStringIndex(intent)
        if (autoPlayIndex >= 0) {
            logEvent("widget_string_tapped", mapOf("index" to autoPlayIndex))
        }
        setContent { Contents(autoPlayIndex) }
    }

    override fun onPause() {
        Timber.d("onPause: sessionWasActive=%b", sessionModeActive.value)
        if (sessionModeActive.value) {
            exitSessionMode()
        }
        toneGenerator.stop()
        stopAudioCapture()
        super.onPause()
    }

    override fun onDestroy() {
        toneGenerator.release()
        super.onDestroy()
    }

    @Composable
    fun Contents(autoPlayIndex: Int = -1) {
        MaterialTheme(
            colorScheme =
                lightColorScheme(
                    primary = colorResource(id = R.color.banjen_accent),
                    onPrimary = colorResource(id = R.color.banjen_background),
                    secondary = colorResource(id = R.color.banjen_gray),
                    onSecondary = colorResource(id = R.color.banjen_accent),
                    background = colorResource(id = R.color.banjen_background),
                    onBackground = colorResource(id = R.color.banjen_accent),
                    surface = colorResource(id = R.color.banjen_gray_light),
                    onSurface = colorResource(id = R.color.banjen_accent),
                    error = Color(0xFFF44336),
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

        NormalLayout(snackbarHostState, referencePitch, autoPlayIndex)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NormalLayout(
        snackbarHostState: SnackbarHostState,
        referencePitch: MutableIntState,
        autoPlayIndex: Int = -1,
    ) {
        val scope = rememberCoroutineScope()
        var showSettings by remember { mutableStateOf(false) }
        val pitchCheckMode = remember { mutableStateOf(false) }
        val pitchResult = remember { mutableStateOf<PitchResult?>(null) }
        val selectedStringIndex = remember { mutableIntStateOf(-1) }

        val savedInstrumentIndex =
            prefs
                .getInt(KEY_INSTRUMENT_INDEX, 0)
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

        val selectedOption = remember { mutableIntStateOf(if (autoPlayIndex in currentTuningModel.notes.indices) autoPlayIndex else -1) }
        val isVolumeLow = remember { mutableStateOf(false) }
        val volumeLowMessage = stringResource(id = R.string.volume_low_message)

        // Debug state for ads (and future): surfaced only in DEBUG builds via settings sheet.
        val adDebugStatus = remember { mutableStateOf("idle") }
        val forceDebugAd = remember { mutableStateOf(false) }

        // Session mode: auto-plays through each string while keeping the main UI visible
        LaunchedEffect(sessionModeActive.value) {
            if (!sessionModeActive.value) {
                selectedOption.intValue = -1
                return@LaunchedEffect
            }
            isVolumeLow.value = isVolumeLow()
            if (isVolumeLow.value) {
                scope.launch { snackbarHostState.showSnackbar(volumeLowMessage) }
            }
            var index = 0
            val totalStrings = currentTuningModel.notes.size
            while (index < totalStrings && sessionModeActive.value) {
                selectedOption.intValue = index
                val n = currentTuningModel.notes[index]
                Timber.d("session: string index=%d note=%s freq=%.2fHz durationSec=%d", index, n.name, n.frequency, SECONDS_PER_STRING)
                toneGenerator.play(currentTuningModel.notes[index].frequency)
                delay(SECONDS_PER_STRING * 1000L)
                if (index < totalStrings - 1) {
                    index++
                } else {
                    exitSessionMode()
                    break
                }
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                )
            },
            containerColor = colorResource(id = R.color.banjen_background),
            contentWindowInsets = WindowInsets(0),
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(paddingValues),
            ) {
                if (autoPlayIndex in currentTuningModel.notes.indices) {
                    LaunchedAutoPlay(autoPlayIndex, isVolumeLow, currentTuningModel.notes[autoPlayIndex])
                }

                BanjoStringCanvas(
                    notes = currentTuningModel.notes,
                    selectedString = selectedOption.intValue,
                    onStringSelected = { index ->
                        if (sessionModeActive.value) return@BanjoStringCanvas
                        if (pitchCheckMode.value) {
                            pitchCheckMode.value = false
                            pitchResult.value = null
                            stopAudioCapture()
                        }
                        if (index == -1) {
                            Timber.d("tap: stop prevIndex=%d", selectedOption.intValue)
                            logEvent(
                                "string_stopped",
                                mapOf(
                                    "index" to selectedOption.intValue,
                                    "instrument" to currentInstrument.name,
                                    "tuning" to currentTuningModel.name,
                                ),
                            )
                            toneGenerator.stop()
                            selectedOption.intValue = -1
                            selectedStringIndex.intValue = -1
                            isVolumeLow.value = false
                        } else {
                            val note = currentTuningModel.notes.getOrNull(index) ?: return@BanjoStringCanvas
                            Timber.d(
                                "tap: play index=%d note=%s freq=%.2fHz instrument=%s tuning=%s refPitch=%d",
                                index,
                                note.name,
                                note.frequency,
                                currentInstrument.name,
                                currentTuningModel.name,
                                referencePitch.intValue,
                            )
                            logEvent(
                                "string_played",
                                mapOf(
                                    "index" to index,
                                    "note" to note.name,
                                    "instrument" to currentInstrument.name,
                                    "tuning" to currentTuningModel.name,
                                    "ref_pitch" to referencePitch.intValue,
                                ),
                            )
                            toneGenerator.play(note.frequency)
                            selectedOption.intValue = index
                            selectedStringIndex.intValue = index
                            isVolumeLow.value = isVolumeLow()
                            if (isVolumeLow.value) {
                                val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                Timber.d("volumeLow: current=%d max=%d", cur, max)
                            }
                            if (isVolumeLow.value) {
                                scope.launch { snackbarHostState.showSnackbar(volumeLowMessage) }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                // Show ad banner only when no string is active (or force in debug for testing).
                val showAd = selectedOption.intValue == -1 || (BuildConfig.DEBUG && forceDebugAd.value)
                if (showAd) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                    ) {
                        AdBanner { status -> adDebugStatus.value = status }
                    }
                }

                // Debug indicator (always visible in DEBUG) + status from ad loads.
                if (BuildConfig.DEBUG) {
                    Text(
                        "DBG:${adDebugStatus.value}",
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B6B),
                    )
                }

                // Overlay: BANJEN wordmark + pill buttons float over the canvas
                CanvasOverlay(
                    isSessionActive = sessionModeActive.value,
                    isStringActive = selectedOption.intValue >= 0,
                    onSessionClick = {
                        toneGenerator.stop()
                        selectedOption.intValue = -1
                        Timber.d("session: start tuning=%s strings=%d", currentTuningModel.name, currentTuningModel.notes.size)
                        logEvent(
                            "session_started",
                            mapOf(
                                "instrument" to currentInstrument.name,
                                "tuning" to currentTuningModel.name,
                                "strings" to currentTuningModel.notes.size,
                            ),
                        )
                        sessionModeActive.value = true
                    },
                    onSettingsClick = {
                        logEvent(
                            "settings_opened",
                            mapOf(
                                "instrument" to currentInstrument.name,
                                "tuning" to currentTuningModel.name,
                            ),
                        )
                        showSettings = true
                    },
                    onStopClick = {
                        logEvent(
                            "session_stopped",
                            mapOf(
                                "instrument" to currentInstrument.name,
                                "tuning" to currentTuningModel.name,
                            ),
                        )
                        exitSessionMode()
                    },
                )
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = colorResource(id = R.color.banjen_background),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    SelectorRow(
                        instrumentName = currentInstrument.name,
                        tuningName = currentTuningModel.name,
                        instruments = ALL_INSTRUMENTS,
                        onInstrumentSelected = { newIndex ->
                            val name = ALL_INSTRUMENTS[newIndex].name
                            Timber.d("instrumentChange: newIndex=%d name=%s", newIndex, name)
                            logEvent("instrument_selected", mapOf("name" to name, "index" to newIndex))
                            showSettings = false
                            toneGenerator.stop()
                            selectedOption.intValue = -1
                            isVolumeLow.value = false
                            instrumentIndex = newIndex
                            tuningModelIndex = 0
                            prefs
                                .edit()
                                .putInt(KEY_INSTRUMENT_INDEX, newIndex)
                                .putInt(KEY_TUNING_INDEX, 0)
                                .apply()
                        },
                        tunings = currentInstrument.tunings,
                        onTuningSelected = { newIndex ->
                            val name = currentInstrument.tunings[newIndex].name
                            Timber.d("tuningChange: newIndex=%d name=%s", newIndex, name)
                            logEvent("tuning_selected", mapOf("name" to name, "index" to newIndex))
                            showSettings = false
                            toneGenerator.stop()
                            selectedOption.intValue = -1
                            isVolumeLow.value = false
                            tuningModelIndex = newIndex
                            prefs
                                .edit()
                                .putInt(KEY_TUNING_INDEX, newIndex)
                                .apply()
                        },
                        onShareTuning = {
                            logEvent(
                                "tuning_shared",
                                mapOf(
                                    "instrument" to currentInstrument.name,
                                    "tuning" to currentTuningModel.name,
                                ),
                            )
                            shareTuning(currentTuningModel)
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF49251E)),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PitchControl(referencePitch) { newPitch ->
                        prefs.edit().putInt(KEY_REFERENCE_PITCH, newPitch).apply()
                        val pitchRatio = calculatePitchRatio(newPitch)
                        val currentIndex = selectedOption.intValue
                        if (currentIndex >= 0) {
                            val note = currentTuningModel.notes.getOrNull(currentIndex)
                            if (note != null) {
                                Timber.d("pitchAdjust: newA=%d ratio=%f effectiveHz=%f", newPitch, pitchRatio, note.frequency * pitchRatio)
                                toneGenerator.play(note.frequency * pitchRatio)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Debug page (DEBUG builds only). Shows ad config + live status from AdListener.
                    // Allows forcing the banner for ad testing independent of string selection.
                    // Use to verify init, unit IDs, load success/fail codes after R8 changes.
                    if (BuildConfig.DEBUG) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF49251E)),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "DEBUG",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold,
                        )
                        Text("ad status: ${adDebugStatus.value}", style = MaterialTheme.typography.bodySmall)
                        val maskedUnit = stringResource(id = R.string.ads_unit_id_banner).let { u ->
                            if (u.length > 8) u.take(8) + "..." + u.takeLast(4) else u
                        }
                        Text("banner unit: $maskedUnit", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val newVal = !forceDebugAd.value
                                    logEvent("debug_force_ad_toggled", mapOf("enabled" to newVal))
                                    forceDebugAd.value = newVal
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(if (forceDebugAd.value) "hide debug ad" else "force show ad", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(
                                onClick = {
                                    logEvent("debug_ad_re_request")
                                    forceDebugAd.value = true
                                    adDebugStatus.value = "re-request"
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text("re-request", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Audio capture effect for visual tuning feedback
        if (pitchCheckMode.value && selectedStringIndex.intValue >= 0) {
            val stringIndex = selectedStringIndex.intValue
            val targetFrequency =
                currentTuningModel.notes
                    .getOrElse(stringIndex) { currentTuningModel.notes[0] }
                    .frequency
                    .toDouble()
            AudioCaptureEffect(targetFrequency, pitchResult)
        }
    }

    @Composable
    private fun LaunchedAutoPlay(
        index: Int,
        isVolumeLow: MutableState<Boolean>,
        note: Note,
    ) {
        LaunchedEffect(Unit) {
            isVolumeLow.value = isVolumeLow()
            Timber.d("autoPlay: index=%d note=%s freq=%.2fHz", index, note.name, note.frequency)
            toneGenerator.play(note.frequency)
        }
    }

    @Composable
    private fun AudioCaptureEffect(
        targetFrequency: Double,
        pitchResult: MutableState<PitchResult?>,
    ) {
        val sampleRate = 44100
        val bufferSize = 4096

        DisposableEffect(targetFrequency) {
            onDispose {
                stopAudioCapture()
            }
        }

        LaunchedEffect(targetFrequency) {
            var lastPitchLogMs = 0L
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
                Timber.d("audioCapture: start sampleRate=%d bufferSize=%d actualBufferSize=%d", sampleRate, bufferSize, actualBufferSize)

                val audioBuffer = FloatArray(bufferSize)
                while (isActive) {
                    val read = record.read(audioBuffer, 0, bufferSize, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        val detected = pitchDetector.detectPitch(audioBuffer)
                        val result =
                            if (detected > 0) {
                                val cents = pitchDetector.centsFromTarget(detected, targetFrequency)
                                PitchResult(
                                    detectedHz = detected,
                                    targetHz = targetFrequency,
                                    centDeviation = cents,
                                    status = pitchDetector.classifyTuning(cents),
                                )
                            } else {
                                PitchResult(
                                    detectedHz = 0.0,
                                    targetHz = targetFrequency,
                                    centDeviation = 0.0,
                                    status = TuningStatus.NO_SIGNAL,
                                )
                            }
                        withContext(Dispatchers.Main) {
                            pitchResult.value = result
                        }
                        val now = System.currentTimeMillis()
                        if (now - lastPitchLogMs >= 1000L) {
                            val detectedStr = if (result.detectedHz <= 0) "none" else "%.1fHz".format(result.detectedHz)
                            val centsStr = if (result.status == TuningStatus.NO_SIGNAL) "n/a" else "%+.1f".format(result.centDeviation)
                            Timber.d(
                                "pitch: detected=%s target=%.1fHz cents=%s status=%s",
                                detectedStr,
                                targetFrequency,
                                centsStr,
                                result.status,
                            )
                            lastPitchLogMs = now
                        }
                    }
                }
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
                    modifier = Modifier.size(24.dp),
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
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF49251E))
                        .border(1.dp, Color(0xFF6B4035), RoundedCornerShape(50))
                        .clickable { expanded = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFFBE9),
                            ),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFFFFFBE9),
                        modifier = Modifier.size(20.dp),
                    )
                }
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
                    val oldPitch = referencePitch.value
                    val newPitch = clampPitch(oldPitch - 1)
                    referencePitch.value = newPitch
                    logEvent("pitch_decreased", mapOf("from" to oldPitch, "to" to newPitch))
                    onPitchChanged(newPitch)
                },
                enabled = canDecreasePitch(referencePitch.value),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Remove,
                    contentDescription = "$pitchLabel -1",
                    tint = colorResource(id = R.color.banjen_accent),
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "A=${referencePitch.value}",
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.banjen_accent),
                        textAlign = TextAlign.Center,
                    ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val oldPitch = referencePitch.value
                    val newPitch = clampPitch(oldPitch + 1)
                    referencePitch.value = newPitch
                    logEvent("pitch_increased", mapOf("from" to oldPitch, "to" to newPitch))
                    onPitchChanged(newPitch)
                },
                enabled = canIncreasePitch(referencePitch.value),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "$pitchLabel +1",
                    tint = colorResource(id = R.color.banjen_accent),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }

    @Composable
    private fun AdBanner(onStatusUpdate: (String) -> Unit = {}) {
        val adsUnitId = stringResource(id = R.string.ads_unit_id_banner)

        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    adUnitId = adsUnitId
                    setAdSize(AdSize.BANNER)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Timber.i("Ad: loaded unit=%s", adsUnitId.take(12) + "...")
                            onStatusUpdate("loaded")
                        }
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Timber.w("Ad: failed code=%d msg=%s domain=%s", loadAdError.code, loadAdError.message, loadAdError.domain)
                            onStatusUpdate("failed:${loadAdError.code}")
                        }
                        override fun onAdOpened() { Timber.d("Ad: opened") }
                        override fun onAdClosed() { Timber.d("Ad: closed") }
                    }
                    loadAd(AdRequest.Builder().build())
                    onStatusUpdate("requested")
                }
            },
            modifier = Modifier.wrapContentSize(),
        )
    }

    @Composable
    private fun CanvasOverlay(
        isSessionActive: Boolean,
        isStringActive: Boolean,
        onSessionClick: () -> Unit,
        onSettingsClick: () -> Unit,
        onStopClick: () -> Unit,
    ) {
        val overlayAlpha by animateFloatAsState(
            targetValue = if (isStringActive) 0.35f else 1f,
            animationSpec = tween(300),
            label = "overlay-alpha",
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .graphicsLayer(alpha = overlayAlpha),
        ) {
            // Main row: play (or space), title, settings. Title stays aligned to play level.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reserve space for play button (and potential stop below)
                Box(modifier = Modifier.size(48.dp)) {
                    PillIconButton(
                        icon = painterResource(id = R.drawable.ic_play),
                        contentDescription = stringResource(id = R.string.session_mode_label),
                        onClick = onSessionClick,
                        size = 48.dp,
                        iconSize = 24.dp,
                    )
                }

                Text(
                    text = "BANJEN",
                    style =
                        androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFFBE9),
                            letterSpacing = 4.sp,
                        ),
                )

                PillIconButton(
                    icon = painterResource(id = R.drawable.ic_gear),
                    contentDescription = stringResource(id = R.string.settings_label),
                    onClick = onSettingsClick,
                    size = 48.dp,
                    iconSize = 24.dp,
                )
            }

            // Stop button placed below the play button when session active.
            // Positioned to the left, below the play pill. Easy to spot.
            if (isSessionActive) {
                Box(
                    modifier = Modifier
                        .padding(start = 0.dp, top = 52.dp)  // below the 48dp play + small gap
                ) {
                    PillIconButton(
                        icon = painterResource(id = R.drawable.ic_stop),
                        contentDescription = stringResource(id = R.string.session_stop),
                        onClick = onStopClick,
                        size = 48.dp,
                        iconSize = 24.dp,
                    )
                }
            }
        }
    }

    @Composable
    private fun PillIconButton(
        icon: androidx.compose.ui.graphics.painter.Painter,
        contentDescription: String,
        onClick: () -> Unit,
        size: Dp = 40.dp,
        iconSize: Dp = 20.dp,
        modifier: Modifier = Modifier,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.92f else 1f,
            animationSpec = tween(100),
            label = "pill-scale",
        )

        Box(
            modifier =
                modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .size(size)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF49251E))
                    .border(1.dp, Color(0xFF6B4035), RoundedCornerShape(50))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = Color(0xFFFFFBE9),
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
