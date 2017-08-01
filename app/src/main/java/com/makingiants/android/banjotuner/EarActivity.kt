package com.makingiants.android.banjotuner

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ToggleButton
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_ear_ads.*
import java.io.IOException

class EarActivity : AppCompatActivity(), View.OnClickListener {

  private val player by lazy { SoundPlayer(this) }
  private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }
  private val clickAnimation: Animation by lazy {
    AnimationUtils.loadAnimation(this, R.anim.shake_animation).apply {
      interpolator = FastOutLinearInInterpolator()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Fabric.with(this, Crashlytics())
    firebaseAnalytics.logEvent("screenview", Bundle().apply { putString("name", "ear") })
    setContentView(R.layout.activity_ear_ads)

    val adRequest: AdRequest
    if (BuildConfig.DEBUG) {
      adRequest = AdRequest.Builder()
          .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
          .addTestDevice("027c6ee5571a8376")
          .build()
    } else {
      adRequest = AdRequest.Builder().build()
    }
    adsView.loadAd(adRequest)

    soundsRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
      (0 until radioGroup.childCount)
          .map { radioGroup.getChildAt(it) as ToggleButton }
          .forEach {
            val shouldCheck = it.id == i

            it.isChecked = shouldCheck

            if (!shouldCheck) {
              it.clearAnimation()
              ViewCompat.setElevation(it, 4f)
            }
          }
    }

    arrayOf(ear1Button, ear2Button, ear3Button, ear4Button).forEach {
      it.setOnClickListener(this)
    }
  }

  override fun onPause() {
    soundsRadioGroup.clearCheck()
    player.stop()
    super.onPause()
  }

  override fun onClick(view: View?) {
    val button = view as ToggleButton

    soundsRadioGroup.check(button.id)
    firebaseAnalytics.logEvent("click", Bundle().apply { putString("id", "$button.id") })

    if (button.isChecked) {
      val buttonTag = Integer.parseInt(button.tag.toString())
      try {
        player.playWithLoop(buttonTag)

        ViewCompat.setElevation(button, 4f)
        button.startAnimation(clickAnimation)
      } catch (e: IOException) {
        Log.e("EarActivity", "Playing sound", e)
      }
    } else {
      button.clearAnimation()
      player.stop()
    }
  }

}

