package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import org.jetbrains.anko.doAsync
import java.util.*

/**
 * 用于 ZoomTabLayout 的 TabView
 * @author BevisWang
 */
class TabView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), ZoomTabLayout.ITabView {
    private var mText: String = "null"
    private var mTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mDefHeight: Int = 20
    private var mDefWidth: Int = 40
    private var mCurTextSize: Float = 0f
    private var mNormalTextSize: Float = 0f
    private var mSelectedTextSize: Float = 0f
    private var mTextAlign: Paint.Align = Paint.Align.LEFT
    // 线条区域高度
    private var mLineBoxHeight: Int = dip2px(context, 10f).toInt()
    // 是否在播放波纹动画
    private var isRunningAnimator: Boolean = false
    // 控制整个控件的缩放比例
    private var mScrollScale: Float = 0f
    // 记录上次的随机值，方便放大时保证波纹的一致性
    private var mRandomArray: Array<Int> = Array(20, init = { 0 })
    // 是否重绘波纹幅度，是则波纹幅度随机分配，否则波纹幅度保留上次的值
    private var mIsRepaint: Boolean = false

    init {
        setTextPaint(context)
        setLinePaint(context)
    }

    /** 设置文字画笔样式 */
    private fun setTextPaint(context: Context) {
        setTextColor(Color.BLACK)
        setTextSize(sp2px(context, 14f))
        setTextAlign(Paint.Align.LEFT)
    }

    /** 设置线条画笔样式 */
    private fun setLinePaint(context: Context) {
        mLinePaint.color = Color.RED
        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.strokeWidth = dip2px(context, 0.5f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 针对 wrap_content 的处理，使 wrap_content 生效
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        computeDefSize()
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefWidth, mDefHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefWidth, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, mDefHeight)
        }
    }

    /** 计算适应内容的默认大小 */
    private fun computeDefSize() {
        mDefHeight = (mCurTextSize + mTextPaint.fontMetrics.descent).toInt() +
                paddingTop + paddingBottom
        mDefWidth = mTextPaint.measureText(mText).toInt() + paddingStart + paddingEnd
        // 加入线条区域高度
        mDefHeight += mLineBoxHeight
    }

    /** @param scale 设置缩放尺寸 */
    override fun setScrollScale(scale: Float) {
        if (isRunningAnimator || mIsRepaint) {
            mIsRepaint = false
            isRunningAnimator = false
        }
        mScrollScale = scale
        // 默认缩放为两倍普通字体大小
        if (mSelectedTextSize == 0f) mSelectedTextSize = 2 * mNormalTextSize
        mCurTextSize = if (mScrollScale != 1f) (mSelectedTextSize - mNormalTextSize) *
                mScrollScale + mNormalTextSize
        else mSelectedTextSize
        // 计算后的 View 重绘
        setCurTextSize(mCurTextSize)
    }

    override fun setText(text: String) {
        mText = text
        postInvalidate()
    }

    /** @param size 设置文字大小 */
    override fun setTextSize(size: Float): TabView {
        mCurTextSize = size
        mNormalTextSize = size
        mTextPaint.textSize = mCurTextSize
        resize()
        return this@TabView
    }

    /** @param size 设置文字选中后的大小 */
    override fun setSelectTextSize(size: Float): TabView {
        mSelectedTextSize = size
        return this@TabView
    }

    /** @param color 设置文字颜色 */
    override fun setTextColor(@ColorInt color: Int): TabView {
        mTextPaint.color = color
        return this@TabView
    }

    /** @param color 设置波纹线条颜色 */
    override fun setLineColor(@ColorInt color: Int): TabView {
        mLinePaint.color = color
        return this@TabView
    }

    /** 开启波纹抖动动画 */
    override fun startTabAnimation() {
        if (isRunningAnimator) return
        doAsync {
            isRunningAnimator = true
            mIsRepaint = true
            (0..8).forEach {
                if (!isRunningAnimator) {
                    mIsRepaint = false
                }
                postInvalidate()
                Thread.sleep(100)
            }
            mIsRepaint = false
            isRunningAnimator = false
        }
    }

    /** 设置当前文字大小，调整整个容器 */
    private fun setCurTextSize(size: Float) {
        mCurTextSize = size
        mTextPaint.textSize = mCurTextSize
        resize()
    }

    /** 重新设置大小 */
    private fun resize() {
        if (height == 0 || width == 0) return
        computeDefSize()
        if (height == mDefHeight && width == mDefWidth) return
        val lp = layoutParams
        lp.width = mDefWidth
        lp.height = mDefHeight
        layoutParams = lp
    }

    /** @param align 设置文字对齐方式 */
    fun setTextAlign(align: Paint.Align): TabView {
        mTextAlign = align
        mTextPaint.textAlign = mTextAlign
        postInvalidate()
        return this@TabView
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawText(canvas)
        drawLine(canvas)
    }

    /** 绘制文字 */
    private fun drawText(canvas: Canvas?) {
        val textStartY = mCurTextSize
        val textStartX = when (mTextAlign) {
            Paint.Align.LEFT -> paddingStart.toFloat()
            Paint.Align.CENTER -> width / 2f
            Paint.Align.RIGHT -> width - paddingEnd.toFloat()
            else -> 0f
        }
        canvas?.drawText(mText, textStartX, textStartY, mTextPaint)
    }

    /** 绘制底部图形 */
    private fun drawLine(canvas: Canvas?) {
        val lineY = height - (mLineBoxHeight - dip2px(context, 1f))
        val lineXPart = width / 40f
        val mLinePath = Path()
        mLinePath.moveTo(0f, lineY)
        val random = Random()
        var index = 0
        if (mSelectedTextSize == 0f) mSelectedTextSize = 2 * mNormalTextSize
        (1..20).forEach {
            // 计算随机波纹幅度
            if (mRandomArray[it - 1] == 0 || mIsRepaint) {
                mRandomArray[it - 1] = when {
                    it < 3 -> dip2px(context, random.nextInt((px2sp(context,
                            mSelectedTextSize) / 8).toInt()) + 1f).toInt()
                    it < 7 -> dip2px(context, random.nextInt((px2sp(context,
                            mSelectedTextSize) / 6).toInt()) + 2f).toInt()
                    it < 15 -> dip2px(context, random.nextInt((px2sp(context,
                            mSelectedTextSize) / 3).toInt()) + 3f).toInt()
                    it < 17 -> dip2px(context, random.nextInt((px2sp(context,
                            mSelectedTextSize) / 6).toInt()) + 2f).toInt()
                    else -> dip2px(context, random.nextInt((px2sp(context,
                            mSelectedTextSize) / 8).toInt()) + 1f).toInt()
                }
            } else {

            }
            index = it * 2
            if (it % 2 == 0)
                mLinePath.quadTo(lineXPart * (index - 1), lineY + (mRandomArray[it - 1] *
                        (mScrollScale + 0.5f)), lineXPart * index, lineY)
            else
                mLinePath.quadTo(lineXPart * (index - 1), lineY - (mRandomArray[it - 1] *
                        (mScrollScale + 0.5f)), lineXPart * index, lineY)
        }
        mLinePath.moveTo(width.toFloat(), lineY)
        canvas?.drawPath(mLinePath, mLinePaint)
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

    /** px 转 sp */
    private fun px2sp(context: Context, px: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return px / scale + 0.5f
    }
}