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

class AttackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val activeAttacks = mutableListOf<Attack>()
    
    var onHitListener: (() -> Unit)? = null

    init {
        textPaint.color = Color.parseColor("#EF4444")
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 40f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        activeAttacks.forEach { attack ->
            if (attack.alpha > 0f) {
                canvas.save()
                canvas.translate(attack.currentX, attack.currentY)
                canvas.rotate(attack.rotation)
                canvas.scale(attack.scale, attack.scale)
                
                val textBounds = Rect()
                textPaint.getTextBounds(attack.text, 0, attack.text.length, textBounds)
                val textHeight = textBounds.height().toFloat()
                
                canvas.drawText(attack.text, 0f, textHeight / 2, textPaint)
                
                canvas.restore()
            }
        }
        
        if (activeAttacks.isEmpty()) {
            visibility = GONE
        }
    }

    fun launchAttack(startX: Float, startY: Float, targetX: Float, targetY: Float, text: String) {
        val attack = Attack(
            startX = startX,
            startY = startY,
            targetX = targetX,
            targetY = targetY,
            text = text
        )
        
        activeAttacks.add(attack)
        
        val distance = sqrt((targetX - startX).pow(2) + (targetY - startY).pow(2))
        val duration = (distance * 2).toLong().coerceAtLeast(200).coerceAtMost(600)
        
        attack.animator = ValueAnimator.ofFloat(0f, 1f).apply {
            setDuration(duration)
            interpolator = AccelerateInterpolator()
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                attack.currentX = startX + (targetX - startX) * progress
                attack.currentY = startY + (targetY - startY) * progress
                attack.rotation = (sin(progress * PI * 4) * 30f).toFloat()
                attack.alpha = 1f
                attack.scale = 1f
                
                checkHit(attack, targetX, targetY)
                invalidate()
            }
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!attack.hasHit) {
                        forceHit(attack)
                    }
                }
            })
            
            start()
        }
        
        invalidate()
    }

    private fun checkHit(attack: Attack, targetX: Float, targetY: Float) {
        if (attack.hasHit) return
        
        val distance = sqrt((targetX - attack.currentX).pow(2) + (targetY - attack.currentY).pow(2))
        
        if (distance < 50f) {
            attack.hasHit = true
            onHitListener?.invoke()
            startHitAnimation(attack)
        }
    }

    private fun forceHit(attack: Attack) {
        attack.hasHit = true
        onHitListener?.invoke()
        startHitAnimation(attack)
    }

    private fun startHitAnimation(attack: Attack) {
        attack.animator?.cancel()
        
        attack.hitAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                attack.alpha = progress
                attack.scale = 1f + (1f - progress) * 0.5f
                invalidate()
            }
            
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    activeAttacks.remove(attack)
                    if (activeAttacks.isEmpty()) {
                        visibility = GONE
                    }
                }
            })
            
            start()
        }
    }

    fun reset() {
        activeAttacks.clear()
        visibility = GONE
        invalidate()
    }

    private data class Attack(
        var startX: Float,
        var startY: Float,
        var targetX: Float,
        var targetY: Float,
        var text: String,
        var currentX: Float = startX,
        var currentY: Float = startY,
        var alpha: Float = 1f,
        var scale: Float = 1f,
        var rotation: Float = 0f,
        var hasHit: Boolean = false,
        var animator: ValueAnimator? = null,
        var hitAnimator: ValueAnimator? = null
    )
}
