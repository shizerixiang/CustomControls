package com.beviswang.customcontrols.graphics.equation

import android.graphics.Path
import android.graphics.PointF
import com.beviswang.customcontrols.loge

/**
 * 直线方程（静态，比较节省性能，但无法动态根据点更新方程）
 * 直线公式：y = k * x + b
 * @author BevisWang
 * @date 2019/9/9 16:44
 */
class LinearEquation private constructor() : IEquation {
    var p1: PointF? = null
    var p2: PointF? = null
    var k: Float = 0f   // 斜率
    var b: Float = 0f   // 偏移量

    constructor(k: Float, b: Float) : this() {
        setKB(k, b)
    }

    constructor(p1: PointF, p2: PointF) : this() {
        set2Point(p1, p2)
    }

    constructor(p1: PointF, k: Float? = null, degreeX: Float? = null) : this() {
        if (k != null) setPointK(p1, k)
        if (degreeX != null) setPointDegreeX(p1, degreeX)
    }

    /**
     * 通过斜率和偏移量生成直线方程
     * @param k 斜率
     * @param b 偏移量
     */
    private fun setKB(k: Float, b: Float) {
        this.k = k
        this.b = b
    }

    /**
     * 通过直线上两点生成直线方程
     * @param p1 点一
     * @param p2 点二
     */
    protected fun set2Point(p1: PointF, p2: PointF) {
        this.p1 = p1
        this.p2 = p2
        k = (p2.y - p1.y) / (p2.x - p1.x)
        b = p1.y - (k * p1.x)
    }

    /**
     * 通过直线上一点和斜率生成直线方程
     * @param p1 点
     * @param k 斜率
     */
    private fun setPointK(p1: PointF, k: Float) {
        this.k = k
        b = p1.y - (k * p1.x)
    }

    /**
     * 通过直线上一点和与 X 轴的夹角生成直线方程
     * @param p1 点
     * @param degreeX 直线与 X 轴的夹角
     */
    private fun setPointDegreeX(p1: PointF, degreeX: Float) {
        val px2 = p1.x + (p1.y / Math.tan(degreeX.toDouble())).toFloat()
        val py2 = 0f
        set2Point(p1, PointF(px2, py2))
    }

    override fun getY(x: Float) = k * x + b

    override fun getX(y: Float) = (y - b) / k

    override fun getCurPoint(p: PointF, progress: Float) {
        if (p1 == null || p2 == null) return
        val x = (p2!!.x - p1!!.x) * progress + p1!!.x
        val y = getY(x)
        p.set(x, y)
    }

    override fun getLinePath(path: Path) {
        if (p1 == null || p2 == null) return
        path.reset()
        path.moveTo(p1!!.x, p1!!.y)
        path.lineTo(p2!!.x, p2!!.y)
    }

    /**
     * 获取经过指定点并垂直于该直线的直线方程
     * @param p 垂线经过的点
     * @return 经过指定点的该直线的垂线
     */
    fun getVerticalLinearEquation(p: PointF): LinearEquation {
        val vk = -1 / k
        loge("k = $k    vk = $vk")
        val vb = p.y - (vk * p.x)
        return LinearEquation(vk, vb)
    }

    /**
     * 通过点和距离，获取直线上的另外两点
     * @param p 中点
     * @param distance 距离
     * @return 在这条直线上，离中点距离相等两点
     */
    fun getPointsByDistance(p: PointF, distance: Float): Array<PointF> {
        val pArray = EquationHelper.getPointsByDistanceInLine(this, distance, p)
        p1 = pArray[0]
        p2 = pArray[1]
        return pArray
    }
}