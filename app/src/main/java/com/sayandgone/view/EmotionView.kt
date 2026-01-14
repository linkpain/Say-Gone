package com.sayandgone.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.*
import kotlin.random.Random

class EmotionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var emotionColor = Color.parseColor("#6B7280")
    private var emotionRadius = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var alpha = 1f
    private var scale = 1f
    private var isDissolving = false

    private val particles = mutableListOf<Particle>()
    private var particleAnimator: ValueAnimator? = null

    var onClickListener: (() -> Unit)? = null

    init {
        blurPaint.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldh, oldh)
        centerX = w / 2f
        centerY = h / 2f
        emotionRadius = min(w, h) / 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isDissolving) {
            drawParticles(canvas)
        } else {
            drawEmotion(canvas)
        }
    }

    private fun drawEmotion(canvas: Canvas) {
        paint.color = emotionColor
        paint.alpha = (255 * alpha).toInt()
        paint.style = Paint.Style.FILL

        canvas.save()
        canvas.scale(scale, scale, centerX, centerY)
        canvas.drawCircle(centerX, centerY, emotionRadius, paint)
        canvas.restore()
    }

    private fun drawParticles(canvas: Canvas) {
        particles.forEach { particle ->
            paint.color = emotionColor
            paint.alpha = (255 * particle.alpha).toInt()
            paint.style = Paint.Style.FILL
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !isDissolving) {
            onClickListener?.invoke()
            return true
        }
        return super.onTouchEvent(event)
    }

    fun setEmotionColor(color: Int) {
        emotionColor = color
        invalidate()
    }

    fun pulse() {
        ValueAnimator.ofFloat(1f, 1.1f, 1f).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                scale = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun startDissolveAnimation(onComplete: () -> Unit) {
        isDissolving = true
        createParticles()

        particleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2500
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                updateParticles(animator.animatedValue as Float)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete()
                }
            })
            start()
        }
    }

    private fun createParticles() {
        val particleCount = 50
        for (i in 0 until particleCount) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val distance = Random.nextFloat() * emotionRadius
            val x = centerX + cos(angle) * distance
            val y = centerY + sin(angle) * distance
            val radius = Random.nextFloat() * 8f + 4f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    radius = radius,
                    velocityX = Random.nextFloat() * 4f - 2f,
                    velocityY = Random.nextFloat() * 4f - 2f,
                    alpha = 1f
                )
            )
        }
    }

    private fun updateParticles(progress: Float) {
        particles.forEach { particle ->
            particle.x += particle.velocityX * (1 - progress) * 3f
            particle.y += particle.velocityY * (1 - progress) * 3f
            particle.alpha = 1f - progress
            particle.radius *= 0.99f
        }
    }

    fun reset() {
        isDissolving = false
        alpha = 1f
        scale = 1f
        particles.clear()
        particleAnimator?.cancel()
        invalidate()
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var velocityX: Float,
        var velocityY: Float,
        var alpha: Float
    )
}
