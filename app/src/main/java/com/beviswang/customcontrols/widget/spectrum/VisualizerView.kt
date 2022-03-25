package com.beviswang.customcontrols.widget.spectrum

import android.animation.ValueAnimator
import android.content.Context
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.beviswang.customcontrols.loge
import java.lang.StringBuilder

open class VisualizerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), Visualizer.OnDataCaptureListener {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    protected var count: Int                            // 需要取点的数量
    private var mDivisor: Int = 0
    private var mAdjacentCount: Int = 0
    private var mMinIndex = 0
    private var mMaxIndex = 0
    private var mCurValue = 0f
    private var fftLength = 128
    private var mBarMaxHeight = 0                       // 每个点的最大值
    protected lateinit var mNewCountValue: FloatArray   // 新数据
    protected lateinit var mOldCountValue: FloatArray   // 旧数据
    protected lateinit var mCurCountValue: FloatArray   // 当前绘制的数据（动画变化的值）

    private var mAnimator: ValueAnimator? = null        // 执行的动画
    private var mProgress: Float = 0f                   // 动画进度

    private var mIsRush: Boolean = false                // 处理数据的模式，是否为湍急模式（未处理的数据，反之为处理后 - 平均化 - 的数据）

    private var mIsDetached: Boolean = false

    init {
        // 设置点数量
        count = 32
        updateCount()
    }

    private fun updateCount() {
        mNewCountValue = FloatArray(count)
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
        mBarMaxHeight = h
    }

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        if (fft == null || mIsDetached) return
        notifySpectrum(fft)
//        val str = StringBuilder("fft[]： ")
//        fft.map { it.toInt() }.forEach { str.append("$it,") }
//        loge(str.toString())
    }

    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

    fun setRushAnimator() {
        mIsRush = true
    }

    fun setSoftAnimator() {
        mIsRush = false
    }

    fun pause() {
        mIsDetached = true
    }

    fun resume() {
        mIsDetached = false
    }

    /** 设置关键点数量 */
    fun setPointCount(c: Int) {
        this.count = c
        updateCount()
    }

    /** 更新频谱 */
    private fun notifySpectrum(fft: ByteArray) {
//        Log.e("notifySpectrum", "fftDataSize=${fft.size}")
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
    private fun getCountValueSoft(fft: ByteArray) = (0 until count).forEach { i ->
        mCurValue = 0f
        mMinIndex = i * mDivisor - mAdjacentCount
        if (mMinIndex < 0) mMinIndex = 0
        mMaxIndex = i * mDivisor + mAdjacentCount
        if (mMaxIndex > fftLength) mMaxIndex = fftLength
        (mMinIndex until mMaxIndex).forEach { j -> mCurValue += Math.hypot(fft[j * 2].toDouble(), fft[j * 2 + 1].toDouble()).toFloat() }
        mOldCountValue[i] = mNewCountValue[i]  // 记录上次的值
        mNewCountValue[i] = (mCurValue / (mMaxIndex - mMinIndex)) / 96f * mBarMaxHeight
    }

    /** 湍急算法 */
    private fun getCountValueRush(fft: ByteArray) {
        mNewCountValue[0] = Math.abs(fft[0].toInt()) / 128f * mBarMaxHeight
        (1 until count).forEach { i ->
            mNewCountValue[i] = Math.hypot(fft[i * mDivisor].toDouble(), fft[i * mDivisor + 1].toDouble()).toFloat() / 128f * mBarMaxHeight
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
            mNewCountValue.forEachIndexed { i, v ->
                mOldCountValue[i] = v
                mNewCountValue[i] = 0f
            }
            doFinishAnimator()
        }
        mAnimator?.interpolator = LinearInterpolator()
        mAnimator?.duration = 80
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mIsDetached = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIsDetached = false
    }

    /** 获取动画进度 */
    protected fun getProgress() = mProgress

    /** 设置每个点的最大值（数组中的值可以直接拿来用，不需要过多的绘制计算） */
    protected fun setMaxValue(v: Int) {
        mBarMaxHeight = v
    }
}