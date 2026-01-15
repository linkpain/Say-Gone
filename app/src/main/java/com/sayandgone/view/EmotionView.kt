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
import kotlin.math.min
import kotlin.random.Random

class EmotionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var emotionColor = Color.parseColor("#6B7280")
    private var emotionRadius = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var alpha = 1f
    private var scale = 1f
    private var isDissolving = false
    private var emotionName = ""

    private val particles = mutableListOf<Particle>()
    private var particleAnimator: ValueAnimator? = null

    var onClickListener: (() -> Unit)? = null

    init {
        blurPaint.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 40f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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
        paint.strokeWidth = 12f
        paint.strokeCap = Paint.Cap.ROUND

        canvas.save()
        canvas.scale(scale, scale, centerX, centerY)

        val headRadius = emotionRadius * 0.35f
        val bodyLength = emotionRadius * 1.5f
        val limbLength = emotionRadius * 1.0f

        val headCenterY = centerY - bodyLength * 0.4f
        val bodyTopY = headCenterY + headRadius
        val bodyBottomY = centerY + bodyLength * 0.6f

        canvas.drawCircle(centerX, headCenterY, headRadius, paint)

        // Draw emotion name inside the head if available
        if (emotionName.isNotEmpty()) {
            val textBounds = Rect()
            textPaint.getTextBounds(emotionName, 0, emotionName.length, textBounds)
            val textWidth = textBounds.width().toFloat()
            val textHeight = textBounds.height().toFloat()
            
            // Calculate max font size to fit in circle
            var textSize = 40f
            textPaint.textSize = textSize
            
            while (textWidth > headRadius * 1.8f || textHeight > headRadius * 1.8f) {
                textSize -= 2f
                textPaint.textSize = textSize
                textPaint.getTextBounds(emotionName, 0, emotionName.length, textBounds)
            }
            
            canvas.drawText(emotionName, centerX, headCenterY + textHeight / 2, textPaint)
        }

        paint.style = Paint.Style.STROKE
        canvas.drawLine(centerX, bodyTopY, centerX, bodyBottomY, paint)

        canvas.drawLine(centerX, bodyTopY + limbLength * 0.3f, centerX - limbLength * 0.5f, bodyTopY + limbLength * 0.7f, paint)
        canvas.drawLine(centerX, bodyTopY + limbLength * 0.3f, centerX + limbLength * 0.5f, bodyTopY + limbLength * 0.7f, paint)

        canvas.drawLine(centerX, bodyBottomY, centerX - limbLength * 0.5f, bodyBottomY + limbLength, paint)
        canvas.drawLine(centerX, bodyBottomY, centerX + limbLength * 0.5f, bodyBottomY + limbLength, paint)

        canvas.restore()
    }

    private fun drawParticles(canvas: Canvas) {
        particles.forEach { particle ->
            paint.color = particle.color
            paint.alpha = (255 * particle.alpha).toInt()
            paint.style = Paint.Style.FILL
            
            canvas.save()
            canvas.rotate(particle.rotation, particle.x, particle.y)
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            canvas.restore()
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

    fun setEmotionName(name: String) {
        emotionName = name
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
        val particleCount = 800
        val headRadius = emotionRadius * 0.25f
        val bodyLength = emotionRadius * 1.2f
        val limbLength = emotionRadius * 0.8f

        val headCenterY = centerY - bodyLength * 0.4f
        val bodyTopY = headCenterY + headRadius
        val bodyBottomY = centerY + bodyLength * 0.6f

        val bodyParts = listOf(
            Pair(centerX, headCenterY),
            Pair(centerX, bodyTopY),
            Pair(centerX, bodyBottomY),
            Pair(centerX - limbLength * 0.5f, bodyTopY + limbLength * 0.7f),
            Pair(centerX + limbLength * 0.5f, bodyTopY + limbLength * 0.7f),
            Pair(centerX - limbLength * 0.5f, bodyBottomY + limbLength),
            Pair(centerX + limbLength * 0.5f, bodyBottomY + limbLength)
        )

        val colors = listOf(
            emotionColor,
            Color.rgb(255, 255, 255),
            Color.rgb(255, 200, 100),
            Color.rgb(100, 200, 255)
        )

        for (i in 0 until particleCount) {
            val bodyPart = bodyParts.random()
            val offsetX = Random.nextFloat() * 30f - 15f
            val offsetY = Random.nextFloat() * 30f - 15f
            val x = bodyPart.first + offsetX
            val y = bodyPart.second + offsetY
            val radius = Random.nextFloat() * 12f + 3f
            val color = colors.random()
            val explosionTime = Random.nextFloat() * 0.3f + 0.2f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    radius = radius,
                    velocityX = Random.nextFloat() * 8f - 4f,
                    velocityY = Random.nextFloat() * 8f - 4f,
                    alpha = 1f,
                    color = color,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 10f - 5f,
                    canExplode = true,
                    explosionTime = explosionTime,
                    hasExploded = false
                )
            )
        }
    }

    private fun updateParticles(progress: Float) {
        val newParticles = mutableListOf<Particle>()
        
        particles.forEach { particle ->
            particle.x += particle.velocityX * (1 - progress) * 5f
            particle.y += particle.velocityY * (1 - progress) * 5f
            particle.alpha = 1f - progress
            particle.radius *= 0.985f
            particle.rotation += particle.rotationSpeed * (1 - progress)
            
            if (particle.canExplode && !particle.hasExploded && progress >= particle.explosionTime) {
                particle.hasExploded = true
                val smallParticleCount = 2
                for (i in 0 until smallParticleCount) {
                    val smallRadius = particle.radius * 0.5f
                    newParticles.add(
                        Particle(
                            x = particle.x,
                            y = particle.y,
                            radius = smallRadius,
                            velocityX = Random.nextFloat() * 6f - 3f,
                            velocityY = Random.nextFloat() * 6f - 3f,
                            alpha = 1f,
                            color = particle.color,
                            rotation = Random.nextFloat() * 360f,
                            rotationSpeed = Random.nextFloat() * 15f - 7.5f,
                            canExplode = false,
                            explosionTime = 0f,
                            hasExploded = false
                        )
                    )
                }
            }
        }
        
        particles.addAll(newParticles)
    }

    fun reset() {
        isDissolving = false
        alpha = 1f
        scale = 1f
        particles.clear()
        particleAnimator?.cancel()
        invalidate()
    }

    fun getHeadTopPosition(): Pair<Float, Float> {
        val bodyLength = emotionRadius * 1.5f
        val headRadius = emotionRadius * 0.35f
        val headCenterY = centerY - bodyLength * 0.4f
        val headTopY = headCenterY - headRadius
        return Pair(centerX, headTopY)
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var velocityX: Float,
        var velocityY: Float,
        var alpha: Float,
        var color: Int,
        var rotation: Float,
        var rotationSpeed: Float,
        var canExplode: Boolean,
        var explosionTime: Float,
        var hasExploded: Boolean
    )
}
