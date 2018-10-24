package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller

/**
 * 自定义缩放 TabView
 * 内部需要实现一个 id 为 text1 的 TextView 以及一个 id 为 icon 的 ImageView
 * @author BevisWang
 */
class ZoomTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0) :
        ViewGroup(context, attrs, def) {
    private var mItemArray: Array<String>? = null
    private var mClazz: Class<ITabView>? = null

    private var mScroller: Scroller = Scroller(context)

    /** @param itemArray 设置项目标题数组 */
    fun setItems(itemArray: Array<String>) {
        mItemArray = itemArray
    }

    /** @param clazz 设置自定义的 ITabView 作为每个项目的 View */
    fun setTabView(clazz: Class<ITabView>) {
        mClazz = clazz
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflateItems()
    }

    /** 添加 Tab */
    private fun inflateItems() {
        if (mClazz != null) {

        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

    /**
     * 应用于 ZoomTabLayout 的 Tab 接口
     * 所有实现该接口的 View 均可设置给 ZoomTabLayout 作为样式使用
     */
    interface ITabView {
        /**
         * Tab 切换时的滑动比例，范围：0f - 1f
         * 利用该数值可以设置跟手动画
         * @param scale 滑动比例
         */
        fun setScrollScale(scale: Float)

        /**
         * 设置 Tab 标题文字
         * @param text 标题文字
         */
        fun setText(text: String)
    }

    /** 用于 ZoomTabLayout 的 TabView */
    class TabView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
        : View(context, attrs, def), ITabView {
        private var mText: String? = null
        private var mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var mDefHeight: Int = 20
        private var mDefWidth: Int = 40
        private var mScrollScale: Float = 0f
        private var mTextSize: Float = 0f
        private var mTextAlign: Paint.Align = Paint.Align.LEFT

        init {
            setTextColor(Color.BLACK)
            setTextSize(20f)
            setTextAlign(Paint.Align.LEFT)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            // 针对 wrap_content 的处理，使 wrap_content 生效
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            if (mText != null) {
                mDefHeight = mTextSize.toInt() + paddingTop + paddingBottom
                mDefWidth = (mTextSize * mText!!.length).toInt() + paddingStart + paddingEnd
            }
            if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(mDefWidth, mDefHeight)
            } else if (widthSpecMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(mDefWidth, heightSpecSize)
            } else if (heightSpecMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSpecSize, mDefHeight)
            }
        }

        override fun setScrollScale(scale: Float) {
            mScrollScale = scale
            postInvalidate()
        }

        override fun setText(text: String) {
            mText = text
            postInvalidate()
        }

        /** @param color 设置文字颜色 */
        fun setTextColor(@ColorInt color: Int): TabView {
            mTextPaint.color = color
            return this@TabView
        }

        /** @param size 设置文字大小 */
        fun setTextSize(size: Float): TabView {
            mTextSize = size
            mTextPaint.textSize = mTextSize
            return this@TabView
        }

        /** @param align 设置文字对齐方式 */
        fun setTextAlign(align: Paint.Align): TabView {
            mTextAlign = align
            mTextPaint.textAlign = mTextAlign
            return this@TabView
        }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            // 针对 padding 的处理，使 padding 生效
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom
            canvas?.drawText(mText
                    ?: "null", x + paddingStart.toFloat(), y + paddingTop.toFloat(), mTextPaint)
        }
    }
}