package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.beviswang.customcontrols.graphics.PointHelper
import com.beviswang.customcontrols.util.SlidingGestureDetector

/**
 * 进度条小工具
 * @author BevisWang
 * @date 2019/9/21 13:31
 */
class TouchProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), SlidingGestureDetector.OnGestureListener {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    private val mCenterPoint: PointF = PointF()
    private val mSlidingGestureDetector = SlidingGestureDetector(context, this)
    private var mProgressBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG) // 总进度画笔
    private var mProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG) // 当前进度画笔
    private var mProgressPaintWidth: Float = 0f // 画笔宽度
    private var mProgress: Float = 0.4f // 当前进度

    private var mStartAngle: Float = 0f // 圆环起始点

    private var mProgressMaxRadius: Float = 0f
    private var mProgressRadius: Float = 0f
    private var mProgressRectF: RectF = RectF()

    private var isShowProgress: Boolean = false // 是否显示进度条
    private var mProgressShowAnimator: ValueAnimator? = null // 进度条显示隐藏动画
    private var mProgressShowAnimatorProgress: Float = 0f // 动画的进度

    init {
        mProgressBgPaint.style = Paint.Style.STROKE
        mProgressBgPaint.color = Color.GRAY
        mProgressBgPaint.alpha = 128
        mProgressBgPaint.strokeWidth = 40f
        mProgressBgPaint.strokeCap = Paint.Cap.ROUND

        mProgressPaint.style = Paint.Style.STROKE
        mProgressPaint.color = Color.RED
        mProgressPaint.strokeWidth = 40f
        mProgressPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 针对 wrap_content 的处理，使 wrap_content 生效
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefWidth, mDefHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterPoint.set(w / 2f, h / 2f)
        mProgressMaxRadius = if (w > h) h / 3f else w / 3f
        mProgressPaintWidth = mProgressMaxRadius / 2f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) = mSlidingGestureDetector.onTouchEvent(event)

    override fun onDown(e: MotionEvent?): Boolean {
        if (mProgressShowAnimator?.isRunning == true) return true
        mProgressShowAnimator?.cancel()
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        showProgress()
    }

    override fun onRelease(e: MotionEvent?) {
        hideProgress()
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e2 == null || !isShowProgress || mProgressShowAnimatorProgress != 1f) return true
        if (mProgress > 0.9f && mStartAngle != 0f) return true
        mProgress = PointHelper.getPointDegree(mCenterPoint, PointF(e2.x, e2.y), 90f) / 360f
        if (mProgress > 0.9f) doResetAnimator() else invalidate()
        return true
    }

    private fun showProgress() {
        isShowProgress = true
        if (!isShowProgress || mProgressShowAnimatorProgress == 1f) return
        doProgressAnimator(1f)
    }

    private fun hideProgress() {
        if (!isShowProgress || mProgressShowAnimatorProgress == 0f) return
        doProgressAnimator(0f, 800)
    }

    private fun doProgressAnimator(endValue: Float, delay: Long = 0) {
        mProgressShowAnimator?.cancel()
        mProgressShowAnimator = ValueAnimator.ofFloat(mProgressShowAnimatorProgress, endValue)
        mProgressShowAnimator?.duration = 240
//        mProgressShowAnimator?.interpolator = LinearInterpolator()
        mProgressShowAnimator?.addUpdateListener {
            mProgressShowAnimatorProgress = it.animatedValue as Float
            isShowProgress = mProgressShowAnimatorProgress != 0f
            invalidate()
        }
        if (delay != 0L && mProgressShowAnimatorProgress == 1f) mProgressShowAnimator?.startDelay = delay
        mProgressShowAnimator?.start()
    }

    private var mResetAnimator: ValueAnimator? = null

    private fun doResetAnimator() {
        if (mStartAngle != 0f) return
        mResetAnimator?.cancel()
        var progress = 0f
        mStartAngle = 1f
        mResetAnimator = ValueAnimator.ofFloat(0f, 1f)
        mResetAnimator?.duration = 240
        mResetAnimator?.addUpdateListener {
            progress = it.animatedValue as Float
            mStartAngle = progress * 360f
            if (progress > 0.99f) mStartAngle = 0f
            invalidate()
        }
        mResetAnimator?.interpolator = LinearInterpolator()
        mResetAnimator?.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        canvas.save()
        canvas.translate(mCenterPoint.x, mCenterPoint.y)
        canvas.rotate(-90f)
        drawProgress(canvas)
        canvas.restore()
    }

    private fun drawProgress(canvas: Canvas) {
        if (!isShowProgress) return
        mProgressRadius = mProgressMaxRadius * mProgressShowAnimatorProgress
        resizeRectFByRadius(mProgressRadius)
        mProgressBgPaint.strokeWidth = mProgressPaintWidth * mProgressShowAnimatorProgress
        mProgressPaint.strokeWidth = mProgressPaintWidth * mProgressShowAnimatorProgress
        mProgressBgPaint.alpha = (128 * mProgressShowAnimatorProgress).toInt()
        mProgressPaint.alpha = (255 * mProgressShowAnimatorProgress).toInt()
        canvas.drawArc(mProgressRectF, 0f, 359 * mProgressShowAnimatorProgress, false, mProgressBgPaint)
        canvas.drawArc(mProgressRectF, mStartAngle, 359 * mProgress * mProgressShowAnimatorProgress, false, mProgressPaint)
    }

    private fun resizeRectFByRadius(radius: Float) {
        mProgressRectF.set(-radius, -radius, radius, radius)
    }

    /** 主动隐藏进度条 */
    fun hide() = doProgressAnimator(0f)

    /** 主动显示进度条 */
    fun show() = showProgress()

    /** 设置进度值 */
    fun setProgress(p: Float) {
        mProgress = p
    }
}