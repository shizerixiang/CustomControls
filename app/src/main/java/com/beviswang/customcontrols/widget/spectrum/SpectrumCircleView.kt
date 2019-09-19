package com.beviswang.customcontrols.widget.spectrum

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.beviswang.customcontrols.graphics.equation.LinearDynamicEquation

/**
 * 圆形频谱
 * @author BevisWang
 * @date 2019/9/19 15:38
 */
class SpectrumCircleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : VisualizerView(context, attrs, def) {
    private val mBarPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRadius: Int = 200 // 圆半径
    private val mPath = Path()
    private lateinit var mPathMeasure: PathMeasure
    private var mPathLength: Float = 0f

    private var mBarWidth: Float = 0f

    init {
        mBarPaint.style = Paint.Style.FILL_AND_STROKE
        mBarPaint.strokeWidth = 12f
        mBarPaint.strokeCap = Paint.Cap.ROUND
        mBarPaint.color = Color.WHITE

        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.strokeWidth = 8f
        mCirclePaint.strokeCap = Paint.Cap.ROUND
        mCirclePaint.color = Color.WHITE

        setPointCount(64)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = if (w < h) w / 4 else h / 4

        mPath.reset()
        mPath.addCircle(0f, 0f, mRadius.toFloat(), Path.Direction.CCW)

        mPathMeasure = PathMeasure(mPath, false)
        mPathLength = mPathMeasure.length

        mBarWidth = mPathLength / count
        setMaxValue((mRadius*1.6f).toInt())

        mBarPaint.strokeWidth = mBarWidth / 1.4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        drawCircle(canvas)
    }

    private val pos = FloatArray(2)
    private val tan = FloatArray(2)

    private var mStartPoint: PointF = PointF()
    private var mEndPoint: PointF = PointF()
    private var mLinearDynamicEquation: LinearDynamicEquation = LinearDynamicEquation(PointF(0f, 0f), mStartPoint)

    private fun drawCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.drawPath(mPath, mCirclePaint)
        (0 until count).forEach {
            getCirclePoint(it)
            mStartPoint.x = pos[0]
            mStartPoint.y = pos[1]
            mCurCountValue[it] = (mNewCountValue[it] - mOldCountValue[it]) * getProgress() + mOldCountValue[it]
            mEndPoint = mLinearDynamicEquation.getP1ToP2DistancePoint(mCurCountValue[it])
            canvas.drawLine(mStartPoint.x, mStartPoint.y, mEndPoint.x, mEndPoint.y, mBarPaint)
        }
        canvas.restore()
    }

    /** 获取圆上的绘制点 */
    private fun getCirclePoint(index: Int): PointF {
        mPathMeasure.getPosTan((index + 1f) / count * mPathLength, pos, tan)
        return PointF(pos[0], pos[1])
    }
}