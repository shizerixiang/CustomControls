package com.beviswang.customcontrols.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.beviswang.customcontrols.graphics.equation.LinearEquation
import com.beviswang.customcontrols.util.SlidingGestureDetector
import org.jetbrains.anko.collections.forEachReversedWithIndex
import java.lang.ref.WeakReference

/**
 * 拖影绘制
 * 问题：
 * 1、平行于 XY 轴的直线处理
 * @author BevisWang
 * @date 2019/9/27 13:52
 */
class SmearView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), SlidingGestureDetector.OnGestureListener {
    private var mSlidingGenericMotionListener = SlidingGestureDetector(context, this)
    private var mP0: PointF? = null
    private var mP1: PointF? = null
    private var mElementHandler: Handler = ElementHandler(this)
    private var mElements: ArrayList<Element> = ArrayList()

    init {
        mElementHandler.sendEmptyMessageDelayed(MSG_HANDLER_ELEMENT, 100)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) = mSlidingGenericMotionListener.onTouchEvent(event)

    override fun onDown(e: MotionEvent?): Boolean {
        if (e == null) return super.onDown(e)
        mP0 = PointF(e.x, e.y)
        return super.onDown(e)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e2 == null || mP0 == null) return super.onScroll(e1, e2, distanceX, distanceY)
        mP1 = PointF(e2.x, e2.y)
        if (mElements.size >= 1) mElements.add(Element(sLine = mElements[mElements.lastIndex].line2, sp = mP0!!, ep = mP1!!))
        else mElements.add(Element(sp = mP0!!, ep = mP1!!))
        if (mElements.size > 100) mElements.removeAt(0)
        invalidate()
        mP0 = mP1
        return true
    }

    override fun onRelease(e: MotionEvent?) {
        if (e == null || mP0 == null) return
        mP1 = PointF(e.x, e.y)
        if (mElements.size >= 1) mElements.add(Element(sLine = mElements[mElements.lastIndex].line2, sp = mP0!!, ep = mP1!!))
        else mElements.add(Element(sp = mP0!!, ep = mP1!!))
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        canvas.save()
        mElements.forEach { it.drawPath(canvas) }
        canvas.restore()
    }

    class ElementHandler(smearView: SmearView) : Handler() {
        private var mWeakView: WeakReference<SmearView> = WeakReference(smearView)

        override fun handleMessage(msg: Message?) {
            val smearView = mWeakView.get() ?: return
            when (msg?.what) {
                MSG_HANDLER_ELEMENT -> {
                    // 更新元素宽度
                    smearView.mElements.filter { it.distance <= 2 }.forEach { smearView.mElements.remove(it) }
                    smearView.mElements.forEach { it.updateDistance(it.distance - 1) }
                    smearView.invalidate()
                    sendEmptyMessageDelayed(MSG_HANDLER_ELEMENT, 20)
                }
            }
        }
    }

    /** 每个点的元素，本质是绘制线段两端的垂线，只有两点确定一个元素 */
    class Element(sLine: LinearEquation? = null, private val sp: PointF, private val ep: PointF, private val width: Float = 40f) {
        private var mLinearPath: Path = Path()
        private var mSVerticalPath: Path = Path()
        private var mEVerticalPath: Path = Path()
        private var mLinearEquation: LinearEquation // 元素的线性方程
        private var mLinearPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var line1: LinearEquation
        var line2: LinearEquation
        var distance: Float = width // 两点间的距离
        var alpha: Int = 255 // 画笔透明度

        init {
            mLinearPaint.color = Color.BLACK
            mLinearPaint.style = Paint.Style.FILL_AND_STROKE
            mLinearPaint.strokeWidth = 8f

            mLinearEquation = LinearEquation(sp, ep)
            mLinearEquation.getLinePath(mLinearPath)
            line1 = sLine ?: mLinearEquation.getVerticalLinearEquation(sp)
            line2 = mLinearEquation.getVerticalLinearEquation(ep)
            updateElement()
        }

        fun updateDistance(d: Float) {
            distance = d
            alpha = (255 * distance / width).toInt()
            updateElement()
        }

        private fun updateElement() {
            mLinearPaint.alpha = alpha
            line1.getPointsByDistance(sp, distance)
            line1.getLinePath(mSVerticalPath)
            line2.getPointsByDistance(ep, distance)
            line2.getLinePath(mEVerticalPath)
        }

        fun drawPath(canvas: Canvas) {
            canvas.drawPath(mLinearPath, mLinearPaint)
            canvas.drawPath(mSVerticalPath, mLinearPaint)
            canvas.drawPath(mEVerticalPath, mLinearPaint)
        }
    }

    companion object {
        private const val MSG_HANDLER_ELEMENT = 0x10
    }
}