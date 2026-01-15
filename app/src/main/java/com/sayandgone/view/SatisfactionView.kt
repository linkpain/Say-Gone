package com.sayandgone.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.*
import kotlin.random.Random

class SatisfactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var text = "爽"
    private var textX = 0f
    private var textY = 0f
    private var targetX = 0f
    private var targetY = 0f
    private var emotionCenterX = 0f
    private var emotionCenterY = 0f
    private var emotionRadius = 0f
    private var textSize = 200f
    private var alpha = 1f
    private var scale = 1f
    private var rotation = 0f
    
    private var isFalling = false
    private var hasCollided = false
    
    private var fallAnimator: ValueAnimator? = null
    private var collisionAnimator: ValueAnimator? = null
    
    var onCollisionListener: (() -> Unit)? = null
    
    private var shakeX = 0f
    private var shakeY = 0f
    private val sparkParticles = mutableListOf<SparkParticle>()
    private var screenShakeX = 0f
    private var screenShakeY = 0f
    
    var onScreenShakeListener: ((Float, Float) -> Unit)? = null

    init {
        paint.color = Color.parseColor("#EF4444")
        paint.style = Paint.Style.FILL
        
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        shadowPaint.color = Color.parseColor("#EF4444")
        shadowPaint.alpha = 100
        shadowPaint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldh, oldh)
        
        textSize = min(w, h) * 0.7f
        textPaint.textSize = textSize
        
        textX = w / 2f
        textY = -textSize * 2
        targetX = w / 2f
        targetY = h / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (alpha <= 0f) return
        
        canvas.save()
        canvas.translate(textX + shakeX, textY + shakeY)
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        
        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()
        
        canvas.drawText(text, 0f, textHeight / 2, textPaint)
        
        canvas.restore()
        
        drawSparkParticles(canvas)
    }

    fun setEmotionPosition(centerX: Float, centerY: Float, radius: Float) {
        emotionCenterX = centerX
        emotionCenterY = centerY
        emotionRadius = radius
    }

    fun startFallingAnimation() {
        if (isFalling) return
        
        isFalling = true
        hasCollided = false
        alpha = 1f
        scale = 1f
        rotation = 0f
        sparkParticles.clear()
        
        val startY = -textSize * 2
        val endY = emotionCenterY + emotionRadius * 0.5f
        
        fallAnimator = ValueAnimator.ofFloat(startY, endY).apply {
            duration = 800
            interpolator = AccelerateInterpolator(1.2f)
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                val normalizedProgress = (progress - startY) / (endY - startY)
                textY = progress
                rotation = sin(progress * 0.02f) * 15f
                
                shakeX = (Random.nextFloat() - 0.5f) * normalizedProgress * 10f
                shakeY = (Random.nextFloat() - 0.5f) * normalizedProgress * 10f
                
                if (Random.nextFloat() < 0.3f) {
                    createSparkParticle(textX, textY)
                }
                
                updateSparkParticles()
                
                checkCollision()
                invalidate()
            }
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!hasCollided) {
                        forceCollision()
                    }
                }
            })
            
            start()
        }
    }

    private fun checkCollision() {
        if (hasCollided) return
        
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        
        val textBottom = textY + textBounds.height() / 2
        val textTop = textY - textBounds.height() / 2
        
        val emotionTop = emotionCenterY - emotionRadius
        val emotionBottom = emotionCenterY + emotionRadius
        
        if (textBottom >= emotionTop) {
            hasCollided = true
            onCollisionListener?.invoke()
            startCollisionAnimation()
        }
    }

    private fun forceCollision() {
        hasCollided = true
        onCollisionListener?.invoke()
        startCollisionAnimation()
    }

    private fun startCollisionAnimation() {
        fallAnimator?.cancel()
        
        collisionAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                alpha = progress
                scale = 1f + (1f - progress) * 0.3f
                textY += (1f - progress) * 10f
                
                val shakeIntensity = (1f - progress) * 20f
                screenShakeX = (Random.nextFloat() - 0.5f) * shakeIntensity
                screenShakeY = (Random.nextFloat() - 0.5f) * shakeIntensity
                
                onScreenShakeListener?.invoke(screenShakeX, screenShakeY)
                
                invalidate()
            }
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = GONE
                }
            })
            
            start()
        }
    }

    fun reset() {
        fallAnimator?.cancel()
        collisionAnimator?.cancel()
        
        isFalling = false
        hasCollided = false
        alpha = 1f
        scale = 1f
        rotation = 0f
        textY = -textSize * 2
        shakeX = 0f
        shakeY = 0f
        sparkParticles.clear()
        screenShakeX = 0f
        screenShakeY = 0f
        
        visibility = VISIBLE
        invalidate()
    }
    
    private fun createSparkParticle(x: Float, y: Float) {
        val particle = SparkParticle(
            x = x + (Random.nextFloat() - 0.5f) * 100f,
            y = y + (Random.nextFloat() - 0.5f) * 50f,
            radius = Random.nextFloat() * 8f + 2f,
            alpha = 1f,
            color = Color.rgb(255, Random.nextInt(100, 256), Random.nextInt(100, 256)),
            velocityX = Random.nextFloat() * 4f - 2f,
            velocityY = Random.nextFloat() * 4f - 2f
        )
        sparkParticles.add(particle)
    }
    
    private fun updateSparkParticles() {
        sparkParticles.forEach { particle ->
            particle.x += particle.velocityX
            particle.y += particle.velocityY
            particle.alpha -= 0.02f
            particle.radius *= 0.98f
        }
        sparkParticles.removeAll { it.alpha <= 0f }
    }
    
    private fun drawSparkParticles(canvas: Canvas) {
        sparkParticles.forEach { particle ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = particle.color
            paint.alpha = (255 * particle.alpha).toInt()
            paint.style = Paint.Style.FILL
            
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
        }
    }
    
    private data class SparkParticle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var alpha: Float,
        var color: Int,
        var velocityX: Float,
        var velocityY: Float
    )
}
