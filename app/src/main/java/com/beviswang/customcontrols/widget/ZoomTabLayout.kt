package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.widget.LinearLayout
import com.beviswang.customcontrols.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.ref.WeakReference

/**
 * 自定义缩放 TabLayout （仿虾米音乐 TabBar）
 * @author BevisWang
 * @date 2018/11/14 16:14
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
    private var mDefSelected = 0
    private var mSelectIndex = 0
    private var mLastSelectIndex = 0
    // 是否在切换，若在切换过程中禁止再切换
    private var mIsChanging: Boolean = false
    // 绑定的 ViewPager
    private var mViewPager: ViewPager? = null
    // 缩放进度
    private var mScale: Float = 0f

    init {
        initParams(context, attrs)
    }

    private fun initParams(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ZoomTabLayout)
        mTextColor = typeArray.getColor(R.styleable.ZoomTabLayout_zoomTabTextColor, Color.BLACK)
        mLineColor = typeArray.getColor(R.styleable.ZoomTabLayout_zoomTabLineColor, Color.RED)
        mItemPadding = typeArray.getInt(R.styleable.ZoomTabLayout_zoomTabPadding, dip2px(context, 8f).toInt())
        mNormalTextSize = typeArray.getFloat(R.styleable.ZoomTabLayout_zoomTabUnSelectTextSize, sp2px(context, 14f))
        mSelectTextSize = typeArray.getFloat(R.styleable.ZoomTabLayout_zoomTabSelectTextSize, sp2px(context, 28f))
        mDefSelected = typeArray.getInt(R.styleable.ZoomTabLayout_zoomTabDefaultSelected, 1)
        mSelectIndex = mDefSelected
        typeArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        inflateItems()
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
            child.setOnClickListener { v ->
                selectItem(indexOfChild(v))
            }
            addView(child)
        }
        getChildAt(0).setPadding(paddingStart, 0, mItemPadding / 2, 0)
        getChildAt(childCount - 1).setPadding(mItemPadding / 2, 0, paddingEnd, 0)
        setPadding(0, paddingTop, 0, paddingBottom)
        // 设置首个 Tab 默认为选中状态
        (getChildAt(mDefSelected) as TabView).setScrollScale(1f)
    }

    private var mLastScale = 0f

    /** @param offsetWidth 当 Tab 发生偏移时的监听方法 */
    private fun onWidthChangedListener(offsetWidth: Int) {
        if (mLastScale == mScale) return
        val tab = getChildAt(mLastSelectIndex) as TabView
        tab.setOffsetWidth(-offsetWidth, mScale)
        mLastScale = mScale
    }

    /**
     * 当发生滑动时的调用方法
     * @param position 当前位置
     * @param positionOffset 移动的进度 0-1 or 1-0
     */
    private fun onPageScrolled(position: Int, positionOffset: Float) {
        if (position != mSelectIndex) {
            // 往上一个页面跳转
            if (mSelectIndex == 0) return // 最前
            mLastSelectIndex = mSelectIndex
            val selectedView = getChildAt(position) as TabView
            mScale = 1f - positionOffset
            selectedView.setScrollScale(mScale)
        } else {
            // 往下一个页面跳转
            if (mSelectIndex == childCount - 1) return // 最后
            mLastSelectIndex = mSelectIndex
            val selectedView = getChildAt(mSelectIndex + 1) as TabView
            mScale = positionOffset
            selectedView.setScrollScale(mScale)
        }
    }

    /**
     * 开始选中动画
     * @param startOffset 起始比例
     * @param endOffset 结束比例
     */
    private fun startSelectAnimation(startOffset: Float, endOffset: Float) {
        val start = (startOffset * 28).toInt()
        val end = (endOffset * 28).toInt()
        doAsync {
            mIsChanging = true
            val selectedView = getChildAt(mSelectIndex) as TabView
            (start..end).forEach { it ->
                uiThread { _ ->
                    mScale = it / 28f
                    selectedView.setScrollScale(mScale)
                }
                Thread.sleep(5)
            }
            uiThread {
                selectedView.startTabAnimation()
            }
            mIsChanging = false
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

    /** @param index 设置默认选中项 （注意：只有在生成视图之前调用才能生效） */
    fun setDefSelectedItem(index: Int) {
        mDefSelected = index
    }

    /** @param index 选中的 item 的 index （注意：只有生成了视图才能调用） */
    fun selectItem(index: Int) {
        mViewPager?.currentItem = index
    }

    /**
     * 选中 Tab
     * @param index 需要选中的 tab index
     * @param scale 其实缩放值
     */
    private fun pageSelectItem(index: Int, scale: Float) {
        if (mSelectIndex == index || mIsChanging) return
        mLastSelectIndex = mSelectIndex
        mSelectIndex = index
        startSelectAnimation(scale, 1f)
    }

    /** @param padding 设置 Item 间的间距 */
    fun setItemPadding(padding: Int) {
        mItemPadding = padding
    }

    /** @param viewPager 与 ViewPager 配合使用，安装 ViewPager */
    fun setupWithViewPager(viewPager: ViewPager) {
        viewPager.currentItem = mDefSelected
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(this@ZoomTabLayout))
        mViewPager = viewPager
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
        fun setOffsetWidth(offsetWidth: Int, scale: Float)

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
        private var offset = 0f
        private var mLastPos = tabLayout.mDefSelected

        private var isSelect = false

        override fun onPageScrollStateChanged(state: Int) {
            if (state == 0) isSelect = false
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            offset = positionOffset
            val tabLayout = mTabLayoutRef.get() ?: return
            if (!isSelect)
                tabLayout.onPageScrolled(position, positionOffset)
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = mTabLayoutRef.get() ?: return
            isSelect = true
            if (mLastPos < position || offset == 0f)
                tabLayout.pageSelectItem(position, offset)
            else
                tabLayout.pageSelectItem(position, 1f - offset)
            mLastPos = position
            offset = 0f
        }
    }
}