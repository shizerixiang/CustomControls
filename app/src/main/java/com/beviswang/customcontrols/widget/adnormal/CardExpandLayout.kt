package com.beviswang.customcontrols.widget.adnormal

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.beviswang.customcontrols.loge
import com.beviswang.customcontrols.util.DrawHelper.drawXfermode

class CardExpandLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    def: Int = 0
) : ConstraintLayout(context, attrs, def) {
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mDrawingAnimator: ValueAnimator? = null
    private var mAnimatorProgress: Float = 0f

    private var mMaskCanvas: Canvas? = null
    private var mMaskBitmap: Bitmap? = null

    private var mRectFArray: Array<RectF> = arrayOf()

    private var mRectUnit: Float = 0f

    private var mLoadFinishedListener: () -> Unit = {}

    private var mIsShow: Boolean = false

    init {
        mPaint.textSize = 48f
        mPaint.color = Color.parseColor("#CCCCCC")
        mPaint.strokeWidth = 0.5f
    }

    fun hide(loadFinished: (() -> Unit)? = null) {
        mIsShow = false
        doAnimator(1f, 0f)
        loadFinished(loadFinished ?: return)
    }

    fun show(loadFinished: (() -> Unit)? = null) {
        mIsShow = true
        doAnimator(0f, 1f)
        loadFinished(loadFinished ?: return)
    }

    fun loadFinished(block: () -> Unit) {
        mLoadFinishedListener = block
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mRectUnit = w / 3f
        mRectFArray = arrayOf(
            RectF(0f, 0f, mRectUnit, 0f),
            RectF(mRectUnit, 0f, mRectUnit * 2, 0f),
            RectF(mRectUnit * 2, 0f, mRectUnit * 3, 0f)
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        if (canvas == null) return
        genMask(canvas)
        mPaint.drawXfermode(canvas, PorterDuff.Mode.DST_IN, src = {
            super.dispatchDraw(canvas)
            canvas.drawLine(mRectUnit, 0f, mRectUnit, height.toFloat(), mPaint)
            canvas.drawLine(mRectUnit * 2, 0f, mRectUnit * 2, height.toFloat(), mPaint)
        }, dst = {
            canvas.drawBitmap(mMaskBitmap ?: return@drawXfermode, 0f, 0f, mPaint)
        })
    }

    private fun doAnimator(start: Float, end: Float) {
        mDrawingAnimator?.cancel()
        mDrawingAnimator = ValueAnimator.ofFloat(start, end)
        mDrawingAnimator?.duration = 600
//        mDrawingAnimator?.interpolator = OvershootInterpolator()
//        mDrawingAnimator?.interpolator = LinearInterpolator()
        mDrawingAnimator?.interpolator = AccelerateDecelerateInterpolator()
//        mDrawingAnimator?.repeatMode = ValueAnimator.RESTART
//        mDrawingAnimator?.repeatMode = ValueAnimator.REVERSE
//        mDrawingAnimator?.repeatCount = ValueAnimator.INFINITE
        mDrawingAnimator?.addUpdateListener {
            mAnimatorProgress = it.animatedValue as Float
            if (mAnimatorProgress == end) {
                mLoadFinishedListener()
            }
            invalidate()
        }
        mDrawingAnimator?.start()
    }

    private fun genMask(canvas: Canvas) {
        if (mRectFArray.isEmpty()) return
        mRectFArray[0].bottom = if (mAnimatorProgress > 0f) height * mAnimatorProgress else 0f
        mRectFArray[1].bottom = if (mAnimatorProgress > 0.3f)
            height / 0.7f * (mAnimatorProgress - 0.3f) else 0f
        mRectFArray[2].bottom = if (mAnimatorProgress > 0.6f)
            height / 0.4f * (mAnimatorProgress - 0.6f) else 0f
        genMaskBitmap(canvas)
    }

    private fun genMaskBitmap(canvas: Canvas) {
        mMaskBitmap =
            Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888) ?: return
        mMaskCanvas = Canvas(mMaskBitmap!!)
        mMaskCanvas?.drawRect(mRectFArray[0], mPaint)
        mMaskCanvas?.drawRect(mRectFArray[1], mPaint)
        mMaskCanvas?.drawRect(mRectFArray[2], mPaint)
    }
}