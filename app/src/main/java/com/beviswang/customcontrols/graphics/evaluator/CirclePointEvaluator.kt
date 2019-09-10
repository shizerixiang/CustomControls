package com.beviswang.customcontrols.graphics.evaluator

import android.animation.TypeEvaluator
import android.graphics.PointF
import com.beviswang.customcontrols.graphics.PointHelper

/**
 * 圆形路径移动的求值器
 * @author BevisWang
 * @date 2019/9/10 8:44
 */
class CirclePointEvaluator constructor(var mRadius: Float /* 半径 */, var mTotalDegrees: Float = 360f /* 角度 */,
                                       var mPoint: PointF? = null)
    : TypeEvaluator<PointF> {

    override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF {
        var degrees = fraction * mTotalDegrees // 当前圆上一点的角度
        val x = fraction * (endValue.x - startValue.x) + startValue.x
        val y = fraction * (endValue.y - startValue.y) + startValue.y

//        var r = mRadius
        var r = fraction * mRadius
        if (fraction < 0.5f) r *= 2 else r = (mRadius - r) * 2

//        if (degrees > 180) degrees = 180 - degrees
        return if (mPoint != null) {
            mPoint?.set(PointHelper.getPointOnCircle(PointF(x, y), r, degrees))
            mPoint!!
        } else PointHelper.getPointOnCircle(PointF(x, y), r, degrees)
    }
}