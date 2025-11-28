package com.rideincab.user.common.helper

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage helper
 * @category WaveDrawable
 * @author SMR IT Solutions
 * 
 */

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import android.view.animation.Interpolator

/* ************************************************************
 wave animation while request page
*************************************************************** */
class WaveDrawable
/**
 * @param color  colro
 * @param radius radius
 */
(var color: Int, var radius: Int) : Drawable() {
    private var waveScale: Float = 0.toFloat()
    var alphas: Int = 0
    var wavePaint: Paint
    var animationTime: Long = 1000
    private var waveInterpolator: Interpolator? = null
    private var alphaInterpolator: Interpolator? = null
    var animator: Animator? = null
    var animatorSet: AnimatorSet

    val isAnimationRunning: Boolean
        get() = if (animator != null) {
            animator!!.isRunning
        } else false

    /**
     * @param color         color
     * @param radius        radius
     * @param animationTime time
     */
    constructor(color: Int, radius: Int, animationTime: Long) : this(color, radius) {
        this.animationTime = animationTime
    }

    init {
        this.waveScale = 0.3f
        this.alphas = 255
        wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        animatorSet = AnimatorSet()
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        // circle
        wavePaint.style = Paint.Style.FILL
        wavePaint.color = color
        wavePaint.alpha = alphas
        canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius * waveScale, wavePaint)
    }

    /**
     * @param interpolator interpolator
     */
    fun setWaveInterpolator(interpolator: Interpolator) {
        this.waveInterpolator = interpolator
    }

    /**
     * @param interpolator interpolator
     */
    fun setAlphaInterpolator(interpolator: Interpolator) {
        this.alphaInterpolator = interpolator
    }

    fun startAnimation() {
        animator = generateAnimation()
        animator!!.start()
    }

    fun stopAnimation() {
        if (animator!!.isRunning) {
            animator!!.end()
        }
    }

    override fun setAlpha(alpha: Int) {
        this.alphas = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        wavePaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return wavePaint.alpha
    }

    protected fun getWaveScale(): Float {
        return waveScale
    }

    protected fun setWaveScale(waveScale: Float) {
        this.waveScale = waveScale
        invalidateSelf()
    }

    fun generateAnimation(): Animator {
        //Wave animation
        val waveAnimator = ObjectAnimator.ofFloat(this, "waveScale", 0.3f, 1f)
        waveAnimator.duration = animationTime
        if (waveInterpolator != null) {
            waveAnimator.interpolator = waveInterpolator
        }
        //The animation is repeated
        waveAnimator.repeatCount = Animation.INFINITE
        waveAnimator.repeatMode = Animation.INFINITE
        //alpha animation
        val alphaAnimator = ObjectAnimator.ofInt(this, "alpha", 255, 50)
        alphaAnimator.duration = animationTime
        if (alphaInterpolator != null) {
            alphaAnimator.interpolator = alphaInterpolator
        }
        alphaAnimator.repeatCount = Animation.INFINITE
        alphaAnimator.repeatMode = Animation.INFINITE
        animatorSet.playTogether(waveAnimator, alphaAnimator)
        return animatorSet
    }
}