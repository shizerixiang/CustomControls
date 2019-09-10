package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.beviswang.customcontrols.graphics.evaluator.CirclePointEvaluator
import com.beviswang.customcontrols.graphics.evaluator.LinearPointEvaluator
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * 混合图形画板
 * @author BevisWang
 * @date 2019/9/10 10:55
 */
class MixedBoardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    private val mDefWidth: Int = 300
    private val mDefHeight: Int = 300
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mDrawAnimator: ValueAnimator? = null
    private var mPoint: PointF = PointF(0f, 0f)
    private var mPath: Path = Path()

    init {
        mPaint.color = Color.BLACK
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 4f
        doAsync {
            Thread.sleep(800)
            uiThread {
                doAnimator()
            }
        }
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

    /** 绘制图形动画 */
    private fun doAnimator() {
//        mDrawAnimator?.cancel()
//        mDrawAnimator = ValueAnimator.ofObject(LinearPointEvaluator(mPoint), PointF(0f, 0f), PointF(width / 4f, height / 4f))
//        mDrawAnimator?.addUpdateListener { invalidate() }
//        mDrawAnimator?.duration = 400
//        mDrawAnimator?.start()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        drawGraph1(canvas)
    }

    private fun drawGraph1(canvas: Canvas) {
//        canvas.save()
        canvas.translate(width / 2f, height / 2f)
//        canvas.drawPoint(mPoint.x, mPoint.y, mPaint)
//        canvas.restore()

        val lpe = LinearPointEvaluator(mPoint)
        (0..100).forEach {
            lpe.evaluate(it / 100f, PointF(0f, 0f), PointF(width / 4f, 0f))
            canvas.drawPoint(mPoint.x, mPoint.y, mPaint)
        }

        val cpe = CirclePointEvaluator(100f, 360f, mPoint)
        (0..100).forEach {
            cpe.evaluate(it / 100f, PointF(0f, 0f), PointF(width / 4f, 0f))
            canvas.drawPoint(mPoint.x, mPoint.y, mPaint)
        }

        (0..100).forEach {
            cpe.evaluate(it / 100f, PointF(0f, 0f), PointF(0f, 0f))
            canvas.drawPoint(mPoint.x, mPoint.y, mPaint)
        }
    }
}