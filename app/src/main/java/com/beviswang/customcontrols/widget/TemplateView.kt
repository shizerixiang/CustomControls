package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.beviswang.customcontrols.R

/**
 * 自定义绘制 View 模板
 * @author BevisWang
 */
class TemplateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    @ColorInt
    private var mColor = Color.RED
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 用于 wrap_content 的默认宽高 px
    private val mDefWidth = 300
    private val mDefHeight = 300

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TemplateView)
        mColor = typedArray.getColor(R.styleable.TemplateView_circle_color, Color.RED)
        typedArray.recycle()

        setCircleColor(mColor)
    }

    /**
     * 设置圆的颜色
     * @param color 颜色值
     */
    fun setCircleColor(@ColorInt color: Int) {
        mPaint.color = color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 针对 wrap_content 的处理，使 wrap_content 生效
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefWidth, mDefHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 针对 padding 的处理，使 padding 生效
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        val radius = Math.min(width, height) / 2f
        canvas?.drawCircle(paddingLeft + width / 2f, paddingTop + height / 2f, radius, mPaint)
    }
}