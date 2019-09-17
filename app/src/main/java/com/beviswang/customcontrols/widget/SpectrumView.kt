package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 * 音频频谱
 * @author BevisWang
 * @date 2019/9/17 15:48
 */
class SpectrumView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def), Visualizer.OnDataCaptureListener {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    private val mLinePaint:Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mLinePaint.style = Paint.Style.FILL_AND_STROKE
        mLinePaint.strokeWidth = 4f
        mLinePaint.color = Color.WHITE
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

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        if (fft == null) return
        Log.e("onFftDataCapture","samplingRate=$samplingRate")
        notifySpectrum(fft)
    }

    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
        if (waveform == null) return
        Log.e("onWaveFormDataCapture","samplingRate=$samplingRate")
        notifySpectrum(waveform)
    }

    fun notifySpectrum(fft:ByteArray) {
        Log.e("notifySpectrum","fftDataSize=${fft.size}")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}