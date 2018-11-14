package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

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
    private var mArcWidth: Float = dip2px(context, 12f)
    private var mArcBitmap: Bitmap? = null
    // 渐变色
    private lateinit var mGradientColors: IntArray
    private lateinit var mGradientPos: FloatArray
    private lateinit var mShader: Shader
    private var mCurAngle: Float = 0f // 当前的旋转角度
    // 主圆环外部类波纹效果画笔
    private var mWavePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 内部进度圆环属性
    private var mProgressArcPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mProgressArcWidth: Float = dip2px(context, 2f)
    private var mProgressBitmap: Bitmap? = null
    private var mCurProgress: Float = 0f
    // 文字
    private var mTxtPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTxtSize: Float = sp2px(context, 32f)
    // 摄像头
    private val mCamera: Camera = Camera()
    private val mMatrix: Matrix = Matrix()
    // 触控
    private var mLastY: Int = 0
    private var mScale: Float = 0f
    @ColorInt
    private var mArcColor: Int = Color.WHITE
    private var mArcAlpha: Int = MAX_ARC_ALPHA

    private var mDegrees = 0f

    init {
        initPaint()
    }

    private fun initPaint() {
        mArcPaint.color = mArcColor
        mArcPaint.style = Paint.Style.STROKE
        mArcPaint.strokeWidth = mArcWidth
        mArcPaint.alpha = mArcAlpha

        mGradientPos = floatArrayOf(0.2f, 0.4f, 0.6f, 0.8f)
        val colorW = Color.argb(160, 255, 255, 255)
        mGradientColors = intArrayOf(colorW, Color.WHITE, Color.WHITE, colorW)
        mShader = SweepGradient(mDefWidth / 2f, mDefHeight / 2f, mGradientColors, mGradientPos)
        mArcPaint.shader = mShader

        mWavePaint.color = colorW
        mWavePaint.style = Paint.Style.STROKE
        mWavePaint.strokeWidth = mArcWidth / 2f

        mProgressArcPaint.color = mArcColor
        mProgressArcPaint.style = Paint.Style.STROKE
        mProgressArcPaint.strokeWidth = mProgressArcWidth
        mProgressArcPaint.alpha = mArcAlpha

        mTxtPaint.color = Color.WHITE
        mTxtPaint.textSize = mTxtSize
        mTxtPaint.textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mProgressBitmap?.recycle()
        mProgressBitmap = null
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = y - mLastY
                if (deltaY == 0) return true
                mScale += deltaY / height.toFloat()
                if (mScale < 0) mScale = 0f
                if (mScale > 1) mScale = 1f
                rotateScale(mScale)
            }
            MotionEvent.ACTION_UP -> {
                mCurProgress += 10
                if (mCurProgress > 360) mCurProgress -= 360
                setProgress(mCurProgress)
            }
        }
        mLastY = y
        return true
    }

    /**
     * 翻转效果
     * @param degrees 翻转角度 0 <= degrees <= 180
     */
    fun rotateDegrees(degrees: Float) {
        if (degrees < 0 || degrees > 180)
            throw IndexOutOfBoundsException("角度值越界！范围 0-180，当前 $mDegrees ！")
        mDegrees = 90 - Math.abs(90 - degrees)
        postInvalidate()
    }

    /**
     * 翻转效果
     * @param scale 翻转比例，1 为完全显示，0 为完全不显示
     */
    fun rotateScale(scale: Float) {
        if (scale < 0 || scale > 1)
            throw IndexOutOfBoundsException("角度值比例越界！范围 0-1，当前 $scale ！")
        mDegrees = scale * 90
        mArcAlpha = ((1 - scale) * MAX_ARC_ALPHA).toInt()
        mArcPaint.alpha = mArcAlpha
        mProgressArcPaint.alpha = mArcAlpha
        postInvalidate()
    }

    /** @param color 设置圆环颜色 */
    fun setArcColor(@ColorInt color: Int) {
        mArcColor = color
        mArcPaint.color = mArcColor
        mProgressArcPaint.color = mArcColor
    }

    /**
     * 设置内环进度
     * @param progressValue 进度值，范围 0-360
     */
    fun setProgress(progressValue: Float) {
        if (progressValue < 0 || progressValue > 360)
            throw IndexOutOfBoundsException("进度值越界！范围 0-360，当前 $progressValue ！")
        mCurProgress = progressValue
    }

    /** 开启渐变动画 */
    fun startAnimation() {
        val animation = ValueAnimator.ofInt(0, 100)
        animation.interpolator = LinearInterpolator()
        animation.duration = 3000
        animation.repeatCount = -1
        animation.addUpdateListener {
            val value = it.animatedValue as Int
            mCurAngle = value / 100f * 360
            postInvalidate()
        }
        animation.start()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val height = height - paddingTop - paddingBottom
        val width = width - paddingStart - paddingEnd
        var dx = mArcWidth / 2f
        var dy = mArcWidth / 2f
        val diameter: Float
        if (width < height) {
            diameter = width - mArcWidth
            dy += (height - width) / 2f
        } else {
            diameter = height - mArcWidth
            dx += (width - height) / 2f
        }

//        val matrix = Matrix()
        // px,py 为旋转中心点坐标；degrees 为旋转度数 (模拟摄像头，但存在无法控制 z 轴的问题)
//        matrix.postRotate(45f, width / 2f, height / 2f)
//        matrix.postSkew(0.5f, 0.5f, width / 2f, height / 2f)
//        matrix.postRotate(45f, width / 2f, height / 2f)
//        matrix.postScale(0.7f, 0.7f, width / 2f, height / 2f)
//        canvas?.concat(matrix)
        setupCamera()
        canvas?.concat(mMatrix)

        val progressRectF = RectF(paddingStart.toFloat(), paddingTop.toFloat(),
                diameter + paddingStart - (mArcWidth * 3), diameter + paddingTop - (mArcWidth * 3))
        progressRectF.offset(dx + (mArcWidth * 1.5f), dy + (mArcWidth * 1.5f))
        // 内部进度环，采用一次绘制后，生成 bitmap，重复绘制时，直接复用，优化绘制速度
        if (mProgressBitmap == null) saveToBitmap(diameter, dx, dy)
        val mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBitmapPaint.alpha = mArcAlpha
        canvas?.drawBitmap(mProgressBitmap, 0f, 0f, mBitmapPaint)

        mProgressArcPaint.alpha = 255
        canvas?.drawArc(progressRectF, 270f, mCurProgress, false, mProgressArcPaint)

        canvas?.drawText("中心", width / 2f + paddingStart, height / 2f + paddingTop, mTxtPaint)

        // 以下部分为旋转部分
        canvas?.save()
        canvas?.rotate(mCurAngle, width / 2f + paddingStart, height / 2f + paddingTop)

        val rectF = RectF(paddingStart.toFloat(), paddingTop.toFloat(),
                diameter + paddingStart, diameter + paddingTop)
        rectF.offset(dx, dy)

        // 绘制需要转动的部分
        if (mArcBitmap == null) mArcBitmap = getWaveBitmap(rectF)
        canvas?.drawBitmap(mArcBitmap, 0f, 0f, mBitmapPaint)
    }

    private fun getWaveBitmap(rectF: RectF): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = mArcWidth - 0.1f
        canvas.drawArc(rectF, 0f, 360f, false, paint)

        val bgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bgCanvas = Canvas(bgBitmap)
        val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mWavePaint.style = Paint.Style.STROKE
        mWavePaint.strokeWidth = dip2px(context, 12f)
        mWavePaint.color = Color.WHITE
        mWavePaint.alpha = 40
        rectF.offset(-dip2px(context, 4f), 0f)
        bgCanvas.drawArc(rectF, 90f, 180f, false, mWavePaint)
        rectF.offset(-dip2px(context, 4f), 0f)
        bgCanvas.drawArc(rectF, 90f, 180f, false, mWavePaint)
        rectF.offset(-dip2px(context, 4f), 0f)
        bgCanvas.drawArc(rectF, 90f, 180f, false, mWavePaint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        canvas.drawBitmap(bgBitmap, 0f, 0f, paint)
        paint.xfermode = null
        rectF.offset(dip2px(context, 12f), 0f)
        // 转动炫光效果渲染器
        setShader()
        // 起始角度 + 绘制角度 大于 360 时，系统会从头开始绘制
        canvas.drawArc(rectF, 0f, 360f, false, mArcPaint)
        return bitmap
    }

    /** 保存为 bitmap，防止重新计算进度 */
    private fun saveToBitmap(diameter: Float, dx: Float, dy: Float) {
        mProgressBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mProgressBitmap)
        val progressRectF = RectF(paddingStart.toFloat(), paddingTop.toFloat(),
                diameter + paddingStart - (mArcWidth * 3), diameter + paddingTop - (mArcWidth * 3))
        progressRectF.offset(dx + (mArcWidth * 1.5f), dy + (mArcWidth * 1.5f))
        (1..360).forEach {
            if (it % 2 == 0) {
                mProgressArcPaint.color = mArcColor
                mProgressArcPaint.alpha = 160
            } else mProgressArcPaint.color = Color.TRANSPARENT
            canvas.drawArc(progressRectF, it - 1f, 0.8f,
                    false, mProgressArcPaint)
        }
    }

    /** 设置渲染器 */
    private fun setShader() {
        mShader = SweepGradient(width / 2f, height / 2f, mGradientColors, mGradientPos)
        mArcPaint.shader = mShader
    }

    /** 安装翻转摄像头 */
    private fun setupCamera() {
        mCamera.save()
        // 翻转角度
        mCamera.rotateX(mDegrees)
        mCamera.getMatrix(mMatrix)
        mCamera.restore()
        mMatrix.preTranslate(-(width / 2f + paddingStart), -(height / 2f + paddingTop))
        mMatrix.postTranslate(width / 2f + paddingStart, height / 2f + paddingTop)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mProgressBitmap?.recycle()
        mArcBitmap?.recycle()
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

    companion object {
        private const val MAX_ARC_ALPHA = 255
    }
}