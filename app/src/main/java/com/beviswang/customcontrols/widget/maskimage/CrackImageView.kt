package com.beviswang.customcontrols.widget.maskimage


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.beviswang.customcontrols.util.DrawHelper.drawXfermode

/**
 * 带开裂效果的 ImageView
 * @author BevisWong
 * @date 2022/4/20
 */
class CrackImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    def: Int = 0
) : AppCompatImageView(context, attrs, def) {
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBlockSize: Float = 0f

    private var mMaskCanvas1: Canvas? = null
    private var mMaskCanvas2: Canvas? = null
    private var mMaskBitmap1: Bitmap? = null
    private var mMaskBitmap2: Bitmap? = null

    private var mDrawingAnimator: ValueAnimator? = null
    private var mAnimatorProgress: Float = 0f
    private var mLoadFinishedListener: () -> Unit = {}

    private var mCenterRectWidth:Float = 180f

    init {
        mPaint.textSize = 48f
        mPaint.color = Color.parseColor("#CCCCCC")
        mPaint.strokeWidth = 0.5f
    }

    fun open() {
        doAnimator(0f,mCenterRectWidth)
    }

    fun close(){
        doAnimator(mCenterRectWidth,0f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBlockSize = w / 2f
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        genMaskBitmap1(canvas)
//        canvas.drawBitmap(mMaskBitmap1 ?: return, -mAnimatorProgress, 0f, mPaint)
//        canvas.drawBitmap(mMaskBitmap1 ?: return, mBlockSize+mAnimatorProgress, 0f, mPaint)
        mPaint.drawXfermode(canvas, PorterDuff.Mode.DST_IN, src = {
            super.onDraw(canvas)
        }, dst = {
            canvas.drawBitmap(mMaskBitmap1 ?: return@drawXfermode,-mAnimatorProgress, 0f, mPaint)
//            canvas.drawBitmap(mMaskBitmap1 ?: return@drawXfermode, mBlockSize+mAnimatorProgress, 0f, mPaint)
        })
    }

    private fun genMaskBitmap1(canvas: Canvas) {
        mMaskBitmap1 =
            Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888) ?: return
        mMaskCanvas1 = Canvas(mMaskBitmap1!!)
        mMaskCanvas1?.drawRect(0f, 0f, mBlockSize, canvas.height.toFloat(), mPaint)
        mMaskCanvas1?.drawRect(mBlockSize+mAnimatorProgress+mAnimatorProgress, 0f, canvas.width.toFloat(), canvas.height.toFloat(), mPaint)
    }

    private fun doAnimator(start: Float, end: Float) {
        mDrawingAnimator?.cancel()
        mDrawingAnimator = ValueAnimator.ofFloat(start, end)
        mDrawingAnimator?.duration = 400
//        mDrawingAnimator?.interpolator = OvershootInterpolator()
        mDrawingAnimator?.interpolator = LinearInterpolator()
//        mDrawingAnimator?.interpolator = AccelerateDecelerateInterpolator()
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
}