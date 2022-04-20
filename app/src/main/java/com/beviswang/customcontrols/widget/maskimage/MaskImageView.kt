package com.beviswang.customcontrols.widget.maskimage


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView

/**
 * 带遮罩效果的 ImageView
 * @author BevisWong
 * @date 2022/4/20
 */
class MaskImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    def: Int = 0
) : AppCompatImageView(context, attrs, def) {
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBlockSize: Float = 0f

    private var mPath1: Path = Path()
    private var mPath2: Path = Path()

    private var mDrawingAnimator: ValueAnimator? = null
    private var mAnimatorProgress: Float = 0f
    private var mLoadFinishedListener: () -> Unit = {}

    private var mCenterRectWidth: Float = 180f

    private var mIsReverse: Boolean = true

    init {
        mPaint.textSize = 48f
        mPaint.color = Color.parseColor("#333333")
        mPaint.strokeWidth = 0.5f
    }

    fun open() {
        doAnimator(0f, mCenterRectWidth)
    }

    fun close() {
//        doAnimator(mCenterRectWidth, 0f)
        mDrawingAnimator?.cancel()
    }

    fun move(progress: Float) {
        mAnimatorProgress = progress * mCenterRectWidth
        invalidate()
    }

    fun reverse(isReverse: Boolean = true) {
        mIsReverse = isReverse
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBlockSize = w / 4f
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        super.onDraw(canvas)
        if (!mIsReverse) genPath() else genPathReverse()
        canvas.drawPath(mPath1, mPaint)
        canvas.drawPath(mPath2, mPaint)
    }

    private fun genPath() {
        mPath1.reset()
        mPath1.moveTo(0f, 0f)
        mPath1.lineTo(width / 4f + mAnimatorProgress, 0f)
        mPath1.lineTo(width / 2f + mAnimatorProgress, height.toFloat())
        mPath1.lineTo(0f, height.toFloat())
        mPath1.close()

        mPath2.reset()
        mPath2.moveTo(width / 2f + mAnimatorProgress, 0f)
        mPath2.lineTo(width.toFloat(), 0f)
        mPath2.lineTo(width.toFloat(), height.toFloat())
        mPath2.lineTo(3 * width / 4f + mAnimatorProgress, height.toFloat())
        mPath2.close()
    }

    private fun genPathReverse() {
        mPath1.reset()
        mPath1.moveTo(0f, 0f)
        mPath1.lineTo(width / 2f + mAnimatorProgress, 0f)
        mPath1.lineTo(width / 4f + mAnimatorProgress, height.toFloat())
        mPath1.lineTo(0f, height.toFloat())
        mPath1.close()

        mPath2.reset()
        mPath2.moveTo(3 * width / 4f + mAnimatorProgress, 0f)
        mPath2.lineTo(width.toFloat(), 0f)
        mPath2.lineTo(width.toFloat(), height.toFloat())
        mPath2.lineTo(width / 2f + mAnimatorProgress, height.toFloat())
        mPath2.close()
    }

    private fun doAnimator(start: Float, end: Float) {
        mDrawingAnimator?.cancel()
        mDrawingAnimator = ValueAnimator.ofFloat(start, end)
        mDrawingAnimator?.duration = 400
//        mDrawingAnimator?.interpolator = OvershootInterpolator()
//        mDrawingAnimator?.interpolator = LinearInterpolator()
        mDrawingAnimator?.interpolator = AccelerateDecelerateInterpolator()
//        mDrawingAnimator?.repeatMode = ValueAnimator.RESTART
        mDrawingAnimator?.repeatMode = ValueAnimator.REVERSE
        mDrawingAnimator?.repeatCount = ValueAnimator.INFINITE
        mDrawingAnimator?.addUpdateListener {
            mAnimatorProgress = it.animatedValue as Float
            if (mAnimatorProgress == end) {
                mLoadFinishedListener()
            }
            invalidate()
        }
        mDrawingAnimator?.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        open()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        close()
    }
}