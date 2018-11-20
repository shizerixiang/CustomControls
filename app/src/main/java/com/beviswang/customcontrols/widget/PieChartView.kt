package com.beviswang.customcontrols.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.HashMap

/**
 * 饼状图
 * @author BevisWang
 * @date 2018/11/16 11:46
 */
class PieChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    // 默认宽高
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    // 画笔
    private val mArcPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG) // 扇形画笔
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)       // 文字画笔
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)       // 线条画笔
    // 扇形属性
    private var mArcRectF = RectF() // 扇形的外切矩形
    private var mArcDiameter = 0f   // 扇形直径
    private var mArcDx = 0f         // 扇形 x 轴的间距
    private var mArcDy = 0f         // 扇形 y 轴的间距
    // 控制线条的因子
    private var mXFactor = dip2px(context, 10f) // 线条长度
    private var mYFactor = 8f // 线条高度，值越大约矮（Max 90）
    // 左右方向线条的 x 轴位置
    private var leftX: Float = 0f
    private var rightX: Float = 0f
    // 实录数据（key 为标记，value 为比例）
    private lateinit var mDataMap: HashMap<String, Float>
    private var mColors: HashMap<String, Int> = HashMap()
    // 中心圆数据，用于产生扇形图间的间隙
    private var mCenterX: Float = 0f
    private var mCenterY: Float = 0f
    // 记录手指的位置
    private var mLastX: Float = 0f
    private var mLastY: Float = 0f
    // 旋转角度
    private var mRotateAngle: Float = 0f
    // 扇形的起始位置
    private var mStartAngle: Float = 0f

    init {
        mArcPaint.style = Paint.Style.FILL_AND_STROKE
        mArcPaint.color = Color.WHITE

        mTextPaint.color = Color.WHITE
        mTextPaint.textSize = sp2px(context, 12f)

        mLinePaint.color = Color.WHITE
        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.strokeWidth = 1f
    }

    /**
     * 设置数据
     * @param dataMap 饼状图数据
     */
    fun setData(dataMap: HashMap<String, Float>) {
        mDataMap = dataMap
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val height = height - paddingTop - paddingBottom
        val width = width - paddingStart - paddingEnd
        mCenterX = width / 2f + paddingStart
        mCenterY = height / 2f + paddingTop
        // 根据控件尺寸，调整扇形属性
        mArcDx = paddingStart.toFloat()
        mArcDy = paddingTop.toFloat()
        if (height > width) {
            mArcDiameter = width.toFloat()
            mArcDy += (height - width) / 2f
        } else {
            mArcDiameter = height.toFloat()
            mArcDx += (width - height) / 2f
        }
        mArcRectF = RectF(0f, 0f, mArcDiameter, mArcDiameter)
        mArcRectF.offset(mArcDx, mArcDy)
        // 调整线条属性
        leftX = mArcDx - (mXFactor * 2)
        rightX = mArcDx + mArcDiameter + (mXFactor * 2)
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

    /**
     * 两点间的距离公式：
     * ab = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))
     * 两点直线与 x 轴的夹角
     * var p1:Point = new Point(40,30);
     * var p2:Point = new Point(80,60);
     *
     * var angle:Number = Math.atan2((p2.y-p1.y), (p2.x-p1.x)) // 弧度
     * var theta:Number = angle*(180/Math.PI); // 角度
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val dx = x - mCenterX
                val dy = y - mCenterY
                val ab = Math.sqrt((dx * dx + dy * dy).toDouble())
                if (ab > mArcDiameter / 2) return super.onTouchEvent(event) // 不在圆上
            }
            MotionEvent.ACTION_MOVE -> {
                if (x == mLastX && y == mLastY) return true // 手指停止移动
                // 上次点与 x 轴的夹角
                val arc1 = Math.atan2(((mCenterY - mLastY).toDouble()), (mCenterX - mLastX).toDouble())
                val angle1 = (arc1 * (180 / Math.PI)).toFloat()
                // 当前点与 x 轴的夹角
                val arc2 = Math.atan2(((mCenterY - y).toDouble()), (mCenterX - x).toDouble())
                val angle2 = (arc2 * (180 / Math.PI)).toFloat()

                mRotateAngle = angle2 - angle1
                mStartAngle += mRotateAngle
                if (mStartAngle < 0) mStartAngle += 360
                if (mStartAngle > 360) mStartAngle -= 360

//                Log.e(javaClass.simpleName, "angle1=$angle1  angle2=$angle2  rotateAngle=$mRotateAngle")
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        postInvalidate()
        mLastX = x
        mLastY = y
        return true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawArc(canvas)
    }

    /**
     * 绘制饼状图
     * 原点坐标：(x0,y0)
     * 半径：r
     * 角度：ao
     *
     * 则圆上任一点为：（x1,y1）
     * x1   =   x0   +   r   *   cos(ao   *   3.14   /180 )
     * y1   =   y0   +   r   *   sin(ao   *   3.14   /180 )
     * @param canvas 画板
     * 注意：HashMap.value 总和必须为 1f
     */
    private fun drawArc(canvas: Canvas?) {
        var startAngle = mStartAngle
        val isRandomColor: Boolean = mColors.isEmpty()
        mDataMap.map {
            val d = it.value * 360
            // 绘制扇形
            if (isRandomColor) mColors[it.key] = getRandomColor()
            mArcPaint.color = mColors[it.key] ?: getRandomColor()
            canvas?.drawArc(mArcRectF, startAngle, d, true, mArcPaint)
            // 绘制提示文字
            val rad = mArcDiameter / 2f
            var ao = startAngle + (d / 2f)
            if (ao > 360) ao -= 360
            var x = mCenterX + (rad * Math.cos(ao * Math.PI / 180)).toFloat()
            var y = mCenterY + (rad * Math.sin(ao * Math.PI / 180)).toFloat()
            val path = Path()
            path.moveTo(x, y)
            when {
                ao < 90 -> {
                    x += mXFactor
                    y += (ao.toInt() % 90) / mYFactor
                    path.lineTo(x, y)
                    x = rightX
                    path.lineTo(x, y)
                    mTextPaint.textAlign = Paint.Align.LEFT
                }
                ao < 180 -> {
                    x -= mXFactor
                    y += (90 - (ao.toInt() % 90)) / mYFactor
                    path.lineTo(x, y)
                    x = leftX
                    path.lineTo(x, y)
                    mTextPaint.textAlign = Paint.Align.RIGHT
                }
                ao < 270 -> {
                    x -= mXFactor
                    y += -(ao.toInt() % 90) / mYFactor
                    path.lineTo(x, y)
                    x = leftX
                    path.lineTo(x, y)
                    mTextPaint.textAlign = Paint.Align.RIGHT
                }
                else -> {
                    x += mXFactor
                    y += -(90 - (ao.toInt() % 90)) / mYFactor
                    path.lineTo(x, y)
                    x = rightX
                    path.lineTo(x, y)
                    mTextPaint.textAlign = Paint.Align.LEFT
                }
            }
            canvas?.drawText(it.key, x, y + (mTextPaint.textSize / 2f), mTextPaint)
            canvas?.drawPath(path, mLinePaint)
            startAngle += d
            if (startAngle > 360) startAngle -= 360
            it
        }
    }

    /** 获取随机颜色 */
    @ColorInt
    fun getRandomColor(): Int {
        val r = Random().nextInt(255)
        val g = Random().nextInt(255)
        val b = Random().nextInt(255)
        return Color.rgb(r, g, b)
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
}