package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * 水波纹
 * @author BevisWang
 */
class WaveView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private var mPath: Path = Path()
    private var waveLength = 800f
    private var waveCount = 4
    private var offset = 0f
    private var centerY = 0f
    private var screenWidth = 800f
    private var screenHeight = 480f
    private var mPaintBezier: Paint = Paint()

    private var valueAnimator: ValueAnimator? = null

    init {
        mPaintBezier.style = Paint.Style.FILL_AND_STROKE
        mPaintBezier.strokeWidth = 4f
        mPaintBezier.color = Color.GRAY
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenHeight = h.toFloat()
        screenWidth = w.toFloat()
        if (centerY == 0f)
            centerY = h / 2f
        setAnimator()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mPath.reset()
        mPath.moveTo(-waveLength + offset, centerY)

        // 绘制贝塞尔曲线
        for (i in 0 until waveCount) {
            mPath.quadTo(-waveLength * 3 / 4 + i * waveLength + offset, centerY + 30,
                    -waveLength / 2 + i * waveLength + offset, centerY)
            mPath.quadTo(-waveLength / 4 + i * waveLength + offset, centerY - 30,
                    i * waveLength + offset, centerY)
        }

        // 封闭波浪
        mPath.lineTo(screenWidth, screenHeight)
        mPath.lineTo(0f, screenHeight)
        mPath.close()

        canvas?.drawPath(mPath, mPaintBezier)
    }

    /**
     * 设置进度
     * @param f 进度比例
     */
    fun setProgress(f: Float) {
        centerY = screenHeight * (1f - f)
        Log.e("aa", "centerY=$centerY")
    }

    /** 开始水波纹波动动画 */
    fun startWave() {
        if (valueAnimator?.isRunning == true) {
            stopAnimator()
            return
        }
        valueAnimator?.start()
    }

    private fun setAnimator() {
        if (valueAnimator != null) return
        valueAnimator = ValueAnimator.ofFloat(0f, waveLength)
        valueAnimator?.duration = 1000
        valueAnimator?.repeatCount = ValueAnimator.INFINITE
        valueAnimator?.interpolator = LinearInterpolator()
        valueAnimator?.addUpdateListener { animation ->
            offset = animation.animatedValue as Float
            invalidate()
        }
    }

    private fun stopAnimator() {
        if (valueAnimator?.isRunning == true)
            valueAnimator?.cancel()
        invalidate()
    }
}