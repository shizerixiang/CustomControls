package com.beviswang.customcontrols.widget.spectrum

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

/**
 * 坐标系四面反射波纹动画
 * @author BevisWang
 * @date 2019/9/19 10:57
 */
class CoordinateReflexView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : VisualizerView(context, attrs, def) {
    private val mBarPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBarWidth = 0f
    private var disX: Float = 0f
    private var disY: Float = 2f

    init {
        mBarPaint.style = Paint.Style.FILL_AND_STROKE
        mBarPaint.strokeWidth = 12f
        mBarPaint.strokeCap = Paint.Cap.ROUND
        mBarPaint.color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBarWidth = w / count.toFloat() / 2
        setMaxValue(h / 2)
        mBarPaint.strokeWidth = mBarWidth / 1.4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        drawBar(canvas)
    }

    private fun drawBar(canvas: Canvas) {
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        disY = mBarPaint.strokeWidth / 2f
        (0 until count).forEach {
            disX = it * mBarWidth + (mBarPaint.strokeWidth * 0.75f)
            mCurCountValue[it] = (mNewCountValue[it] - mOldCountValue[it]) * getProgress() + mOldCountValue[it]
            mBarPaint.alpha = 255
            canvas.drawLine(disX, -disY, disX, -mCurCountValue[it] - disY - 1, mBarPaint)
            canvas.drawLine(-disX, -disY, -disX, -mCurCountValue[it] - disY - 1, mBarPaint)
            mBarPaint.alpha = 180
            canvas.drawLine(disX, disY, disX, mCurCountValue[it] + disY + 1, mBarPaint)
            canvas.drawLine(-disX, disY, -disX, mCurCountValue[it] + disY + 1, mBarPaint)
        }
        canvas.restore()
    }
}