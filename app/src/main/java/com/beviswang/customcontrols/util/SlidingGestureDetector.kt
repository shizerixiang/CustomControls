package com.beviswang.customcontrols.util

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * 增强版滑动手势
 * @author BevisWang
 * @date 2018/11/14 16:12
 */
class SlidingGestureDetector constructor(context: Context, private val listener: OnGestureListener)
    : GestureDetector.SimpleOnGestureListener() {
    private val mGestureDetector = GestureDetector(context, this)
    private var mIsFirstScroll: Boolean = true
    private var mSlideDirection: Int = 0            // 当前手指滑动方向是水平还是垂直

    /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the [OnGestureListener] supplied.
     *
     * @param ev The current motion event.
     * @return true if the [OnGestureListener] consumed the event,
     *              else false.
     */
    fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) listener.onRelease(ev) // 松手事件
        return mGestureDetector.onTouchEvent(ev)
    }

    override fun onShowPress(e: MotionEvent?) = listener.onShowPress(e)

    override fun onSingleTapUp(e: MotionEvent?): Boolean = listener.onSingleTapUp(e)

    override fun onDown(e: MotionEvent?): Boolean {
        mIsFirstScroll = true
        mSlideDirection = 0
        return listener.onDown(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean = listener.onDoubleTap(e)

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean = listener.onDoubleTapEvent(e)

    override fun onContextClick(e: MotionEvent?): Boolean = listener.onContextTap(e)

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean = listener.onSingleTapConfirmed(e)

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float)
            : Boolean = listener.onFling(e1, e2, velocityX, velocityY)

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null || e2 == null) return false
        val distance = getSlideDistance(e1, e2)
        // 根据方向进行回调
        when (mSlideDirection) {
            HORIZONTAL -> listener.onHorizontalSlide(distance, e1.x)
            VERTICAL -> listener.onVerticalSlide(distance, e1.x)
            else -> return false
        }
        if (mIsFirstScroll) mIsFirstScroll = false
        return listener.onScroll(e1, e2, distanceX, distanceY)
    }

    /**
     * 获取滑动距离
     * @param e1 起始点事件
     * @param e2 当前点事件
     * @return 滑动的距离
     */
    private fun getSlideDistance(e1: MotionEvent, e2: MotionEvent): Float {
        val disX = e1.x - e2.x
        val disY = e1.y - e2.y
        // 未对方向进行判断时，对滑动方向进行判断
        if (mSlideDirection == 0 && mIsFirstScroll) {
            mSlideDirection = if (Math.abs(disX) > Math.abs(disY)) HORIZONTAL else VERTICAL
        }
        return when (mSlideDirection) {
            HORIZONTAL -> disX
            VERTICAL -> disY
            else -> 0f
        }
    }

    override fun onLongPress(e: MotionEvent?) = listener.onLongPress(e)

    /** 增强版滑动手势监听器 */
    interface OnGestureListener : GestureDetector.OnGestureListener {
        /**
         * 水平滑动
         * @param distance 滑动距离
         * @param hx 起始点 x 轴的坐标
         */
        fun onHorizontalSlide(distance: Float, hx: Float) {}

        /**
         * 垂直滑动
         * @param distance 滑动距离
         * @param vx 起始点 y 轴的坐标
         */
        fun onVerticalSlide(distance: Float, vx: Float) {}

        /**
         * 双击事件
         * @param e 事件
         */
        fun onDoubleTap(e: MotionEvent?): Boolean = false

        /**
         * 全文点击事件
         * @param e 事件
         */
        fun onContextTap(e: MotionEvent?): Boolean = false

        /**
         * 确定性的单击
         * @param e 事件
         */
        fun onSingleTapConfirmed(e: MotionEvent?): Boolean = false

        /**
         * 双击事件
         * @param e 事件
         */
        fun onDoubleTapEvent(e: MotionEvent?): Boolean = false

        /**
         * 松开手势
         * @param e 事件
         */
        fun onRelease(e: MotionEvent?) {}

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = false

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = false

        override fun onLongPress(e: MotionEvent?) {}

        override fun onDown(e: MotionEvent?): Boolean = true

        override fun onShowPress(e: MotionEvent?) {}

        override fun onSingleTapUp(e: MotionEvent?): Boolean = false
    }

    companion object {
        const val VERTICAL = 0          // 垂直
        const val HORIZONTAL = 1        // 水平
    }
}