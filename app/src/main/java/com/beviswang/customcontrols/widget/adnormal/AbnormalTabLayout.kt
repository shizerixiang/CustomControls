package com.beviswang.customcontrols.widget.adnormal

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.graphics.evaluator.ColorEvaluator
import com.beviswang.customcontrols.graphics.path.ViewPath
import com.beviswang.customcontrols.loge
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
    private var mArgbAnimator: ValueAnimator? = null

    private var mShaderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
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

    private var mTabShaderX: Float = 0f
    private var mTabShaderY: Float = 4f
    private var mTabShaderAlpha: Float = 0.1f

    private var mTabSelectedChangedListener: (Int) -> Unit = {}

    init {
        initParams(context, attrs)
        mBgPaint.color = mBgColor
        mBgPaint.style = Paint.Style.FILL_AND_STROKE

        mTabPaint.color = mUnSelectColor
        mTabPaint.style = Paint.Style.FILL_AND_STROKE

        mShaderPaint.color = Color.BLACK
        mShaderPaint.alpha = (255 * mTabShaderAlpha).toInt()
        mShaderPaint.style = Paint.Style.FILL_AND_STROKE
        mShaderPaint.maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

    fun newTab(@DrawableRes drawableId: Int): AbnormalTab {
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
        // 会导致中断移动发生位置偏移，根本原因就是动画中的移动均为真实移动
//        mCurArcX = mTabArray[mSelectIndex].centerPos.x
//        doSelectAnimation(mTabArray[mOldSelectIndex].centerPos.x, mCurArcX)
        mCurArcX = mTabWidth * mSelectIndex + (mTabWidth / 2)
        doSelectAnimation(mTabWidth * mOldSelectIndex + (mTabWidth / 2), mCurArcX)
        mTabSelectedChangedListener(mSelectIndex)
    }

    fun setOnTabSelectedChanged(listener: (Int) -> Unit) {
        mTabSelectedChangedListener = listener
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

    private var mTempIndex: Int = -1

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        if (event.y < mTabPaddingTop) { // 没有触碰到 Tab 本身
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTempIndex = -1
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                mOldSelectIndex = mSelectIndex
                mTempIndex = (event.x / mTabWidth).toInt()
                if (mTempIndex > mTabArray.lastIndex) {
                    mTempIndex = mTabArray.lastIndex
                }
                if (mTempIndex < 0) {
                    mTempIndex = 0
                }
                selectTab(mTempIndex)
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
        mShaderPaint.alpha = (mTabShaderAlpha * 64).toInt()
        canvas.translate(0f, -4f)
        canvas.drawPath(mBgPath, mShaderPaint)
        canvas.translate(0f, 4f)
        canvas.drawPath(mBgPath, mBgPaint)
    }

    private var mTabRectRadius: Float = 0f

    private fun drawTabs(canvas: Canvas) {
        canvas.translate(0f, 4f)
//        mTabRectRadius = mTabRadius * (1 - (mProgress * 0.5f))
        mTabRectRadius = mTabRadius * (1 - mProgress)
        mTabArray.forEachIndexed { index, tab ->
            if (index == mSelectIndex) {
                mTabPaint.color = mBgColor
                // 绘制 Tab 阴影
                mShaderPaint.alpha = ((1 - mProgress) * mTabShaderAlpha * 255).toInt()
                canvas.drawRoundRect(
                    tab.centerPos.x - mTabRadius + mTabShaderX,
                    tab.centerPos.y - mTabRadius + mTabShaderY,
                    tab.centerPos.x + mTabRadius + mTabShaderX,
                    tab.centerPos.y + mTabRadius + mTabShaderY,
                    mTabRectRadius,
                    mTabRectRadius,
                    mShaderPaint
                )
                // 绘制 Tab 背景
                canvas.drawRoundRect(
                    tab.centerPos.x - mTabRadius,
                    tab.centerPos.y - mTabRadius,
                    tab.centerPos.x + mTabRadius,
                    tab.centerPos.y + mTabRadius,
                    mTabRectRadius,
                    mTabRectRadius,
                    mTabPaint
                )
                // 为 Tab 染色
                tab.drawable?.setTint(
                    ColorEvaluator.getInstance().evaluate(mProgress, mSelectedColor, mUnSelectColor)
                )
            } else {
                mTabPaint.color = mBgColor
                // 绘制 Tab 阴影
                if (index == mOldSelectIndex) {
                    mShaderPaint.alpha = (mProgress * mTabShaderAlpha * 255).toInt()
                    canvas.drawRoundRect(
                        tab.centerPos.x - mTabRadius + mTabShaderX,
                        tab.centerPos.y - mTabRadius + mTabShaderY,
                        tab.centerPos.x + mTabRadius + mTabShaderX,
                        tab.centerPos.y + mTabRadius + mTabShaderY,
                        mTabRadius - mTabRectRadius,
                        mTabRadius - mTabRectRadius,
                        mShaderPaint
                    )
                    // 为 Tab 染色
                    tab.drawable?.setTint(
                        ColorEvaluator.getInstance()
                            .evaluate(mProgress, mUnSelectColor, mSelectedColor)
                    )
                    // 绘制 Tab 背景
                    canvas.drawRoundRect(
                        tab.centerPos.x - mTabRadius,
                        tab.centerPos.y - mTabRadius,
                        tab.centerPos.x + mTabRadius,
                        tab.centerPos.y + mTabRadius,
                        mTabRadius - mTabRectRadius,
                        mTabRadius - mTabRectRadius,
                        mTabPaint
                    )
                } else {
                    // 为 Tab 染色
                    tab.drawable?.setTint(mUnSelectColor)
                }
            }
            tab.draw(canvas)
        }
        canvas.translate(0f, -4f)
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
        loge("startX=$startX   endX=$endX")
        val dx = endX - startX
        mDrawingAnimator?.cancel()
        mDrawingAnimator = ValueAnimator.ofFloat(startX, endX)
        mDrawingAnimator?.duration = 380
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