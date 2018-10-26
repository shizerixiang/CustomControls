package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Scroller

/**
 * 自定义缩放 TabView
 * 内部需要实现一个 id 为 text1 的 TextView 以及一个 id 为 icon 的 ImageView
 * @author BevisWang
 */
class ZoomTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0) :
        LinearLayout(context, attrs, def) {
    private var mItemArray: Array<String>? = null

    private var mScroller: Scroller = Scroller(context)

    // Tab 的属性样式
    private var mNormalTextSize = sp2px(context,14f)
    private var mSelectTextSize = sp2px(context,28f)
    @ColorInt
    private var mTextColor = Color.BLACK
    private var mLineColor = Color.RED

    private var mTabMargin: Int = 0

    /** @param itemArray 设置项目标题数组 */
    fun setItems(itemArray: Array<String>) {
        mItemArray = itemArray
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        inflateItems()
    }

    /** 添加 Tab */
    private fun inflateItems() {
        orientation = LinearLayout.HORIZONTAL
        mItemArray?.forEach {
            val child = TabView(context)
            child.setText(it)
            child.setTextSize(mNormalTextSize)
            child.setSelectTextSize(mSelectTextSize)
            child.setTextColor(mTextColor)
            child.setLineColor(mLineColor)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(mTabMargin, 0, mTabMargin, 0)
            child.layoutParams = lp
            addView(child)
        }
    }

    /** dp 转 px */
    private fun dip2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    /** sp 转 px */
    private fun sp2px(context: Context, sp: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return sp * scale + 0.5f
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

        /** 开启波纹抖动动画 */
        fun startTabAnimation()

        /** @param text 设置 Tab 标题文字 */
        fun setText(text: String)

        /** @param size 设置 Tab 文字大小 */
        fun setTextSize(size: Float): ITabView

        /** @param size 设置 Tab 文字选中后的大小 */
        fun setSelectTextSize(size: Float): ITabView

        /** @param color 设置 Tab 文字颜色 */
        fun setTextColor(@ColorInt color: Int): ITabView

        /** @param color 设置 Tab 底部波纹颜色 */
        fun setLineColor(@ColorInt color: Int): ITabView
    }
}