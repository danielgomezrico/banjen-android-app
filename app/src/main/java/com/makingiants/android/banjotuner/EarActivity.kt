package com.makingiants.android.banjotuner

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.media.AudioManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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

private const val PREFS_NAME = "banjen_prefs"
private const val KEY_INSTRUMENT_INDEX = "instrument_index"
private const val KEY_TUNING_INDEX = "tuning_index"

class EarActivity : AppCompatActivity() {
    @VisibleForTesting
    internal val toneGenerator by lazy { ToneGenerator() }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        setContent { Contents() }
    }

    override fun onPause() {
        toneGenerator.stop()
        super.onPause()
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

        val savedInstrumentIndex = prefs.getInt(KEY_INSTRUMENT_INDEX, 0)
            .coerceIn(0, ALL_INSTRUMENTS.size - 1)
        val savedTuningIndex = prefs.getInt(KEY_TUNING_INDEX, 0)

        var instrumentIndex by remember { mutableIntStateOf(savedInstrumentIndex) }
        var tuningIndex by remember { mutableIntStateOf(
            savedTuningIndex.coerceIn(0, ALL_INSTRUMENTS[savedInstrumentIndex].tunings.size - 1)
        ) }

        val currentInstrument = ALL_INSTRUMENTS[instrumentIndex]
        val currentTuning = currentInstrument.tunings[tuningIndex]

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = colorResource(id = R.color.banjen_background),
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val selectedNoteIndex = remember { mutableIntStateOf(-1) }
                    val isVolumeLow = remember { mutableStateOf(false) }

                    SelectorRow(
                        instrumentName = currentInstrument.name,
                        tuningName = currentTuning.name,
                        instruments = ALL_INSTRUMENTS,
                        onInstrumentSelected = { newIndex ->
                            toneGenerator.stop()
                            selectedNoteIndex.intValue = -1
                            isVolumeLow.value = false
                            instrumentIndex = newIndex
                            tuningIndex = 0
                            prefs.edit()
                                .putInt(KEY_INSTRUMENT_INDEX, newIndex)
                                .putInt(KEY_TUNING_INDEX, 0)
                                .apply()
                        },
                        tunings = currentInstrument.tunings,
                        onTuningSelected = { newIndex ->
                            toneGenerator.stop()
                            selectedNoteIndex.intValue = -1
                            isVolumeLow.value = false
                            tuningIndex = newIndex
                            prefs.edit()
                                .putInt(KEY_TUNING_INDEX, newIndex)
                                .apply()
                        },
                        onShareTuning = { shareTuning(currentTuning) },
                    )

                    currentTuning.notes.forEachIndexed { index, note ->
                        NoteButton(
                            index = index,
                            note = note,
                            selectedNoteIndex = selectedNoteIndex,
                            isVolumeLow = isVolumeLow,
                            snackbarHostState = snackbarHostState,
                        )
                    }

                    AdBanner()
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
            modifier = Modifier
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
                    style = TextStyle(
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
        selectedNoteIndex: MutableState<Int>,
        isVolumeLow: MutableState<Boolean>,
        snackbarHostState: SnackbarHostState,
    ) {
        val isSelected = selectedNoteIndex.value == index
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
        val iconShakeAnimation = if (showVolumeIcon) {
            rememberInfiniteTransition(label = "icon-infinite").animateFloat(
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
                if (selectedNoteIndex.value != index) {
                    selectedNoteIndex.value = index
                    isVolumeLow.value = isVolumeLow()
                    toneGenerator.play(note.frequency)
                } else {
                    toneGenerator.stop()
                    selectedNoteIndex.value = -1
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
                        modifier = Modifier
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
                    text = note.name,
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
