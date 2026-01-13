package com.sayandgone

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        if (hasShownOnboarding()) {
            startMainActivity()
            return
        }

        onboardingText = findViewById(R.id.onboardingText)
        startOnboardingAnimation()
    }

    private fun hasShownOnboarding(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)
    }

    private fun markOnboardingShown() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_ONBOARDING_SHOWN, true)
        }
    }

    private fun startOnboardingAnimation() {
        onboardingText.text = getString(R.string.onboarding_line1)

        onboardingText.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(0)
            .start()

        onboardingText.postDelayed({
            onboardingText.text = getString(R.string.onboarding_line1) + "\n" + getString(R.string.onboarding_line2)
        }, 1000)

        onboardingText.postDelayed({
            onboardingText.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        markOnboardingShown()
                        startMainActivity()
                    }
                })
                .start()
        }, 3000)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val PREFS_NAME = "sayandgone_prefs"
        private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    }
}
