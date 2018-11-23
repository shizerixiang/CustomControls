package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.graphics.Point3DF
import com.beviswang.customcontrols.util.BitmapHelper

/**
 * 自定义翻版控件
 * @author BevisWang
 * @date 2018/11/21 10:56
 */
class FlipboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    // 尺寸
    private var mCenterX: Float = 0f
    private var mCenterY: Float = 0f
    // 绘制属性
    private var mPicBitmap: Bitmap?                             // 图片 Bitmap
    private var mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)     // 图片绘制画笔
    private var mCamera: Camera = Camera()                       // Camera
    // 动画属性
    private var mValueAnimator: ValueAnimator? = null           // 动画类
    private var mCurValue: Float = 0f                           // 当前动画进度 0f-1f

    init {
        mPicBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_flipboard_logo)
    }

    /** 开启动画 */
    fun startAnimator() {
        if (mValueAnimator == null) newAnimator()
        if (mValueAnimator?.isRunning == true) return
        mValueAnimator?.start()
    }

    /** 暂停动画 */
    fun pauseAnimator() {
        if (mValueAnimator?.isRunning == true) mValueAnimator?.pause()
    }

    /** 设置需要翻转的图片 Bitmap */
    fun setDrawBitmap(bitmap: Bitmap) {
        mPicBitmap = bitmap
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 调整 bitmap 大小
        mPicBitmap = BitmapHelper.scaleBitmap(mPicBitmap, mPicBitmap!!.width * 2, mPicBitmap!!.height * 2)

        mCenterX = width / 2f
        mCenterY = height / 2f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        val rectFTop = RectF(0f, 0f, width.toFloat(), height / 2f)
//        drawRotateBitmap(canvas, rectFTop, Point3DF(x = 30f * mCurValue))
//
//        val rectFBottom = RectF(0f, height / 2f, width.toFloat(), height.toFloat())
//        drawRotateBitmap(canvas, rectFBottom, Point3DF(x = 30f * mCurValue))


    }

    /**
     * 绘制需要转动部分的 bitmap
     * @param canvas
     * @param rectF 绘制需要旋转的部分
     * @param p 旋转各轴的角度
     */
    private fun drawRotateBitmap(canvas: Canvas?, rectF: RectF, p: Point3DF) {
        val picWidth = mPicBitmap!!.width
        val picHeight = mPicBitmap!!.height
        canvas?.save()
        canvas?.clipRect(rectF)
        mCamera.save()
        mCamera.rotateX(p.x)
        mCamera.rotateY(p.y)
        mCamera.rotateZ(p.z)
        canvas?.translate(mCenterX, mCenterY)
        mCamera.applyToCanvas(canvas)
        canvas?.translate(-mCenterX, -mCenterY)
        mCamera.restore()
        canvas?.drawBitmap(mPicBitmap, (width - picWidth) / 2f, (height - picHeight) / 2f, mBitmapPaint)
        canvas?.restore()
    }

    /** 创建动画 */
    private fun newAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        mValueAnimator?.duration = 1200
        mValueAnimator?.repeatCount = -1
        mValueAnimator?.addUpdateListener {
            mCurValue = it.animatedValue as Float * 2f
            if (mCurValue > 1f) mCurValue = 2f - mCurValue
            postInvalidate()
        }
    }

    /** 移除动画 */
    private fun removeAnimator() {
        if (mValueAnimator?.isRunning == true) mValueAnimator?.cancel()
        mValueAnimator = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAnimator()
        mPicBitmap?.recycle()
        mPicBitmap = null
    }

    /** dp 转 px */
    private fun dip2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }
}