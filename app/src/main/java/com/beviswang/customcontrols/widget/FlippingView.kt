package com.beviswang.customcontrols.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * 带有翻转动画的圆环
 * @author BevisWang
 */
class FlippingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    // 用于 wrap_content 的默认宽高 px
    private val mDefWidth = 300
    private val mDefHeight = 300
    // 主圆环属性
    private var mArcPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mArcWidth: Float = 8f

    init {
        initPaint()
    }

    private fun initPaint() {
        mArcPaint.color = Color.RED
        mArcPaint.style = Paint.Style.STROKE
        mArcPaint.strokeWidth = mArcWidth
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var dx = mArcWidth / 2f
        var dy = mArcWidth / 2f
        val arcRadius: Float
        if (width < height) {
            arcRadius = width - mArcWidth
            dy += (height - width) / 2f
        } else {
            arcRadius = height - mArcWidth
            dx += (width - height) / 2f
        }
        val rectF = RectF(0f, 0f, arcRadius, arcRadius)
        rectF.offset(dx, dy)

        val matrix = Matrix()
        matrix.postRotate(10f,50f,50f)
//        canvas?.save()
        canvas?.concat(matrix)
        canvas?.drawArc(rectF, 0f, 360f, false, mArcPaint)
    }
}