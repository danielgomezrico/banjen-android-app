package com.makingiants.android.banjotuner

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.IOException
import java.lang.ref.WeakReference

class EarActivity : AppCompatActivity() {
    //    private val adsView by lazy { findViewById<AdView>(R.id.adView) }
    private val player by lazy { SoundPlayer(this) }

    // private val adsRunnable by lazy { SetupAdsRunnable(adsView, this) }

    @VisibleForTesting
    internal val clickAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.shake_animation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Contents() }

//        adsView.postDelayed(adsRunnable, 500)
    }

    override fun onPause() {
        player.stop()
        super.onPause()
    }

    @Composable
    @Preview
    fun Contents() {
        MaterialTheme(
            colorScheme = lightColorScheme(
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
            )
        ) {
            MainLayout()
        }
    }

    @Composable
    fun MainLayout() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val radioOptions = listOf(
                    R.string.ear_button_4_text,
                    R.string.ear_button_3_text,
                    R.string.ear_button_2_text,
                    R.string.ear_button_1_text,
                )

                val selectedOption = remember { mutableIntStateOf(-1) }

                radioOptions.forEachIndexed { index, text ->
                    TextButton(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        onClick = {
                            val selectedValue = selectedOption.value

                            if (selectedValue != text) {
                                selectedOption.value = text

                                try {
                                    player.playWithLoop(index)
                                } catch (e: IOException) {
                                    Log.e("EarActivity", "Playing sound", e)
                                }
                            } else {
                                player.stop()
                                selectedOption.value = -1
                            }

                        },
                    ) {
                        Text(
                            text = getString(text),
                            style = TextStyle(
                                fontSize = 20.sp, // Corresponding to TextAppearance.AppCompat.Large
                                color = colorResource(id = R.color.banjen_accent),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }

            //TODO: Fix
            // AdView
//            AndroidView(
//                factory = { context ->A
//                    AdView(context).apply {
//                        adUnitId = "@string/ads_unit_id_banner"
//                        loadAd(AdRequest.Builder().build())
//                    }
//                },
//                modifier = Modifier
//                    .wrapContentSize()
//            )
        }
    }

    @Composable
    fun GuitarString(
        vibrationFrequency: Float = 1f,
        amplitude: Float = 10f
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "string")
        val phase = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "string float"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height / 2

            for (i in 0 until width.toInt()) {
                val x = i.toFloat()
                val v = (vibrationFrequency * x + phase.value).toDouble()
                val y = height + amplitude * kotlin.math.sin(v)
                drawCircle(Color.Black, 1f, Offset(x, y.toFloat()))
            }
        }
    }

    /**
     * Setup the ads in a runnable helping to not lose frames when the view is been created
     * ([onCreate]), ads process takes time to finish.
     */
    class SetupAdsRunnable(adsView: AdView, context: Context) : Runnable {
        private val weakAdsView = WeakReference(adsView)
        private val weakContext = WeakReference(context)

        override fun run() {
            try {
                weakContext.get()?.let {
                    MobileAds.initialize(it) {
                        FirebaseCrashlytics.getInstance().log("Ads initialized")
                    }
                }

                weakAdsView.get()?.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (e: IllegalStateException) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }
}
