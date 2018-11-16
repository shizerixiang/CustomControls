package com.beviswang.customcontrols.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.beviswang.customcontrols.R
/**
 * 头部天气布局控件 （仿美团头部天气）
 * @author BevisWang
 * @date 2018/11/15 9:24
 */
class WeatherLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : RelativeLayout(context, attrs, def) {
    private lateinit var mHeaderView: View
    private var mScale: Float = 0f

    private lateinit var mImg: View // 天气图标
    private lateinit var mTem: TextView // 气温
    private lateinit var mSearchBar: View // 搜索条

    private lateinit var mImgOriPos: LayoutParams
    private lateinit var mTemOriPos: LayoutParams
    private lateinit var mSearchBarOriPos: LayoutParams

    private var mDstImgHeight = 0
    private var mDstImgWidth = 0
    private var mDstTemTopMargin = 0
    private var mDstTemLeftMargin = 0
    private var mDstTemTxtSize = 0f
    private var mDstSBTopMargin = 0
    private var mDstSBLeftMargin = 0

    private var mListener: OnHeaderListener? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.layout_header, null)
        addView(mHeaderView)

        mImg = mHeaderView.findViewById(R.id.mWeatherImg)
        mTem = mHeaderView.findViewById(R.id.mWeatherTem)
        mSearchBar = mHeaderView.findViewById(R.id.mSearchBar)

        mImgOriPos = mImg.layoutParams as LayoutParams
        mTemOriPos = mTem.layoutParams as LayoutParams
        mSearchBarOriPos = mSearchBar.layoutParams as LayoutParams

        mDstImgHeight = dip2px(context, 32f).toInt() - mImgOriPos.height
        mDstImgWidth = dip2px(context, 32f).toInt() - mImgOriPos.width
        mDstTemTopMargin = -mTemOriPos.topMargin
        mDstTemLeftMargin = (mImgOriPos.width + (dip2px(context, 16f)).toInt()) - mTemOriPos.leftMargin
        mDstTemTxtSize = 14f
        mDstSBTopMargin = (mTemOriPos.topMargin + (dip2px(context, 12f)).toInt()) - mSearchBarOriPos.topMargin
        mDstSBLeftMargin = 0 - mSearchBarOriPos.leftMargin

        val search = mSearchBar.findViewById<View>(R.id.mSearch)
        search.setOnClickListener { mListener?.onSearchBarClick(search) }
        val location = mSearchBar.findViewById<View>(R.id.mLocation)
        location.setOnClickListener { mListener?.onLocationClick(location) }
    }

    /**
     * 设置动画比例
     * @param scale 设置比例   0-1
     */
    fun setScale(scale: Float) {
        mScale = scale
        scaleLayout()
    }

    /* 关闭 */
    fun close() {
        closeLayout()
    }

    /* 打开 */
    fun open() {
        openLayout()
    }

    /**
     * 设置内部子控件点击监听器
     * @param listener 监听器
     */
    fun setOnHeaderClickListener(listener: OnHeaderListener) {
        mListener = listener
    }

    private fun scaleLayout() {
        val imgLp = LayoutParams(mImgOriPos)
        imgLp.height = (mDstImgHeight * mScale + mImgOriPos.height).toInt()
        imgLp.width = (mDstImgWidth * mScale + mImgOriPos.width).toInt()
        mImg.layoutParams = imgLp

        val temLp = LayoutParams(mTemOriPos)
        temLp.topMargin = (mDstTemTopMargin * mScale + mTemOriPos.topMargin).toInt()
        temLp.leftMargin = (mDstTemLeftMargin * mScale + mTemOriPos.leftMargin).toInt()
        temLp.width = LayoutParams.WRAP_CONTENT
        mTem.layoutParams = temLp
        mTem.textSize = mDstTemTxtSize * mScale + 12f

        val searchBarLp = LayoutParams(mSearchBarOriPos)
        searchBarLp.topMargin = (mDstSBTopMargin * mScale + mSearchBarOriPos.topMargin).toInt()
        searchBarLp.leftMargin = (mDstSBLeftMargin * mScale + mSearchBarOriPos.leftMargin).toInt()
        mSearchBar.layoutParams = searchBarLp

        postInvalidate()
    }

    private fun closeLayout() {
        mImg.layoutParams = mImgOriPos
        mTem.layoutParams = mTemOriPos
        mTem.textSize = 12f
        mSearchBar.layoutParams = mSearchBarOriPos
        postInvalidate()
    }

    private fun openLayout() {
        val imgLp = LayoutParams(mImgOriPos)
        imgLp.height = dip2px(context, 32f).toInt()
        imgLp.width = dip2px(context, 32f).toInt()
        mImg.layoutParams = imgLp

        val temLp = LayoutParams(mTemOriPos)
        temLp.topMargin = (dip2px(context, 4f)).toInt()
        temLp.leftMargin = imgLp.width + (dip2px(context, 8f)).toInt()
        temLp.width = LayoutParams.WRAP_CONTENT
        mTem.layoutParams = temLp
        mTem.textSize = 24f

        val searchBarLp = LayoutParams(mSearchBarOriPos)
        searchBarLp.topMargin = mTemOriPos.topMargin + (dip2px(context, 12f)).toInt()
        searchBarLp.leftMargin = 0
        mSearchBar.layoutParams = searchBarLp

        postInvalidate()
    }

    /** dp 转 px */
    private fun dip2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    /**
     * 点击监听器
     */
    interface OnHeaderListener {
        /**
         * 位置点击
         * @param location 位置选择器
         */
        fun onLocationClick(location: View)

        /**
         * 搜索条点击事件
         * @param searchBar 搜索条
         */
        fun onSearchBarClick(searchBar: View)
    }
}