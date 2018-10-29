package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.SCROLL_STATE_IDLE
import android.util.AttributeSet
import android.widget.LinearLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference

/**
 * 自定义缩放 TabLayout
 * @author BevisWang
 */
class ZoomTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0) :
        LinearLayout(context, attrs, def) {
    private var mItemArray: Array<String>? = null
    // Tab 的属性样式
    private var mNormalTextSize = sp2px(context, 14f)
    private var mSelectTextSize = sp2px(context, 28f)
    @ColorInt
    private var mTextColor = Color.BLACK
    @ColorInt
    private var mLineColor = Color.RED
    // Tab 之间的间距
    private var mItemPadding: Int = 0
    // 默认选中的 Tab
    private var mSelectIndex = 0
    private var mLastSelectIndex = 0
    // 是否在切换，若在切换过程中禁止再切换
    private var mIsChanging: Boolean = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        inflateItems()
        selectItem(1)
    }

    /** 添加 Tab */
    private fun inflateItems() {
        if (childCount != 0) return
        orientation = LinearLayout.HORIZONTAL
        mItemArray?.forEach {
            val child = TabView(context)
            child.setText(it)
            child.setTextSize(mNormalTextSize)
            child.setSelectTextSize(mSelectTextSize)
            child.setTextColor(mTextColor)
            child.setLineColor(mLineColor)
            child.setPadding(mItemPadding / 2, 0, mItemPadding / 2, 0)
            child.addWidthChangedListener { offsetWidth -> onWidthChangedListener(offsetWidth) }
            child.setOnClickListener { v -> selectItem(indexOfChild(v)) }
            addView(child)
        }
        getChildAt(0).setPadding(paddingStart, 0, mItemPadding / 2, 0)
        getChildAt(childCount - 1).setPadding(mItemPadding / 2, 0, paddingEnd, 0)
        setPadding(0, paddingTop, 0, paddingBottom)
    }

    /**
     * 选择 Tab
     * @param selectedIndex 选中的 item index
     * @param unSelectedIndex 取消选中的 item index
     */
    private fun selectItemByIndex(selectedIndex: Int, unSelectedIndex: Int) {
        doAsync {
            mIsChanging = true
            val selectedView = getChildAt(selectedIndex) as TabView
            val unSelectedView = getChildAt(unSelectedIndex) as TabView
            (0..24).forEach { it ->
                uiThread { _ ->
                    selectedView.setScrollScale(it / 24f)
                    unSelectedView.setScrollScale((24 - it) / 24f)
                }
                Thread.sleep(12)
            }
            uiThread {
                selectedView.startTabAnimation()
            }
            mIsChanging = false
        }
    }

    /** @param offsetWidth 当 Tab 发生偏移时的监听方法 */
    private fun onWidthChangedListener(offsetWidth: Int) {
        val tab = getChildAt(mLastSelectIndex) as TabView
        tab.setOffsetWidth(-offsetWidth)
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

    /** @param size Tab 文字大小 */
    fun setTabTextSize(size: Float, selectSize: Float = size * 2) {
        mNormalTextSize = size
        mSelectTextSize = selectSize
    }

    /** 设置文字颜色 */
    fun setTextColor(@ColorInt textColor: Int) {
        mTextColor = textColor
    }

    /** 设置底部线条颜色 */
    fun setLineColor(@ColorInt lineColor: Int) {
        mLineColor = lineColor
    }

    /** @param itemArray 设置项目标题数组 */
    fun setItems(itemArray: Array<String>) {
        mItemArray = itemArray
    }

    /** @param index 选中的 item 的 index */
    fun selectItem(index: Int) {
        if (mSelectIndex == index || mIsChanging) return
        mLastSelectIndex = mSelectIndex
        mSelectIndex = index
        selectItemByIndex(mSelectIndex, mLastSelectIndex)
    }

    /** @param padding 设置 Item 间的间距 */
    fun setItemPadding(padding: Int) {
        mItemPadding = padding
    }

    /** @param viewPager 与 ViewPager 配合使用，安装 ViewPager */
    fun setupWithViewPager(viewPager: ViewPager) {
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(this@ZoomTabLayout))
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
         * 当 Tab 的尺寸会发生改变时，需要重写实现该方法
         * 如果没有改变宽度，则直接返回 0
         * @param listener 监听器回调宽度变化值
         */
        fun addWidthChangedListener(listener: (offsetWidth: Int) -> Unit)

        /** @param offsetWidth 宽度变化值 */
        fun setOffsetWidth(offsetWidth: Int)

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

    /** 主要监听 ViewPager 滑动 */
    class TabLayoutOnPageChangeListener(tabLayout: ZoomTabLayout) : ViewPager.OnPageChangeListener {
        private val mTabLayoutRef: WeakReference<ZoomTabLayout> = WeakReference(tabLayout)
        private var mPreviousScrollState: Int = 0
        private var mScrollState: Int = 0

        override fun onPageScrollStateChanged(state: Int) {
            mPreviousScrollState = mScrollState
            mScrollState = state
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val tabLayout = mTabLayoutRef.get() ?: return
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = mTabLayoutRef.get() ?: return
        }

        internal fun reset() {
            mScrollState = SCROLL_STATE_IDLE
            mPreviousScrollState = mScrollState
        }
    }
}