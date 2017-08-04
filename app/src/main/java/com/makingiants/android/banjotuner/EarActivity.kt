package com.makingiants.android.banjotuner

import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
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
  private val elevationPixels: Float by lazy {
    resources.getDimensionPixelSize(R.dimen.spacing_4dp).toFloat()
  }

  @VisibleForTesting
  internal val clickAnimation: Animation by lazy {
    AnimationUtils.loadAnimation(this, R.anim.shake_animation)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Fabric.with(this, Crashlytics())
    firebaseAnalytics.logEvent("screenview", Bundle().apply { putString("name", "ear") })
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
  }

  override fun onPause() {
    soundsRadioGroup.clearCheck()
    player.stop()
    super.onPause()
  }

  override fun onClick(view: View?) {
    (view as? ToggleButton)?.let { button ->
      firebaseAnalytics.logEvent("click", Bundle().apply { putString("id", "$button.id") })
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

  fun handleAnimation(button: Button, shouldShow: Boolean = false) {
    if (shouldShow) {
      ViewCompat.setElevation(button, elevationPixels)
      button.startAnimation(clickAnimation)
    } else {
      ViewCompat.setElevation(button, 0f)
      button.clearAnimation()
    }
  }

}

