package com.makingiants.android.banjotuner

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.IOException
import java.lang.ref.WeakReference


class EarActivity : AppCompatActivity(), View.OnClickListener {

    private val adsView by lazy { findViewById<AdView>(R.id.adView) }
    private val ear1Button by lazy { findViewById<Button>(R.id.ear1Button) }
    private val ear2Button by lazy { findViewById<Button>(R.id.ear2Button) }
    private val ear3Button by lazy { findViewById<Button>(R.id.ear3Button) }
    private val ear4Button by lazy { findViewById<Button>(R.id.ear4Button) }
    private val soundsRadioGroup by lazy { findViewById<RadioGroup>(R.id.soundsRadioGroup) }

    private val player by lazy { SoundPlayer(this) }
    private val elevationPixels by lazy {
        resources.getDimensionPixelSize(R.dimen.spacing_4dp).toFloat()
    }

    private val adsRunnable by lazy { SetupAdsRunnable(adsView, this) }

    @VisibleForTesting
    internal val clickAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.shake_animation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ear_ads)

        soundsRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            (0 until radioGroup.childCount)
                    .map { radioGroup.getChildAt(it) as ToggleButton }
                    .forEach {
                        val shouldCheck = it.id == i

                        it.isChecked = shouldCheck
                        handleAnimation(it, shouldCheck)
                    }
        }

        arrayOf(ear1Button, ear2Button, ear3Button, ear4Button).forEach {
            it.setOnClickListener(this)
        }

        adsView.postDelayed(adsRunnable, 500)
    }

    override fun onPause() {
        soundsRadioGroup.clearCheck()
        player.stop()
        super.onPause()
    }

    override fun onClick(view: View?) {
        (view as? ToggleButton)?.let { button ->
            soundsRadioGroup.check(button.id)
            handleAnimation(button, button.isChecked)

            if (button.isChecked) {
                val buttonTag = Integer.parseInt(button.tag.toString())
                try {
                    player.playWithLoop(buttonTag)
                } catch (e: IOException) {
                    Log.e("EarActivity", "Playing sound", e)
                }
            } else {
                player.stop()
            }
        }
    }

    private fun handleAnimation(button: Button, shouldShow: Boolean = false) {
        if (shouldShow) {
            ViewCompat.setElevation(button, elevationPixels)
            button.startAnimation(clickAnimation)
        } else {
            ViewCompat.setElevation(button, 0f)
            button.clearAnimation()
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

                if (BuildConfig.DEBUG) {
                    val testDevices: MutableList<String> = ArrayList()
                    testDevices.add(AdRequest.DEVICE_ID_EMULATOR)

                    val requestConfiguration = RequestConfiguration.Builder()
                            .setTestDeviceIds(testDevices)
                            .build()
                    MobileAds.setRequestConfiguration(requestConfiguration)
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

