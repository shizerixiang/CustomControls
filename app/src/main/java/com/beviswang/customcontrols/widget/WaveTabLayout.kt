package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.graphics.path.ViewPath
import com.beviswang.customcontrols.loge
import org.jetbrains.anko.dip
import java.lang.Math.abs

/**
 * 水波纹样式的 Tab
 * @author BevisWong
 * @date 2022/3/16
 */
class WaveTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    def: Int = 0
) : View(context, attrs, def) {
    private val mDefWidth: Int = 100
    private val mDefHeight: Int = 40

    // 动画
    private var mDrawingAnimator: ValueAnimator? = null

    private var mBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBgRectF: RectF = RectF()
    private var mBgPath: Path = Path()

    private var mTabPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTabCenterPoints: Array<PointF> = arrayOf()
    private var mTabPaddingTop: Float = 0f
    private var mTabWidth: Float = 0f
    private var mTabRadius: Float = 0f
    private var mTabPadding: Float = 0f

    private var mArcY: Float = 0f
    private var mArcPoint: PointF = PointF(0f, 0f)
    private val mArcBezierPoints: Array<PointF> = Array(7) { PointF(0f, 0f) }

    private var mBgColor: Int = Color.GRAY
    private var mSelectedColor: Int = Color.RED
    private var mUnSelectColor: Int = Color.GRAY
    private var mDefSelected: Int = 0

    private var mCurArcX: Float = 0f // 动画进度 startX - endX
    private var mProgress: Float = 0f // 动画进度 0f - 1f
    private var mTabViewPath: ViewPath = ViewPath(Path())

    private var mStayTabCPoint: PointF = PointF()
    private var mDoTabCPoint: PointF = PointF()

    private var mTabImageRes: Array<Int> = arrayOf()
    private var mOldSelectIndex: Int = 0
    private var mSelectIndex: Int = 0

    private var mTabMipmapRes: Array<Int> = arrayOf()
    private var mTabDrawableRes: Array<Int> = arrayOf()
    private var mTabDrawableArray: Array<Drawable?> = arrayOf()

    private var mTabImageType: Int = -1 // -1 noting 0 mipmap 1 drawable

    init {
        initParams(context, attrs)
        mBgPaint.color = mBgColor
        mBgPaint.style = Paint.Style.FILL_AND_STROKE

        mTabPaint.color = mUnSelectColor
        mTabPaint.style = Paint.Style.FILL_AND_STROKE
    }

    fun setTabImageIcons(icons: Array<Int>) {
        mTabImageType = 0
        mTabMipmapRes = icons
    }

    fun setTabDrawableIcons(icons: Array<Int>) {
        mTabImageType = 1
        mTabDrawableRes = icons
    }

    fun selectTab(index: Int) {
        if (index == mSelectIndex) return
        mSelectIndex = index
        updateTabs()
        mCurArcX = mTabCenterPoints[mSelectIndex].x
        doSelectAnimation(mTabCenterPoints[mOldSelectIndex].x, mCurArcX)
    }

    private fun initParams(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.WaveTabLayout)
        mBgColor = typeArray.getColor(R.styleable.WaveTabLayout_bgColor, mBgColor)
        mSelectedColor = typeArray.getColor(R.styleable.WaveTabLayout_selectedColor, mSelectedColor)
        mUnSelectColor = typeArray.getColor(R.styleable.WaveTabLayout_unselectColor, mUnSelectColor)
        mTabPaddingTop = typeArray.getDimensionPixelOffset(
            R.styleable.WaveTabLayout_tabPaddingTop, dip(8)
        ).toFloat()
        mDefSelected = typeArray.getInt(R.styleable.WaveTabLayout_defaultSelectedIndex, 0)
        mSelectIndex = mDefSelected
        typeArray.recycle()
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
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                if (event.y < mTabPaddingTop) {
                    return super.onTouchEvent(event)
                }
                mOldSelectIndex = mSelectIndex
                when {
                    event.x < mTabWidth -> selectTab(0)
                    event.x < 2 * mTabWidth -> selectTab(1)
                    event.x < 3 * mTabWidth -> selectTab(2)
                    event.x < 4 * mTabWidth -> selectTab(3)
                    else -> selectTab(mSelectIndex)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val realHeight = h.toFloat() - paddingTop - paddingBottom
        val realWidth = w.toFloat() - paddingLeft - paddingRight
        mTabPaddingTop = realHeight / 4f
        mBgRectF.set(
            paddingLeft.toFloat(),
            mTabPaddingTop,
            w - paddingRight.toFloat(),
            realHeight
        )
        mTabRadius = (realHeight - mTabPaddingTop) / 2f
        configTabImages()
        mTabWidth = realWidth / mTabImageRes.size
        mTabCenterPoints = Array(mTabImageRes.size) { PointF(0f, 0f) }
        mTabPadding = mTabRadius / 4f

        measureTabViewPath()

        mArcY = mBgRectF.bottom - mTabPaddingTop / 2
        updateTabs()
        mCurArcX = mTabCenterPoints[mSelectIndex].x
        updateArc()

    }

    private fun configTabImages() {
        val iconSize = (mTabRadius).toInt()
        var drawable: Drawable? = null
        when (mTabImageType) {
            0 -> mTabImageRes = mTabMipmapRes
            1 -> mTabImageRes = mTabDrawableRes
        }
        mTabDrawableArray = Array(mTabImageRes.size) { null }
        mTabImageRes.forEachIndexed { index, icRes ->
            if (mTabImageType == 0) {
                drawable = BitmapDrawable(
                    resources, Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(resources, icRes), iconSize, iconSize, true
                    )
                )
            }
            if (mTabImageType == 1) {
                drawable = ContextCompat.getDrawable(context, icRes) ?: return@forEachIndexed
            }
            drawable?.setBounds(iconSize/2, iconSize/2, iconSize/2+iconSize, iconSize/2+iconSize)
            mTabDrawableArray[index] = drawable
        }
    }

    /** 计算 Tab 的路径 */
    private fun measureTabViewPath() {
        mTabViewPath.path.reset()
        mTabViewPath.path.moveTo(mTabWidth / 2f, mTabPaddingTop + mTabRadius)
        mTabViewPath.path.quadTo(
            mTabWidth / 2f + (mTabRadius / 2f),
            mTabPaddingTop / 2f + (mTabRadius / 2f),
            mTabWidth / 2f,
            mTabRadius
        )
        mTabViewPath.measurePath()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        drawBgPath(canvas)
        drawTabs(canvas)
    }

    private fun drawBgPath(canvas: Canvas) {
        mBgPath.reset()
        mBgPath.moveTo(mBgRectF.left, mBgRectF.top)
        mBgPath.lineTo(mArcBezierPoints[0].x, mArcBezierPoints[0].y)
        mBgPath.cubicTo(
            mArcBezierPoints[1].x, mArcBezierPoints[1].y,
            mArcBezierPoints[2].x, mArcBezierPoints[2].y,
            mArcBezierPoints[3].x, mArcBezierPoints[3].y
        )
        mBgPath.cubicTo(
            mArcBezierPoints[4].x, mArcBezierPoints[4].y,
            mArcBezierPoints[5].x, mArcBezierPoints[5].y,
            mArcBezierPoints[6].x, mArcBezierPoints[6].y
        )
        mBgPath.lineTo(mBgRectF.right, mTabPaddingTop)
        mBgPath.lineTo(mBgRectF.right, mBgRectF.bottom)
        mBgPath.lineTo(mBgRectF.left, mBgRectF.bottom)
        mBgPath.close()
        canvas.drawPath(mBgPath, mBgPaint)
    }

    private fun drawTabs(canvas: Canvas) {
        mTabCenterPoints.forEachIndexed { index, point ->
            if (index == mSelectIndex) {
                mTabPaint.color = mBgColor
                canvas.drawCircle(point.x, point.y, mTabRadius, mTabPaint)
                mTabDrawableArray[index]?.setTint(mSelectedColor)
            } else {
                mTabPaint.color = mBgColor
                canvas.drawCircle(point.x, point.y, mTabRadius, mTabPaint)
                mTabDrawableArray[index]?.setTint(mUnSelectColor)
            }
            canvas.translate(point.x - mTabRadius, point.y - mTabRadius)
            mTabDrawableArray[index]?.draw(canvas)
            canvas.translate(mTabRadius - point.x, mTabRadius - point.y)
        }
    }

    private fun updateTabs() {
        mStayTabCPoint.set(mTabWidth / 2, mTabRadius + mTabPaddingTop)
        mTabImageRes.forEachIndexed { index, icon ->
            when (index) {
                mSelectIndex -> {
                    mDoTabCPoint = mTabViewPath.getPathPoint(1 - mProgress) // 动画进行中的中心点
                    mTabCenterPoints[index].set(
                        index * mTabWidth + mDoTabCPoint.x, mDoTabCPoint.y
                    )
                }
                mOldSelectIndex -> {
                    mDoTabCPoint = mTabViewPath.getPathPoint(mProgress) // 动画进行中的中心点
                    if (mSelectIndex != mOldSelectIndex) {
                        mTabCenterPoints[index].set(
                            index * mTabWidth + mDoTabCPoint.x, mDoTabCPoint.y
                        )
                    }
                }
                else -> mTabCenterPoints[index].set(
                    index * mTabWidth + mStayTabCPoint.x, mStayTabCPoint.y
                )
            }
        }
    }

    private fun updateArc() {
        val xUnit = mTabWidth / 4f + mTabPadding
        mArcPoint.set(mCurArcX, mArcY)
        mArcBezierPoints[3].set(mArcPoint)
        mArcBezierPoints[2].set(mArcBezierPoints[3].x - xUnit, mArcBezierPoints[3].y)
        mArcBezierPoints[1].set(mArcBezierPoints[2].x - mTabPadding, mBgRectF.top)
        mArcBezierPoints[0].set(mArcBezierPoints[2].x - xUnit, mArcBezierPoints[1].y)

        mArcBezierPoints[4].set(mArcBezierPoints[3].x + xUnit, mArcBezierPoints[3].y)
        mArcBezierPoints[5].set(mArcBezierPoints[4].x + mTabPadding, mBgRectF.top)
        mArcBezierPoints[6].set(mArcBezierPoints[5].x + xUnit, mArcBezierPoints[5].y)
    }

    private fun doSelectAnimation(startX: Float, endX: Float) {
        val dx = endX - startX
        mDrawingAnimator?.cancel()
        mDrawingAnimator = ValueAnimator.ofFloat(startX, endX)
        mDrawingAnimator?.duration = 320
        mDrawingAnimator?.interpolator = OvershootInterpolator()
//        mDrawingAnimator?.interpolator = AccelerateDecelerateInterpolator()
        mDrawingAnimator?.repeatMode = ValueAnimator.RESTART
        mDrawingAnimator?.addUpdateListener {
            mCurArcX = it.animatedValue as Float
            mProgress = abs((endX - mCurArcX) / dx)
            updateTabs()
            updateArc()
            invalidate()
        }
        mDrawingAnimator?.start()
    }
}