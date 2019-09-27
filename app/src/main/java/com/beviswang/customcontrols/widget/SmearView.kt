package com.beviswang.customcontrols.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.beviswang.customcontrols.graphics.equation.LinearEquation
import com.beviswang.customcontrols.util.SlidingGestureDetector

/**
 * 拖影绘制
 * @author BevisWang
 * @date 2019/9/27 13:52
 */
class SmearView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), SlidingGestureDetector.OnGestureListener {
    private var mSlidingGenericMotionListener = SlidingGestureDetector(context, this)
    private var mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG) // 拖影画笔
    private var mElement: Element? = null // 拖影元素
    private var mP0: PointF? = null
    private var mP1: PointF? = null

    init {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) = mSlidingGenericMotionListener.onTouchEvent(event)

    override fun onRelease(e: MotionEvent?) {
        if (e == null) return
        if (mP0 == null) {
            mP0 = PointF(e.x, e.y)
            return
        }
        mP1 = PointF(e.x, e.y)
        mElement = Element(mP0!!, mP1!!)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        canvas.save()
        mElement?.drawPath(canvas)
        canvas.restore()
    }

    /** 每个点的元素，本质是绘制线段两端的垂线，只有两点确定一个元素 */
    class Element(private val sp: PointF, private val ep: PointF) {
        private var mLinearPath: Path = Path()
        private var mSVerticalPath: Path = Path()
        private var mEVerticalPath: Path = Path()
        private var mLinearEquation: LinearEquation // 元素的线性方程
        private var mLinearPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var mDistance: Float = 40f // 两点间的距离
        private var mLength: Float = 0f // 元素的线段长度

        init {
            mLinearPaint.color = Color.BLACK
            mLinearPaint.style = Paint.Style.FILL_AND_STROKE
            mLinearPaint.strokeWidth = 8f

            mLinearEquation = LinearEquation(sp, ep)
            mLinearEquation.getLinePath(mLinearPath)
            val line1 = mLinearEquation.getVerticalLinearEquation(sp)
            line1.getPointsByDistance(sp, mDistance)
            line1.getLinePath(mSVerticalPath)
            val line2 = mLinearEquation.getVerticalLinearEquation(ep)
            line2.getPointsByDistance(ep, mDistance)
            line2.getLinePath(mEVerticalPath)
        }

        fun drawPath(canvas: Canvas) {
            canvas.drawPath(mLinearPath, mLinearPaint)
            canvas.drawPath(mSVerticalPath, mLinearPaint)
            canvas.drawPath(mEVerticalPath, mLinearPaint)
        }
    }
}