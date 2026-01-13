package com.sayandgone

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sayandgone.data.EndingPhrases
import com.sayandgone.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var rootLayout: FrameLayout
    private lateinit var inputEditText: EditText
    private lateinit var hintText: TextView
    private lateinit var mainButton: Button
    private lateinit var disappearedText: TextView
    private lateinit var undoHint: TextView
    private lateinit var endingPhraseText: TextView

    private var undoTimer: CountDownTimer? = null
    private var breathingAnimator: ValueAnimator? = null
    private var backgroundAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initViews()
        setupInputListener()
        setupButtonListener()
        observeViewModel()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        inputEditText = findViewById(R.id.inputEditText)
        hintText = findViewById(R.id.hintText)
        mainButton = findViewById(R.id.mainButton)
        disappearedText = findViewById(R.id.disappearedText)
        undoHint = findViewById(R.id.undoHint)
        endingPhraseText = findViewById(R.id.endingPhraseText)
    }

    private fun setupInputListener() {
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateText(s?.toString() ?: "")
                startBreathingEffect()
                startBackgroundEffect()
            }

            override fun afterTextChanged(s: Editable?) {
                inputEditText.postDelayed({
                    stopBreathingEffect()
                    stopBackgroundEffect()
                }, 1000)
            }
        })
    }

    private fun setupButtonListener() {
        mainButton.setOnClickListener {
            when {
                viewModel.canUndo.value -> {
                    undo()
                }
                viewModel.endingPhrase.value.isNotEmpty() -> {
                    finish()
                }
                viewModel.inputText.value.isNotEmpty() -> {
                    startDestructionCeremony()
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.inputText.collect { text ->
                if (!viewModel.isDestroyed.value) {
                    inputEditText.setText(text)
                    inputEditText.setSelection(text.length)
                }
            }
        }
    }

    private fun startBreathingEffect() {
        stopBreathingEffect()

        breathingAnimator = ValueAnimator.ofFloat(1f, 1.02f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                inputEditText.scaleX = scale
                inputEditText.scaleY = scale
            }

            start()
        }
    }

    private fun stopBreathingEffect() {
        breathingAnimator?.cancel()
        breathingAnimator = null
        inputEditText.scaleX = 1f
        inputEditText.scaleY = 1f
    }

    private fun startBackgroundEffect() {
        stopBackgroundEffect()

        val originalColor = ContextCompat.getColor(this, R.color.background)
        val lighterColor = adjustBrightness(originalColor, 5)

        backgroundAnimator = ValueAnimator.ofArgb(originalColor, lighterColor, originalColor).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                rootLayout.setBackgroundColor(animator.animatedValue as Int)
            }

            start()
        }
    }

    private fun stopBackgroundEffect() {
        backgroundAnimator?.cancel()
        backgroundAnimator = null
        rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background))
    }

    private fun adjustBrightness(color: Int, amount: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        val newRed = (red * (100 + amount) / 100).coerceIn(0, 255)
        val newGreen = (green * (100 + amount) / 100).coerceIn(0, 255)
        val newBlue = (blue * (100 + amount) / 100).coerceIn(0, 255)

        return Color.rgb(newRed, newGreen, newBlue)
    }

    private fun startDestructionCeremony() {
        viewModel.saveText()

        inputEditText.isFocusable = false
        inputEditText.isFocusableInTouchMode = false
        inputEditText.isClickable = false

        mainButton.text = getString(R.string.button_confirmed)

        inputEditText.postDelayed({
            startMainDestruction()
        }, 600)
    }

    private fun startMainDestruction() {
        inputEditText.animate()
            .alpha(0f)
            .translationY(40f)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        inputEditText.postDelayed({
            startAftermath()
        }, 1800)
    }

    private fun startAftermath() {
        disappearedText.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        undoHint.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        viewModel.destroy()
        startUndoTimer()
    }

    private fun startUndoTimer() {
        undoTimer?.cancel()

        undoTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                undoHint.text = "$seconds 秒内可撤销"
            }

            override fun onFinish() {
                finalizeDestruction()
            }
        }.start()
    }

    private fun undo() {
        undoTimer?.cancel()

        disappearedText.animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        undoHint.animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        endingPhraseText.animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        inputEditText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start()

        inputEditText.isFocusable = true
        inputEditText.isFocusableInTouchMode = true
        inputEditText.isClickable = true

        mainButton.text = getString(R.string.button_initial)

        viewModel.undo()
    }

    private fun finalizeDestruction() {
        undoTimer?.cancel()

        undoHint.animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        disappearedText.animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        viewModel.finalize()

        val textLength = viewModel.inputText.value.length
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isDay = hour in 8..20

        val phrase = EndingPhrases.getPhrase(textLength, isDay)
        viewModel.setEndingPhrase(phrase)

        endingPhraseText.text = phrase
        endingPhraseText.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        mainButton.text = getString(R.string.button_close)
    }

    override fun onDestroy() {
        super.onDestroy()
        undoTimer?.cancel()
        breathingAnimator?.cancel()
        backgroundAnimator?.cancel()
    }
}
