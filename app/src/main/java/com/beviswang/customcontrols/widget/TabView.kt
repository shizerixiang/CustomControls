package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View

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
    private var mScrollScale: Float = 0f
    private var mTextSize: Float = 0f
    private var mTextAlign: Paint.Align = Paint.Align.LEFT
    // 线条区域高度
    private var mLineBoxHeight: Int = 16

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
        mLinePaint.strokeWidth = dip2px(context, 1f)
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
        mDefHeight = (mTextSize + mTextPaint.fontMetrics.descent).toInt() + paddingTop + paddingBottom
        mDefWidth = mTextPaint.measureText(mText).toInt() + paddingStart + paddingEnd
        // 加入线条区域高度
        mDefHeight += mLineBoxHeight
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
        resize()
        return this@TabView
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
        val textStartY = mTextSize
        val textStartX = when (mTextAlign) {
            Paint.Align.LEFT -> paddingStart + x
            Paint.Align.CENTER -> width / 2f + x
            Paint.Align.RIGHT -> width - paddingEnd + x
            else -> x
        }
        canvas?.drawText(mText, textStartX, textStartY, mTextPaint)
    }

    /** 绘制底部图形 */
    private fun drawLine(canvas: Canvas?) {
        canvas?.drawLine(0f, height - 2f, width.toFloat(), height - 2f, mLinePaint)
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
}