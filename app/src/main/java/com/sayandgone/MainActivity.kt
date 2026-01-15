package com.sayandgone

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.sayandgone.data.EndingPhrases
import com.sayandgone.view.AttackView
import com.sayandgone.view.EmotionView
import com.sayandgone.view.SatisfactionView
import com.sayandgone.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private val profanityLibrary = listOf(
        "艹尼马",
        "你TM有病",
        "让你TM画饼",
        "让你PUA我",
        "干你妈",
        "霸王龙徒手捏爆你妈的行星",
        "CNM",
        "CTMD",
        "CNND"
    )

    private lateinit var viewModel: MainViewModel
    private lateinit var rootLayout: FrameLayout
    private lateinit var inputContainer: LinearLayout
    private lateinit var releaseContainer: FrameLayout
    private lateinit var emotionNameInput: EditText
    private lateinit var inputEditText: EditText
    private lateinit var hintText: TextView
    private lateinit var mainButton: Button
    private lateinit var emotionView: EmotionView
    private lateinit var satisfactionView: SatisfactionView
    private lateinit var attackView: AttackView
    private lateinit var touchOverlay: View
    private lateinit var releaseHint: TextView
    private lateinit var clickCountText: TextView
    private lateinit var finishReleaseButton: Button
    private lateinit var disappearedText: TextView
    private lateinit var undoHint: TextView
    private lateinit var endingPhraseText: TextView
    private lateinit var speechBubble: TextView

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
        setupEmotionViewListener()
        setupAttackListener()
        observeViewModel()
        
        mainButton.isEnabled = false
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                mainButton.isEnabled = true
            }
        }.start()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        inputContainer = findViewById(R.id.inputContainer)
        releaseContainer = findViewById(R.id.releaseContainer)
        emotionNameInput = findViewById(R.id.emotionNameInput)
        inputEditText = findViewById(R.id.inputEditText)
        hintText = findViewById(R.id.hintText)
        mainButton = findViewById(R.id.mainButton)
        emotionView = findViewById(R.id.emotionView)
        satisfactionView = findViewById(R.id.satisfactionView)
        attackView = findViewById(R.id.attackView)
        touchOverlay = findViewById(R.id.touchOverlay)
        releaseHint = findViewById(R.id.releaseHint)
        clickCountText = findViewById(R.id.clickCountText)
        finishReleaseButton = findViewById(R.id.finishReleaseButton)
        disappearedText = findViewById(R.id.disappearedText)
        undoHint = findViewById(R.id.undoHint)
        endingPhraseText = findViewById(R.id.endingPhraseText)
        speechBubble = findViewById(R.id.speechBubble)
    }

    private fun setupInputListener() {
        emotionNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateEmotionName(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
                viewModel.inputText.value.isNotEmpty() || viewModel.emotionName.value.isNotEmpty() -> {
                    startReleasingPhase()
                }
            }
        }

        finishReleaseButton.setOnClickListener {
            finishRelease()
        }
    }

    private fun setupEmotionViewListener() {
        emotionView.onClickListener = {
            if (viewModel.isReleasing.value) {
                viewModel.incrementClick()
                emotionView.pulse()
                updateClickCountText()
            }
        }
    }

    private fun setupAttackListener() {
        touchOverlay.setOnTouchListener { _, event ->
            android.util.Log.d("MainActivity", "Touch event: action=${event.action}, isReleasing=${viewModel.isReleasing.value}")
            if (viewModel.isReleasing.value && event.action == MotionEvent.ACTION_DOWN) {
                val text = viewModel.inputText.value
                android.util.Log.d("MainActivity", "Text: $text")
                
                viewModel.incrementClick()
                updateClickCountText()
                emotionView.pulse()
                
                launchProfanityAttack()
                
                if (text.isNotEmpty()) {
                    val lines = text.split("\n")
                    val randomLine = lines.random()
                    android.util.Log.d("MainActivity", "Launching attack: $randomLine")
                    launchAttack(0f, 0f, randomLine)
                }
                true
            } else {
                false
            }
        }
    }

    private fun launchAttack(startX: Float, startY: Float, text: String) {
        android.util.Log.d("MainActivity", "launchAttack called: text=$text")
        val emotionCenterX = releaseContainer.width / 2f
        val emotionCenterY = releaseContainer.height / 2f
        android.util.Log.d("MainActivity", "Container size: ${releaseContainer.width}x${releaseContainer.height}, target: ($emotionCenterX, $emotionCenterY)")
        
        val (attackStartX, attackStartY) = getRandomStartPosition(emotionCenterX, emotionCenterY)
        android.util.Log.d("MainActivity", "Attack start position: ($attackStartX, $attackStartY)")
        
        attackView.visibility = View.VISIBLE
        
        attackView.launchAttack(attackStartX, attackStartY, emotionCenterX, emotionCenterY, text)
        android.util.Log.d("MainActivity", "Attack started")
    }

    private fun launchProfanityAttack() {
        val emotionCenterX = releaseContainer.width / 2f
        val emotionCenterY = releaseContainer.height / 2f
        
        val (attackStartX, attackStartY) = getProfanityAttackPosition()
        val profanityText = profanityLibrary.random()
        
        android.util.Log.d("MainActivity", "Profanity attack from ($attackStartX, $attackStartY): $profanityText")
        
        attackView.visibility = View.VISIBLE
        
        attackView.launchAttack(attackStartX, attackStartY, emotionCenterX, emotionCenterY, profanityText)
    }

    private fun getRandomStartPosition(targetX: Float, targetY: Float): Pair<Float, Float> {
        val width = releaseContainer.width.toFloat()
        val height = releaseContainer.height.toFloat()
        val margin = 50f
        
        val positions = mutableListOf<Pair<Float, Float>>()
        
        positions.add(Pair(0f, margin + (height - 2 * margin) * kotlin.random.Random.nextFloat()))
        positions.add(Pair(width, margin + (height - 2 * margin) * kotlin.random.Random.nextFloat()))
        positions.add(Pair(margin + (width - 2 * margin) * kotlin.random.Random.nextFloat(), 0f))
        positions.add(Pair(margin + (width - 2 * margin) * kotlin.random.Random.nextFloat(), height))
        positions.add(Pair(0f, 0f))
        positions.add(Pair(width, 0f))
        positions.add(Pair(0f, height))
        positions.add(Pair(width, height))
        
        return positions.random()
    }

    private fun getProfanityAttackPosition(): Pair<Float, Float> {
        val width = releaseContainer.width.toFloat()
        val height = releaseContainer.height.toFloat()
        val margin = 50f
        
        val positions = mutableListOf<Pair<Float, Float>>()
        
        positions.add(Pair(width / 2f, 0f))
        positions.add(Pair(width / 2f, height))
        positions.add(Pair(0f, height / 3f))
        positions.add(Pair(0f, height * 2f / 3f))
        positions.add(Pair(width, height / 3f))
        positions.add(Pair(width, height * 2f / 3f))
        
        return positions.random()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.inputText.collect { text ->
                if (!viewModel.isDestroyed.value && !viewModel.isReleasing.value) {
                    inputEditText.setText(text)
                    inputEditText.setSelection(text.length)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.emotionName.collect { name ->
                if (!viewModel.isDestroyed.value && !viewModel.isReleasing.value) {
                    emotionNameInput.setText(name)
                    emotionNameInput.setSelection(name.length)
                }
                emotionView.setEmotionName(name)
            }
        }

        lifecycleScope.launch {
            viewModel.releaseIntensity.collect { intensity ->
                updateEmotionColor(intensity)
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

    private fun startReleasingPhase() {
        viewModel.saveText()
        viewModel.startReleasing()

        satisfactionView.reset()
        satisfactionView.visibility = View.GONE
        attackView.reset()
        finishReleaseButton.visibility = View.VISIBLE
        releaseHint.visibility = View.VISIBLE
        clickCountText.visibility = View.VISIBLE
        clickCountText.text = ""
        mainButton.visibility = View.GONE
        speechBubble.visibility = View.GONE

        inputContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                inputContainer.visibility = View.GONE
                releaseContainer.visibility = View.VISIBLE
                releaseContainer.alpha = 0f
                releaseContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    private fun updateClickCountText() {
        val count = viewModel.clickCount.value
        clickCountText.text = "已点击 $count 次"
        updateSpeechBubble(count)
    }

    private fun updateEmotionColor(intensity: MainViewModel.ReleaseIntensity) {
        val color = when (intensity) {
            MainViewModel.ReleaseIntensity.LEVEL_1 -> Color.parseColor("#6B7280")
            MainViewModel.ReleaseIntensity.LEVEL_2 -> Color.parseColor("#FFD700")
            MainViewModel.ReleaseIntensity.LEVEL_3 -> Color.parseColor("#3B82F6")
            MainViewModel.ReleaseIntensity.LEVEL_4 -> Color.parseColor("#F59E0B")
            MainViewModel.ReleaseIntensity.LEVEL_5 -> Color.parseColor("#EF4444")
        }
        emotionView.setEmotionColor(color)
    }

    private fun updateSpeechBubble(count: Int) {
        when {
            count in 30..59 -> {
                speechBubble.text = "就这就这"
                speechBubble.visibility = View.VISIBLE
            }
            count in 60..79 -> {
                speechBubble.visibility = View.GONE
            }
            count in 80..129 -> {
                speechBubble.text = "我是不会被打败的"
                speechBubble.visibility = View.VISIBLE
            }
            count in 130..149 -> {
                speechBubble.visibility = View.GONE
            }
            count in 150..179 -> {
                speechBubble.text = "我难道要被挂在路灯上吗？"
                speechBubble.visibility = View.VISIBLE
            }
            count in 180..194 -> {
                speechBubble.visibility = View.GONE
            }
            count in 195..200 -> {
                speechBubble.text = "要爆炸啦"
                speechBubble.visibility = View.VISIBLE
            }
            count >= 201 -> {
                speechBubble.visibility = View.GONE
                finishRelease()
            }
        }
    }

    private fun finishRelease() {
        finishReleaseButton.visibility = View.GONE
        releaseHint.visibility = View.GONE
        clickCountText.visibility = View.GONE

        satisfactionView.visibility = View.VISIBLE
        
        val emotionCenterX = releaseContainer.width / 2f
        val emotionCenterY = releaseContainer.height / 2f
        val emotionRadius = min(releaseContainer.width, releaseContainer.height) / 4f
        
        satisfactionView.setEmotionPosition(emotionCenterX, emotionCenterY, emotionRadius)
        
        satisfactionView.onCollisionListener = {
            emotionView.startDissolveAnimation {
                startAftermath()
            }
        }
        
        satisfactionView.startFallingAnimation()
    }

    private fun startAftermath() {
        releaseContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                releaseContainer.visibility = View.GONE
                disappearedText.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()

                viewModel.destroy()
                startUndoTimer()
            }
            .start()
    }

    private fun startUndoTimer() {
        undoTimer?.cancel()

        undoTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
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

        inputContainer.visibility = View.VISIBLE
        inputContainer.alpha = 0f
        inputContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        inputEditText.isFocusable = true
        inputEditText.isFocusableInTouchMode = true
        inputEditText.isClickable = true

        mainButton.text = getString(R.string.button_initial)
        mainButton.visibility = View.VISIBLE
        mainButton.alpha = 1f
        mainButton.isEnabled = true

        viewModel.undo()
        emotionView.reset()
        satisfactionView.reset()
        satisfactionView.visibility = View.GONE
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
        endingPhraseText.alpha = 0f

        mainButton.text = getString(R.string.button_close)
        mainButton.visibility = View.VISIBLE
        mainButton.alpha = 0f
        mainButton.isEnabled = false

        endingPhraseText.postDelayed({
            endingPhraseText.animate()
                .alpha(1f)
                .setDuration(500)
                .start()

            mainButton.animate()
                .alpha(1f)
                .setDuration(500)
                .start()
            mainButton.isEnabled = true
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        undoTimer?.cancel()
        breathingAnimator?.cancel()
        backgroundAnimator?.cancel()
    }
}
