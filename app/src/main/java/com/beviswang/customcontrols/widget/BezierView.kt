package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.beviswang.customcontrols.graphics.equation.LinearDynamicEquation
import com.beviswang.customcontrols.graphics.equation.LinearEquation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * 贝塞尔曲线演示
 * @author BevisWang
 * @date 2019/9/10 16:42
 */
class BezierView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    // 动画
    private var mDrawingAnimator: ValueAnimator? = null
    // 画笔
    private var mPointPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 路径
//    private var mPath: Path = Path()
    private var mPath01: Path = Path()
    private var mPath12: Path = Path()
    private var mPath23: Path = Path()
    private var mPath012: Path = Path()
    private var mPath123: Path = Path()
    private var mPath0123: Path = Path()
    private var mPathBezier: Path = Path()
    // 静态方程
    private var mLinearEquation01: LinearEquation
    private var mLinearEquation12: LinearEquation
    private var mLinearEquation23: LinearEquation
    // 动态方程
    private var mLinearEquation012: LinearDynamicEquation
    private var mLinearEquation123: LinearDynamicEquation
    private var mLinearEquation0123: LinearDynamicEquation
    // 03 起终点 12 控制点
    private var mPoint0: PointF = PointF(-240f, 440f)
    private var mPoint1: PointF = PointF(-320f, -240f)
    private var mPoint2: PointF = PointF(240f, -240f)
    private var mPoint3: PointF = PointF(440f, 440f)
    // 根据进度计算的点
    private var mPoint01: PointF = PointF()
    private var mPoint12: PointF = PointF()
    private var mPoint23: PointF = PointF()
    private var mPoint012: PointF = PointF()
    private var mPoint123: PointF = PointF()
    // 最终点
    private var mPoint0123: PointF = PointF()
    // 进度 0f-1f
    private var mProgress: Float = 0f

    init {
        mPointPaint.style = Paint.Style.STROKE
        mPointPaint.strokeWidth = 20f
        mPointPaint.color = Color.BLACK
        mPointPaint.strokeCap = Paint.Cap.ROUND

        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.strokeWidth = 4f
        mLinePaint.color = Color.GRAY

        mLinearEquation01 = LinearEquation(mPoint0, mPoint1)
        mLinearEquation12 = LinearEquation(mPoint1, mPoint2)
        mLinearEquation23 = LinearEquation(mPoint2, mPoint3)

        mLinearEquation01.getCurPoint(mPoint01, mProgress)
        mLinearEquation12.getCurPoint(mPoint12, mProgress)
        mLinearEquation23.getCurPoint(mPoint23, mProgress)

        mLinearEquation012 = LinearDynamicEquation(mPoint01, mPoint12)
        mLinearEquation123 = LinearDynamicEquation(mPoint12, mPoint23)

        mLinearEquation012.getCurPoint(mPoint012, mProgress)
        mLinearEquation123.getCurPoint(mPoint123, mProgress)

        mLinearEquation0123 = LinearDynamicEquation(mPoint012, mPoint123)

        mLinearEquation0123.getCurPoint(mPoint0123, mProgress)

        updateLine()

        doAsync {
            Thread.sleep(800)
            uiThread { doDrawingAnimator() }
        }
    }

    /** 绘制动画 */
    private fun doDrawingAnimator() {
        mDrawingAnimator?.cancel()
        mDrawingAnimator = ValueAnimator.ofFloat(0f, 1f)
        mDrawingAnimator?.duration = 5000
        mDrawingAnimator?.interpolator = LinearInterpolator()
        mDrawingAnimator?.repeatCount = ValueAnimator.INFINITE
        mDrawingAnimator?.addUpdateListener {
            mProgress = it.animatedValue as Float
            invalidate()
        }
        mDrawingAnimator?.start()
    }

    /** 更新点 */
    private fun updatePoint() {
        mLinearEquation01.getCurPoint(mPoint01, mProgress)
        mLinearEquation12.getCurPoint(mPoint12, mProgress)
        mLinearEquation23.getCurPoint(mPoint23, mProgress)

        mLinearEquation012.getCurPoint(mPoint012, mProgress)
        mLinearEquation123.getCurPoint(mPoint123, mProgress)

        mLinearEquation0123.getCurPoint(mPoint0123, mProgress)

//        mPath.reset()
//        mPath.moveTo(mPoint0.x, mPoint0.y)
//        mPath.lineTo(mPoint1.x, mPoint1.y)
//        mPath.lineTo(mPoint2.x, mPoint2.y)
//        mPath.lineTo(mPoint3.x, mPoint3.y)
//
//        mPath.moveTo(mPoint01.x, mPoint01.y)
//        mPath.lineTo(mPoint12.x, mPoint12.y)
//        mPath.lineTo(mPoint23.x, mPoint23.y)
//
//        mPath.moveTo(mPoint012.x, mPoint012.y)
//        mPath.lineTo(mPoint123.x, mPoint123.y)
//
//        mPath.moveTo(mPoint0.x, mPoint0.y)
//        mPath.cubicTo(mPoint01.x, mPoint01.y, mPoint012.x, mPoint012.y, mPoint0123.x, mPoint0123.y)
    }

    private fun updateLine() {
        mLinearEquation01.getLinePath(mPath01)
        mLinearEquation12.getLinePath(mPath12)
        mLinearEquation23.getLinePath(mPath23)

        mLinearEquation012.getLinePath(mPath012)
        mLinearEquation123.getLinePath(mPath123)

        mLinearEquation0123.getLinePath(mPath0123)

        mPathBezier.reset()
        mPathBezier.moveTo(mPoint0.x, mPoint0.y)
        mPathBezier.cubicTo(mPoint01.x, mPoint01.y, mPoint012.x, mPoint012.y, mPoint0123.x, mPoint0123.y)
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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        canvas.translate(width / 2f, height / 2f)
        drawLine(canvas)
        drawPoint(canvas)
    }

    private fun drawLine(canvas: Canvas) {
        updateLine()

        mLinePaint.strokeWidth = 8f
        mLinePaint.color = Color.GRAY
        canvas.drawPath(mPath01, mLinePaint)
        canvas.drawPath(mPath12, mLinePaint)
        canvas.drawPath(mPath23, mLinePaint)

        mLinePaint.strokeWidth = 4f
        mLinePaint.color = Color.GREEN
        canvas.drawPath(mPath012, mLinePaint)
        canvas.drawPath(mPath123, mLinePaint)

        mLinePaint.color = Color.BLUE
        canvas.drawPath(mPath0123, mLinePaint)

        mLinePaint.strokeWidth = 8f
        mLinePaint.color = Color.RED
        canvas.drawPath(mPathBezier,mLinePaint)
    }

    private fun drawPoint(canvas: Canvas) {
        updatePoint()

        mPointPaint.strokeWidth = 4f
        mPointPaint.color = Color.BLACK
        canvas.drawCircle(mPoint0.x, mPoint0.y,10f, mPointPaint)
        canvas.drawCircle(mPoint1.x, mPoint1.y,10f, mPointPaint)
        canvas.drawCircle(mPoint2.x, mPoint2.y,10f, mPointPaint)
        canvas.drawCircle(mPoint3.x, mPoint3.y,10f, mPointPaint)

        mPointPaint.strokeWidth = 16f
        mPointPaint.color = Color.GREEN
        canvas.drawPoint(mPoint01.x, mPoint01.y, mPointPaint)
        canvas.drawPoint(mPoint12.x, mPoint12.y, mPointPaint)
        canvas.drawPoint(mPoint23.x, mPoint23.y, mPointPaint)

        mPointPaint.color = Color.BLUE
        canvas.drawPoint(mPoint012.x, mPoint012.y, mPointPaint)
        canvas.drawPoint(mPoint123.x, mPoint123.y, mPointPaint)

        mPointPaint.color = Color.BLACK
        canvas.drawPoint(mPoint0123.x, mPoint0123.y, mPointPaint)
    }
}