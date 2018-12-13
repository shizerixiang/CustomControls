package com.beviswang.customcontrols.widget

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.graphics.Point3DF
import com.beviswang.customcontrols.graphics.PointHelper
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
    private var mAnimatorSet: AnimatorSet? = null
    private var mValueAnimator1: ValueAnimator? = null           // 动画类1
    private var mValueAnimator2: ValueAnimator? = null           // 动画类2
    private var mValueAnimator3: ValueAnimator? = null           // 动画类3
    private var mCurValue: Float = 0f                           // 当前动画进度 0f-1f
    // 上次的动画进度，记录 mCurValue
    private var mLastValue = 0f
    // 镜头最大翻转角度
    private var maxAngel = 0f

    /**
     * 旋转矩形的四个点坐标
     *  B --------- C
     *    |       |
     *    |       |
     *  A --------- D
     */
    private var pA = PointF(0f, 0f)
    private var pB = PointF(0f, 0f)
    private var pC = PointF(0f, 0f)
    private var pD = PointF(0f, 0f)
    // 四个点的旋转路径圆的半径
    private var radiusA: Float = 0f
    private var radiusB: Float = 0f
    private var radiusC: Float = 0f
    private var radiusD: Float = 0f
    // 四个点的旋转角度（初始角度不同）
    private var angelA = 90f
    private var angelB = 270f
    private var angelC = 315f
    private var angelD = 45f

    init {
        mPicBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.flipboard_logo_400px)
        // 摄像头单位 英寸，在 Android 中换算为 1英寸72像素，同时需要适配屏幕，故出此算法
        mCamera.setLocation(0f, 0f, dip2px(context, -576f) / 72)
    }

    /** 开启动画 */
    fun startAnimator() {
        if (mAnimatorSet == null) newAnimator()
        if (mAnimatorSet?.isRunning == true) return
        mCurValue = 0f
        maxAngel = 0f
        mAnimatorSet?.start()
    }

    /** 暂停动画 */
    fun pauseAnimator() {
        if (mAnimatorSet?.isRunning == true) mAnimatorSet?.pause()
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
        mPicBitmap = BitmapHelper.scaleBitmap(mPicBitmap, w / 3 * 2, w / 3 * 2)

        mCenterX = width / 2f
        mCenterY = height / 2f

        // 四个点的旋转路径圆的半径
        val picWidth = mPicBitmap!!.width
        val picHeight = mPicBitmap!!.height
        radiusA = Math.sqrt(Math.pow(picHeight.toDouble(), 2.0) + Math.pow(picWidth.toDouble(), 2.0)).toFloat() / 2f
        radiusC = Math.sqrt(Math.pow(radiusA.toDouble(), 2.0) * 2).toFloat()
        radiusB = radiusA
        radiusD = radiusC
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        calculateAngel()
        calculatePoints()
        val curPath = Path()
        curPath.moveTo(pA.x, pA.y)
        curPath.lineTo(pB.x, pB.y)
        curPath.lineTo(pC.x, pC.y)
        curPath.lineTo(pD.x, pD.y)
        curPath.close()

        val point3DF = getPoint3DF()
        drawRotateBitmap(canvas, curPath, point3DF)
        drawClipBitmap(canvas, curPath)
    }

    /** 获取三轴旋转角度 */
    private fun getPoint3DF(): Point3DF {
        val point3DF = Point3DF()
        when {
            mCurValue < 90 -> {
                point3DF.x = mCurValue % 90 / 90f * -maxAngel
                point3DF.y = mCurValue % 90 / 90f * maxAngel - maxAngel
            }
            mCurValue < 180 -> {
                point3DF.x = mCurValue % 90 / 90f * maxAngel - maxAngel
                point3DF.y = mCurValue % 90 / 90f * maxAngel
            }
            mCurValue < 270 -> {
                point3DF.x = mCurValue % 90 / 90f * maxAngel
                point3DF.y = maxAngel - mCurValue % 90 / 90f * maxAngel
            }
            mCurValue < 360 -> {
                point3DF.x = maxAngel - mCurValue % 90 / 90f * maxAngel
                point3DF.y = mCurValue % 90 / 90f * -maxAngel
            }
        }
        return point3DF
    }

    /**
     * 绘制路径外的图案
     * @param canvas 画板
     * @param path 路径
     */
    private fun drawClipBitmap(canvas: Canvas?, path: Path) {
        val picWidth = mPicBitmap!!.width
        val picHeight = mPicBitmap!!.height
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint.color = Color.BLUE

        val destBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val destCanvas = Canvas(destBitmap)
        destCanvas.drawPath(path, paint)
        destCanvas.save()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val srcCanvas = Canvas(bitmap)

        val saved = srcCanvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        srcCanvas.drawBitmap(mPicBitmap, (width - picWidth) / 2f, (height - picHeight) / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        srcCanvas.drawBitmap(destBitmap, 0f, 0f, paint)
        paint.xfermode = null
        srcCanvas.restoreToCount(saved)

        canvas?.drawBitmap(bitmap, 0f, 0f, mBitmapPaint)
    }

    /** 计算当前四个点的角度 */
    private fun calculateAngel() {
        val changedValue = mCurValue - mLastValue
        angelA -= changedValue
        if (angelA < 0) angelA += 360
        if (angelA > 360) angelA -= 360
        angelB -= changedValue
        if (angelB < 0) angelB += 360
        if (angelB > 360) angelB -= 360
        angelC -= changedValue
        if (angelC < 0) angelC += 360
        if (angelC > 360) angelC -= 360
        angelD -= changedValue
        if (angelD < 0) angelD += 360
        if (angelD > 360) angelD -= 360
        mLastValue = mCurValue
    }

    /**
     * 计算旋转矩形的四个顶点
     */
    private fun calculatePoints() {
        pA = PointHelper.getPointOnCircle(PointF(mCenterX, mCenterY), radiusA, angelA)
        pB = PointHelper.getPointOnCircle(PointF(mCenterX, mCenterY), radiusB, angelB)
        pC = PointHelper.getPointOnCircle(PointF(mCenterX, mCenterY), radiusC, angelC)
        pD = PointHelper.getPointOnCircle(PointF(mCenterX, mCenterY), radiusD, angelD)
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

    /**
     * 绘制需要转动部分的 bitmap
     * @param canvas
     * @param path 绘制需要旋转的部分
     * @param p 旋转各轴的角度
     */
    private fun drawRotateBitmap(canvas: Canvas?, path: Path, p: Point3DF) {
        val picWidth = mPicBitmap!!.width
        val picHeight = mPicBitmap!!.height
        canvas?.save()
        canvas?.clipPath(path)
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
        mValueAnimator1 = ValueAnimator.ofFloat(0f, 1f)
        mValueAnimator1?.interpolator = AccelerateDecelerateInterpolator()
        mValueAnimator1?.duration = 1200
        mValueAnimator1?.addUpdateListener {
            mCurValue = it.animatedValue as Float * 270
            postInvalidate()
        }

        mValueAnimator2 = ValueAnimator.ofFloat(0f, 1f, 1f)
        mValueAnimator2?.duration = 1800
        mValueAnimator2?.addUpdateListener {
            maxAngel = it.animatedValue as Float * MAX_ANGEL
            postInvalidate()
        }

        mValueAnimator3 = ValueAnimator.ofFloat(1f, 1f, 0f)
        mValueAnimator3?.duration = 1800
        mValueAnimator3?.addUpdateListener {
            maxAngel = it.animatedValue as Float * MAX_ANGEL
            postInvalidate()
        }

        mAnimatorSet = AnimatorSet()
        mAnimatorSet?.playSequentially(mValueAnimator2, mValueAnimator1, mValueAnimator3)
    }

    /** 移除动画 */
    private fun removeAnimator() {
        if (mAnimatorSet?.isRunning == true) mAnimatorSet?.cancel()
        mAnimatorSet = null
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

    companion object {
        private const val MAX_ANGEL = 30
    }
}