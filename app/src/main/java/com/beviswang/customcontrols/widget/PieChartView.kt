package com.beviswang.customcontrols.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 饼状图
 * @author BevisWang
 * @date 2018/11/16 11:46
 */
class PieChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    // 扇形画笔
    private var mArcPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 中心圆数据，用于产生扇形图间的间隙
    private var mCenterR: Float = 1f
    private var mCenterX: Float = 0f
    private var mCenterY: Float = 0f

    init {
        mArcPaint.style = Paint.Style.FILL_AND_STROKE
        mArcPaint.color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val height = height - paddingTop - paddingBottom
        val width = width - paddingStart - paddingEnd
        mCenterX = width / 2f + paddingStart
        mCenterY = height / 2f + paddingTop
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
        val height = height - paddingTop - paddingBottom
        val width = width - paddingStart - paddingEnd
        var dx: Float = paddingStart.toFloat()
        var dy: Float = paddingTop.toFloat()
        var diameter = 0f
        if (height > width) {
            diameter = width.toFloat()
            dy += (height - width) / 2f
        } else {
            diameter = height.toFloat()
            dx += (width - height) / 2f
        }
        val arcRectF = RectF(0f, 0f, diameter, diameter)
        arcRectF.offset(dx, dy)
        val d = 60f
        mArcPaint.color = Color.WHITE
        canvas?.drawArc(arcRectF, 0f, d, true, mArcPaint)
        mArcPaint.color = Color.RED
        canvas?.drawArc(arcRectF, 60f, d, true, mArcPaint)
        mArcPaint.color = Color.YELLOW
        canvas?.drawArc(arcRectF, 120f, d, true, mArcPaint)
        mArcPaint.color = Color.GREEN
        canvas?.drawArc(arcRectF, 180f, d, true, mArcPaint)
        mArcPaint.color = Color.BLACK
        canvas?.drawArc(arcRectF, 240f, d, true, mArcPaint)
        arcRectF.offset(6f,-4f)
        mArcPaint.color = Color.GRAY
        canvas?.drawArc(arcRectF, 300f, d, true, mArcPaint)
    }
}