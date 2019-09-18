package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 音频频谱
 * @author BevisWang
 * @date 2019/9/17 15:48
 */
class SpectrumView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), Visualizer.OnDataCaptureListener {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    private val mBarPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var count: Int                                                   // 需要取点的数量
    private var mCountValue: FloatArray
    private var mDivisor: Int
    private var mAdjacentCount: Int
    private var mMinIndex = 0
    private var mMaxIndex = 0
    private var mCurValue = 0f
    private var mBarWidth = 0f
    private var mBarMaxHeight = 0
    private var fftLength = 128
    private var mOldCountValue: FloatArray
    private var mCurCountValue: FloatArray

    private var mAnimator: ValueAnimator? = null
    private var mProgress: Float = 0f

    private var mIsRush: Boolean = false

    init {
        mBarPaint.style = Paint.Style.FILL_AND_STROKE
        mBarPaint.strokeWidth = 12f
        mBarPaint.strokeCap = Paint.Cap.ROUND
        mBarPaint.color = Color.WHITE

        // 设置点数量
        count = 128
        mCountValue = FloatArray(count)
        mOldCountValue = FloatArray(count)
        mCurCountValue = FloatArray(count)
        mDivisor = fftLength / count
        mAdjacentCount = mDivisor / 2
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
        mBarWidth = w / count.toFloat()
        mBarMaxHeight = h
        mBarPaint.strokeWidth = mBarWidth / 1.4f
    }

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        if (fft == null) return
        notifySpectrum(fft)
    }

    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

    fun setRushAnimator() {
        mIsRush = true
    }

    fun setSoftAnimator() {
        mIsRush = false
    }

    /** 更新频谱 */
    private fun notifySpectrum(fft: ByteArray) {
        Log.e("notifySpectrum", "fftDataSize=${fft.size}")
        if (fft.size != fftLength) {
            fftLength = fft.size
            mDivisor = fftLength / count / 4
            mAdjacentCount = mDivisor / 2
        }
        if (mIsRush) getCountValueRush(fft)
        else getCountValueSoft(fft)
        doAnimator()
    }

    /** 缓和算法 */
    private fun getCountValueSoft(fft: ByteArray) {
        (0 until count).forEach { i ->
            mCurValue = 0f
            mMinIndex = i * mDivisor - mAdjacentCount
            if (mMinIndex < 0) mMinIndex = 0
            mMaxIndex = i * mDivisor + mAdjacentCount
            if (mMaxIndex > fftLength) mMaxIndex = fftLength
            (mMinIndex until mMaxIndex).forEach { j -> mCurValue += Math.hypot(fft[j * 2].toDouble(), fft[j * 2 + 1].toDouble()).toFloat() }
            mOldCountValue[i] = mCountValue[i]  // 记录上次的值
            mCountValue[i] = (mCurValue / (mMaxIndex - mMinIndex)) / 96f * mBarMaxHeight
        }
    }

    /** 湍急算法 */
    private fun getCountValueRush(fft: ByteArray) {
        mCountValue[0] = Math.abs(fft[0].toInt()) / 128f * mBarMaxHeight
        (1 until count).forEach { i ->
            mCountValue[i] = Math.hypot(fft[i * mDivisor].toDouble(), fft[i * mDivisor + 1].toDouble()).toFloat() / 128f * mBarMaxHeight
        }
    }

    /** 做衔接动画 */
    private fun doAnimator() {
        // 衔接上次进度
        if (mAnimator?.isRunning == true) mCurCountValue.forEachIndexed { i, it -> mOldCountValue[i] = it }
        mAnimator?.cancel()
        mAnimator = ValueAnimator.ofFloat(0f, 1f)
        mAnimator?.addUpdateListener {
            mProgress = it.animatedValue as Float
            if (mProgress < 1f) {
                invalidate()
                return@addUpdateListener
            }
            // 主要动画结束，没有下一个任务直接返回起点，即：高度为 0
            mCountValue.forEachIndexed { i, v ->
                mOldCountValue[i] = v
                mCountValue[i] = 0f
            }
            doFinishAnimator()
        }
        mAnimator?.interpolator = LinearInterpolator()
        mAnimator?.duration = 110
        mAnimator?.start()
    }

    /** 做结束动画 */
    private fun doFinishAnimator() {
        // 衔接上次进度
        if (mAnimator?.isRunning == true) mCurCountValue.forEachIndexed { i, it -> mOldCountValue[i] = it }
        mAnimator?.cancel()
        mAnimator = ValueAnimator.ofFloat(0f, 1f)
        mAnimator?.addUpdateListener {
            mProgress = it.animatedValue as Float
            invalidate()
        }
        mAnimator?.interpolator = LinearInterpolator()
        mAnimator?.duration = 60
        mAnimator?.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        drawBar(canvas)
    }

    private fun drawBar(canvas: Canvas) {
        canvas.save()
        canvas.translate(0f, height / 2f)
        var x: Float
        var y: Float
        (0 until count).forEach {
            x = it * mBarWidth + (mBarPaint.strokeWidth / 2) + 1
            y = (mCountValue[it] - mOldCountValue[it]) * mProgress + mOldCountValue[it]
            mCurCountValue[it] = y
            canvas.drawLine(x, -2f, x, -y, mBarPaint)
            canvas.drawLine(x, 2f, x, y, mBarPaint)
//            canvas.drawLine(-x, -2f, -x, -y, mBarPaint)
//            canvas.drawLine(-x, 2f, -x, y, mBarPaint)
        }
        canvas.restore()
    }
}