package com.beviswang.customcontrols.graphics.equation

import android.graphics.Path
import android.graphics.PointF
import com.beviswang.customcontrols.graphics.PointHelper

/**
 * 动态直线方程（动态根据现在的点进行方程更新，比较吃性能）
 * 直线公式：y = k * x + b
 * @author BevisWang
 * @date 2019/9/9 16:44
 */
class LinearDynamicEquation private constructor() : IEquation {
    private var p1: PointF = PointF()
    private var p2: PointF = PointF()
    private var k: Float = 0f   // 斜率
    private var b: Float = 0f   // 偏移量

    constructor(p1: PointF, p2: PointF) : this() {
        set2Point(p1, p2)
    }

    /**
     * 通过直线上两点生成直线方程
     * @param p1 点一
     * @param p2 点二
     */
    private fun set2Point(p1: PointF, p2: PointF) {
        this.p1 = p1
        this.p2 = p2
        update()
    }

    /** 更新方程 */
    fun update() {
        k = (p2.y - p1.y) / (p2.x - p1.x)
        b = p1.y - (k * p1.x)
    }

    override fun getY(x: Float): Float {
        update()
        return k * x + b
    }

    override fun getX(y: Float): Float {
        update()
        return (y - b) / k
    }

    override fun getCurPoint(p: PointF, progress: Float) {
        update()
        val x = (p2.x - p1.x) * progress + p1.x
        val y = getY(x)
        p.set(x, y)
    }

    override fun getLinePath(path: Path) {
        path.reset()
        path.moveTo(p1.x, p1.y)
        path.lineTo(p2.x, p2.y)
    }

    /** 获取 p1 到 p2 方向的延长 distance 距离的点 */
    fun getP1ToP2DistancePoint(distance: Float): PointF {
        update()
        return PointHelper.getPointByDistance(p2, k, b, distance, p2.x > p1.x)
    }
}