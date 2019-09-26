package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.annotation.ColorInt
import com.beviswang.customcontrols.graphics.PointHelper
import com.beviswang.customcontrols.loge
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
    private var mProgress: Float = 0f // 当前进度
    private var mTrueProgress: Float = 0f // 非动画进度，即真实进度值（实时）

    private var mStartAngle: Float = 0f // 圆环起始点

    private var mProgressMaxRadius: Float = 0f
    private var mProgressRadius: Float = 0f
    private var mProgressRectF: RectF = RectF()

    private var isShowProgress: Boolean = false // 是否显示进度条
    private var mProgressShowAnimator: ValueAnimator? = null // 进度条显示隐藏动画
    private var mProgressShowAnimatorProgress: Float = 0f // 动画的进度

    private var mCurPointDegree: Float = 0f // 当前角度
    private var mLastProgress: Float = 0f // 上次进度
    private var mTouchDegrees = 0f  // 手指滑动的角度，未滑动或滑动进度不足一周都为0，满一周则为 360

    private var mResetAnimator: ValueAnimator? = null // 进度条重置动画
    private var mSpeed: Float = 1.2f // 跑进度动画的速度 degree/ms

    private var mProgressAnimator: ValueAnimator? = null

    private var mSeekBarListener: OnProgressChangeListener? = null

    private var isLongPress: Boolean = false // 长按判定
    private var isPress: Boolean = false // 拖动判定

    init {
        mProgressBgPaint.style = Paint.Style.STROKE
        mProgressBgPaint.color = Color.parseColor("#ffffff")
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
//        mProgressMaxRadius = if (w > h) h / 3f else w / 3f
        mProgressMaxRadius = 140f
        mProgressPaintWidth = mProgressMaxRadius / 3f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) = mSlidingGestureDetector.onTouchEvent(event)

    override fun onDown(e: MotionEvent?): Boolean {
//        mLastProgress = mProgress // 记录进度
        if (e == null) return true
        if (!isShowProgress) mCenterPoint.set(e.x, e.y)
        if (mProgressShowAnimator?.isRunning == true) return true
        mProgressShowAnimator?.cancel()
        handlerTouch(e)
        mSeekBarListener?.onStartTouch(this)
        if (isShowProgress && mProgressShowAnimatorProgress == 1f) isPress = true
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        isLongPress = true
        showProgress()
    }

    override fun onRelease(e: MotionEvent?) {
        hideProgress()
        isPress = false
        if (isLongPress) {
            isLongPress = false
            return
        }
        if (updateTrueProgress(e)) return
        mSeekBarListener?.onStopTouch(this)
    }

    private fun updateTrueProgress(e: MotionEvent?): Boolean {
        if (e == null || !isShowProgress || mProgressShowAnimatorProgress != 1f) return true
        mCurPointDegree = PointHelper.getPointDegree(mCenterPoint, PointF(e.x, e.y), 90f)
        mTrueProgress = mCurPointDegree / 360f
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val isHandler = handlerTouch(e2)
        mSeekBarListener?.onProgressChanged(this, mProgress, true)
        return isHandler
    }

    private fun handlerTouch(e2: MotionEvent?): Boolean {
        // 未显示、动画未执行完时直接不处理手势
        if (e2 == null || !isShowProgress || mProgressShowAnimatorProgress != 1f) return true
        // 正在执行进度动画
        if (mProgressAnimator?.isRunning == true) return true
        mCurPointDegree = PointHelper.getPointDegree(mCenterPoint, PointF(e2.x, e2.y), 90f)
        mProgress = mCurPointDegree / 360f
        mTrueProgress = mProgress
        if (mStartAngle != 0f) return true
        loge("当前进度：$mProgress")
        // 当前滑动超过一周
        if (mProgress < 0.2f && mLastProgress > 0.8f) doResetAnimator()
        // 当前滑动距离过大
        else if (Math.abs(mProgress - mLastProgress) > 0.2) {
            doProgressAnimator(mLastProgress, mProgress)
            return true
        }
        // 没有特殊情况
        else invalidate()
        mLastProgress = mProgress
        return true
    }

    private fun showProgress() {
        isShowProgress = true
        if (!isShowProgress || mProgressShowAnimatorProgress == 1f) return
        doStateAnimator(1f)
    }

    private fun hideProgress() {
        if (!isShowProgress || mProgressShowAnimatorProgress == 0f) return
        doStateAnimator(0f, 1800)
    }

    private fun doStateAnimator(endValue: Float, delay: Long = 0) {
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

    private fun doResetAnimator() {
        if (mStartAngle != 0f) return
        mResetAnimator?.cancel()
        var progress = 0f
        mStartAngle = 1f
        mResetAnimator = ValueAnimator.ofFloat(0f, 1f)
        mResetAnimator?.duration = (360f / mSpeed).toLong()
        mTouchDegrees = 360f
        mResetAnimator?.addUpdateListener {
            progress = it.animatedValue as Float
            mStartAngle = progress * 360f
            if (progress == 1f) {
                mTouchDegrees = 0f
                mStartAngle = 0f
            }
            invalidate()
        }
        mResetAnimator?.interpolator = LinearInterpolator()
        mResetAnimator?.start()
    }

    private fun doProgressAnimator(startProgress: Float, endProgress: Float) {
        mProgressAnimator?.cancel()
        mProgressAnimator = ValueAnimator.ofFloat(startProgress, endProgress)
        // 动画时间通过进度计算，定义一个速度值，单位 degree/ms
        mProgressAnimator?.duration = (360f * Math.abs(endProgress - startProgress) / mSpeed).toLong()
        mProgressAnimator?.addUpdateListener {
            mProgress = it.animatedValue as Float
            mSeekBarListener?.onProgressChanged(this, mProgress, true)
            invalidate()
            mLastProgress = mProgress
        }
        mProgressAnimator?.interpolator = LinearInterpolator()
        mProgressAnimator?.start()
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
        mProgressBgPaint.alpha = (255 * mProgressShowAnimatorProgress).toInt()
        mProgressPaint.alpha = (255 * mProgressShowAnimatorProgress).toInt()
        canvas.drawArc(mProgressRectF, 0f, 360 * mProgressShowAnimatorProgress, false, mProgressBgPaint)
        canvas.drawArc(mProgressRectF, mStartAngle, 360 * mProgress * mProgressShowAnimatorProgress + mTouchDegrees - mStartAngle, false, mProgressPaint)
    }

    private fun resizeRectFByRadius(radius: Float) {
        mProgressRectF.set(-radius, -radius, radius, radius)
    }

    /** 主动隐藏进度条 */
    fun hide() = doStateAnimator(0f)

    /** 主动显示进度条 */
    fun show() = showProgress()

    /** 设置进度值 百分比 */
    fun setProgress(p: Float) {
        mProgress = p
        mTrueProgress = mProgress
        if (!isPress) invalidate()
        mSeekBarListener?.onProgressChanged(this, mProgress, false)
    }

    /** 设置进度条颜色 */
    fun setProgressColor(@ColorInt color: Int) {
        mProgressPaint.color = color
    }

    /** 获取进度值 */
    fun getProgress() = mTrueProgress

    /** 监听进度 */
    fun addSeekBarChangedListener(listener: OnProgressChangeListener) {
        mSeekBarListener = listener
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(tpv: TouchProgressView, progress: Float, fromUser: Boolean)
        fun onStartTouch(tpv: TouchProgressView)
        fun onStopTouch(tpv: TouchProgressView)
    }
}