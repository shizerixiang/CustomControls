package com.beviswang.customcontrols.widget.adnormal

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.graphics.path.ViewPath
import org.jetbrains.anko.dip
import java.lang.Math.abs

/**
 * 反常识的 Tab
 * @author BevisWong
 * @date 2022/3/16
 */
class AbnormalTabLayout @JvmOverloads constructor(
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

    private var mCurArcX: Float = 0f // 动画进度 startX - endX
    private var mProgress: Float = 0f // 动画进度 0f - 1f
    private var mTabViewPath: ViewPath = ViewPath(Path())

    private var mTabArray: Array<AbnormalTab> = arrayOf() // Tab 集合
    private var mDefSelected: Int = 0 // 默认选中位置
    private var mOldSelectIndex: Int = 0 // 上次选中的位置
    private var mSelectIndex: Int = 0 // 当前选中的位置

    private var mStayTabCPoint: PointF = PointF() // Tab 常驻位置
    private var mDoTabCPoint: PointF = PointF() // 做动画时 Tab 的位置

    init {
        initParams(context, attrs)
        mBgPaint.color = mBgColor
        mBgPaint.style = Paint.Style.FILL_AND_STROKE

        mTabPaint.color = mUnSelectColor
        mTabPaint.style = Paint.Style.FILL_AND_STROKE
    }

    fun newTab(@DrawableRes drawableId: Int):AbnormalTab {
        return AbnormalTab().apply {
            parent = this@AbnormalTabLayout
            setDrawableId(drawableId)
        }
    }

    fun setTabs(tabs: Array<AbnormalTab>) {
        mTabArray = tabs
    }

    fun selectTab(index: Int) {
        if (index == mSelectIndex) return
        mSelectIndex = index
        updateTabs()
        mCurArcX = mTabArray[mSelectIndex].centerPos.x
        doSelectAnimation(mTabArray[mOldSelectIndex].centerPos.x, mCurArcX)
    }

    private fun initParams(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.AbnormalTabLayout)
        mBgColor = typeArray.getColor(R.styleable.AbnormalTabLayout_atl_bgColor, mBgColor)
        mSelectedColor =
            typeArray.getColor(R.styleable.AbnormalTabLayout_atl_selectedColor, mSelectedColor)
        mUnSelectColor =
            typeArray.getColor(R.styleable.AbnormalTabLayout_atl_unselectColor, mUnSelectColor)
        mTabPaddingTop = typeArray.getDimensionPixelOffset(
            R.styleable.AbnormalTabLayout_atl_tabPaddingTop, dip(8)
        ).toFloat()
        mDefSelected = typeArray.getInt(R.styleable.AbnormalTabLayout_atl_defaultSelectedIndex, 0)
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
        mTabWidth = realWidth / mTabArray.size
        mTabPadding = mTabRadius / 4f

        measureTabViewPath()

        mArcY = mBgRectF.bottom - mTabPaddingTop / 2
        updateTabs()
        mCurArcX = mTabArray[mSelectIndex].centerPos.x
        updateArc()
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
        mTabArray.forEachIndexed { index, tab ->
            if (index == mSelectIndex) {
                mTabPaint.color = mBgColor
                canvas.drawCircle(tab.centerPos.x, tab.centerPos.y, mTabRadius, mTabPaint)
                tab.drawable?.setTint(mSelectedColor)
            } else {
                mTabPaint.color = mBgColor
                canvas.drawCircle(tab.centerPos.x, tab.centerPos.y, mTabRadius, mTabPaint)
                tab.drawable?.setTint(mUnSelectColor)
            }
            tab.draw(canvas)
        }
    }

    private fun updateTabs() {
        mStayTabCPoint.set(mTabWidth / 2, mTabRadius + mTabPaddingTop)
        mTabArray.forEachIndexed { index, tab ->
            tab.onSizeChanged(mTabRadius.toInt())
            when (index) {
                mSelectIndex -> {
                    mDoTabCPoint = mTabViewPath.getPathPoint(1 - mProgress) // 动画进行中的中心点
                    tab.centerPos.set(index * mTabWidth + mDoTabCPoint.x, mDoTabCPoint.y)
                }
                mOldSelectIndex -> {
                    mDoTabCPoint = mTabViewPath.getPathPoint(mProgress) // 动画进行中的中心点
                    tab.centerPos.set(index * mTabWidth + mDoTabCPoint.x, mDoTabCPoint.y)
                }
                else -> tab.centerPos.set(index * mTabWidth + mStayTabCPoint.x, mStayTabCPoint.y)
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