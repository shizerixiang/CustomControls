package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.util.Log
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
        mDefHeight = (mCurTextSize + mTextPaint.fontMetrics.descent).toInt() + paddingTop + paddingBottom
        mDefWidth = mTextPaint.measureText(mText).toInt() + paddingStart + paddingEnd
        // 加入线条区域高度
        mDefHeight += mLineBoxHeight
    }

    /** @param scale 设置缩放尺寸 */
    override fun setScrollScale(scale: Float) {
        mScrollScale = scale
        val dSize = if (mSelectedTextSize != 0f)
            mSelectedTextSize - mNormalTextSize
        else mNormalTextSize
        mCurTextSize += dSize * mScrollScale
        Log.e("aa","size: $mCurTextSize px")
        setTextSize(mCurTextSize)
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
        mCurTextSize = size
        mNormalTextSize = size
        mTextPaint.textSize = mCurTextSize
        resize()
        return this@TabView
    }

    /** @param size 设置文字选中后的大小 */
    fun setSelectTextSize(size: Float): TabView {
        mSelectedTextSize = size
        return this@TabView
    }

    /** 开启波纹抖动动画 */
    fun startRippleAnimation() {
        if (isRunningAnimator) return
        doAsync {
            isRunningAnimator = true
            (0..8).forEach {
                postInvalidate()
                Thread.sleep(100)
            }
            isRunningAnimator = false
        }
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
            Paint.Align.LEFT -> paddingStart + x
            Paint.Align.CENTER -> width / 2f + x
            Paint.Align.RIGHT -> width - paddingEnd + x
            else -> x
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
        (1..20).forEach {
            val dy = when {
                it < 5 -> dip2px(context, random.nextInt((px2sp(context, mCurTextSize) / 8).toInt()) + 1f).toInt()
                it < 9 -> dip2px(context, random.nextInt((px2sp(context, mCurTextSize) / 4).toInt()) + 1.5f).toInt()
                it < 15 -> dip2px(context, random.nextInt((px2sp(context, mCurTextSize) / 2.4f).toInt()) + 4f).toInt()
                it < 17 -> dip2px(context, random.nextInt((px2sp(context, mCurTextSize) / 4).toInt()) + 1.5f).toInt()
                else -> dip2px(context, random.nextInt((px2sp(context, mCurTextSize) / 8).toInt()) + 1f).toInt()
            }
            index = it * 2
            if (it % 2 == 0)
                mLinePath.quadTo(lineXPart * (index - 1), lineY + dy, lineXPart * index, lineY)
            else
                mLinePath.quadTo(lineXPart * (index - 1), lineY - dy, lineXPart * index, lineY)
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