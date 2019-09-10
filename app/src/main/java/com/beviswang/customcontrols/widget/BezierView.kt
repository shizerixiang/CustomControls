package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.beviswang.customcontrols.graphics.equation.LinearDynamicEquation
import com.beviswang.customcontrols.graphics.equation.LinearEquation

/**
 * 贝塞尔曲线演示
 * @author BevisWang
 * @date 2019/9/10 16:42
 */
class BezierView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
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
    private var mPoint01: PointF
    private var mPoint12: PointF
    private var mPoint23: PointF
    private var mPoint012: PointF
    private var mPoint123: PointF
    // 最终点
    private var mPoint0123: PointF
    // 进度 0f-1f
    private var mProgress: Float = 0f

    init {
        mLinearEquation01 = LinearEquation(mPoint0, mPoint1)
        mLinearEquation12 = LinearEquation(mPoint1, mPoint2)
        mLinearEquation23 = LinearEquation(mPoint2, mPoint3)

        mPoint01 = mLinearEquation01.getCurPoint(mProgress)
        mPoint12 = mLinearEquation12.getCurPoint(mProgress)
        mPoint23 = mLinearEquation23.getCurPoint(mProgress)

        mLinearEquation012 = LinearDynamicEquation(mPoint01, mPoint12)
        mLinearEquation123 = LinearDynamicEquation(mPoint12, mPoint23)

        mPoint012 = mLinearEquation012.getCurPoint(mProgress)
        mPoint123 = mLinearEquation123.getCurPoint(mProgress)

        mLinearEquation0123 = LinearDynamicEquation(mPoint012, mPoint123)

        mPoint0123 = mLinearEquation0123.getCurPoint(mProgress)
    }

    /** 更新点 */
    private fun updatePoint() {
        mPoint01 = mLinearEquation01.getCurPoint(mProgress)
        mPoint12 = mLinearEquation12.getCurPoint(mProgress)
        mPoint23 = mLinearEquation23.getCurPoint(mProgress)

        mPoint012 = mLinearEquation012.getCurPoint(mProgress)
        mPoint123 = mLinearEquation123.getCurPoint(mProgress)

        mPoint0123 = mLinearEquation0123.getCurPoint(mProgress)
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
    }
}