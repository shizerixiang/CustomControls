package com.beviswang.customcontrols.widget

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import androidx.palette.graphics.Palette
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.loge
import com.beviswang.customcontrols.util.BitmapHelper

/**
 * 烟花绘制
 * @author BevisWang
 * @date 2019/9/26 11:39
 */
class FireworksSceneView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300

    private var mFireworksPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mBitmapPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mBitmap: Bitmap? = null

    private var mFireworks: Fireworks

    init {
        mFireworksPaint.color = Color.WHITE

        mLinePaint.color = Color.WHITE
        mLinePaint.strokeWidth = 10f
        mLinePaint.style = Paint.Style.FILL_AND_STROKE
        mLinePaint.maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)

        mBitmapPaint.color = Color.WHITE
        mBitmapPaint.maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.OUTER)

        mBitmap = BitmapHelper.scaleBitmap(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_rocket), 600, 600)

        setLayerType(LAYER_TYPE_SOFTWARE, null)

        Palette.from(mBitmap!!).generate {
            mBitmapPaint.color = it?.lightVibrantSwatch?.rgb ?: Color.WHITE
            invalidate()
        }

        mFireworks = Fireworks()
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
        if (canvas == null) return
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        if (mBitmap != null) canvas.drawBitmap(mBitmap!!.extractAlpha(), -300f, -300f, mBitmapPaint)
        if (mBitmap != null) canvas.drawBitmap(mBitmap!!, -300f, -300f, null)
//        canvas.drawLine(-300f, -300f, 300f, 300f, mLinePaint)
        canvas.restore()
    }

    /** 烟花 */
    class Fireworks() {
        private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG) // 烟花绘制画笔
        private var isExplode: Boolean = false // 烟花是否炸裂

        init {
            mPaint.color = Color.WHITE
        }

        fun onDraw(canvas: Canvas) {
            if (isExplode) drawAfterExplode(canvas)
            else drawBeforeExplode(canvas)
        }

        /** 绘制爆炸之后 */
        private fun drawAfterExplode(canvas: Canvas) {

        }

        /** 绘制爆炸之前 */
        private fun drawBeforeExplode(canvas: Canvas) {

        }
    }
}