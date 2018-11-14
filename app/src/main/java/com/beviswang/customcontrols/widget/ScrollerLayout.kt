package com.beviswang.customcontrols.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import android.widget.Scroller

/**
 * 跟手滑动控件
 * @author BevisWang
 * @date 2018/11/14 16:12
 */
class ScrollerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : RelativeLayout(context, attrs, def) {
    private var mScroller: Scroller = Scroller(context)

    private var mLastX: Int = 0
    private var mLastY: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return super.onTouchEvent(event)
        }
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mLastX
                val deltaY = y - mLastY
                translationX += deltaX
                translationY += deltaY
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        mLastX = x
        mLastY = y
        return true
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            postInvalidate()
        }
    }
}