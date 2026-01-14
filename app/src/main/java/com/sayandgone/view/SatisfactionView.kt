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

    init {
        paint.color = Color.parseColor("#EF4444")
        paint.style = Paint.Style.FILL
        
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        shadowPaint.color = Color.parseColor("#EF4444")
        shadowPaint.alpha = 100
        shadowPaint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldh, oldh)
        textX = w / 2f
        textY = -textSize
        targetX = w / 2f
        targetY = h / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (alpha <= 0f) return
        
        canvas.save()
        canvas.translate(textX, textY)
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        
        val textWidth = textBounds.width().toFloat()
        val textHeight = textBounds.height().toFloat()
        
        canvas.drawText(text, 0f, textHeight / 2, textPaint)
        
        canvas.restore()
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
        
        val startY = -textSize * 2
        val endY = emotionCenterY + emotionRadius * 0.5f
        
        fallAnimator = ValueAnimator.ofFloat(startY, endY).apply {
            duration = 800
            interpolator = AccelerateInterpolator(1.2f)
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                textY = progress
                rotation = sin(progress * 0.02f) * 15f
                
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
        
        visibility = VISIBLE
        invalidate()
    }
}
