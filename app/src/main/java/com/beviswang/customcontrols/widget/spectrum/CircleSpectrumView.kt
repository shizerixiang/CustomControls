package com.beviswang.customcontrols.widget.spectrum

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.beviswang.customcontrols.graphics.equation.LinearDynamicEquation

/**
 * 圆的频谱
 * @author BevisWang
 * @date 2019/9/19 15:38
 */
class CircleSpectrumView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : VisualizerView(context, attrs, def) {
    private val mBarPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRadius: Int = 200 // 圆半径
    private val mCirclePath = Path()  // 圆路径
    private val mAmbPath = Path()
    private lateinit var mPathMeasure: PathMeasure
    private var mPathLength: Float = 0f

    private var mBarWidth: Float = 0f

    private val pos = FloatArray(2)
    private val tan = FloatArray(2)

    private var mStartPoint: PointF = PointF()
    private var mEndPoint: PointF = PointF()
    private var mLinearDynamicEquation: LinearDynamicEquation = LinearDynamicEquation(PointF(0f, 0f), mStartPoint)

    private var mWaveDis: Float = 1.04f

    private var mWaveEnable: Boolean = true     // 启用外侧波纹线条
    private var mBarEnable: Boolean = true     // 启用线条频谱线条
    private var mCircleEnable: Boolean = true  // 启用圆线条

    private var mAutoBarWidth: Boolean = false   // 自适应频谱线条宽度

    init {
        mBarPaint.style = Paint.Style.FILL_AND_STROKE
        mBarPaint.strokeWidth = 12f
        mBarPaint.strokeCap = Paint.Cap.ROUND
        mBarPaint.color = Color.WHITE

        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.strokeWidth = 6f
        mCirclePaint.strokeCap = Paint.Cap.ROUND
        mCirclePaint.color = Color.WHITE
        mCirclePaint.pathEffect = CornerPathEffect(16f)

        setPointCount(64)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = if (w < h) w / 4 else h / 4

        mCirclePath.reset()
        mCirclePath.addCircle(0f, 0f, mRadius.toFloat(), Path.Direction.CCW)

        mPathMeasure = PathMeasure(mCirclePath, false)
        mPathLength = mPathMeasure.length

        mBarWidth = mPathLength / count
        setMaxValue((mRadius * 1.2f).toInt())

        mBarPaint.strokeWidth = if (mAutoBarWidth) mBarWidth / 1.4f else 6f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        mAmbPath.reset()
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        drawCircle(canvas)
        (0 until count).forEach {
            getCirclePoint(it)
            mStartPoint.x = pos[0]
            mStartPoint.y = pos[1]
            mCurCountValue[it] = (mNewCountValue[it] - mOldCountValue[it]) * getProgress() + mOldCountValue[it]
            mEndPoint = mLinearDynamicEquation.getP1ToP2DistancePoint(mCurCountValue[it])
            drawBar(canvas)
            drawWave(canvas, it)
        }
        canvas.restore()
    }

    /** 绘制圆 */
    private fun drawCircle(canvas: Canvas) {
        if (!mCircleEnable) return
        canvas.drawPath(mCirclePath, mCirclePaint)
    }

    /** 绘制频谱线条 */
    private fun drawBar(canvas: Canvas) {
        if (!mBarEnable) return
        canvas.drawLine(mStartPoint.x, mStartPoint.y, mEndPoint.x, mEndPoint.y, mBarPaint)
    }

    /** 绘制外侧波纹线条 */
    private fun drawWave(canvas: Canvas, it: Int) {
        if (!mWaveEnable) return
        when (it) {
            0 -> mAmbPath.moveTo(mEndPoint.x * mWaveDis, mEndPoint.y * mWaveDis)
            count - 1 -> {
                mAmbPath.close()
                canvas.drawPath(mAmbPath, mCirclePaint)
            }
            else -> mAmbPath.lineTo(mEndPoint.x * mWaveDis, mEndPoint.y * mWaveDis)
        }
    }

    /** 获取圆上的绘制点 */
    private fun getCirclePoint(index: Int): PointF {
        mPathMeasure.getPosTan((index + 1f) / count * mPathLength, pos, tan)
        return PointF(pos[0], pos[1])
    }

    /** 启用外侧波纹线条 */
    fun enableWaveLine(enable: Boolean) {
        mWaveEnable = enable
    }

    /** 启用频谱线条 */
    fun enableBarLine(enable: Boolean) {
        mBarEnable = enable
    }

    /** 启用圆 */
    fun enableCircleLine(enable: Boolean) {
        mCircleEnable = enable
    }

    /** 自适应频谱线条宽度 */
    fun autoBarWidth(auto: Boolean) {
        mAutoBarWidth = auto
    }
}