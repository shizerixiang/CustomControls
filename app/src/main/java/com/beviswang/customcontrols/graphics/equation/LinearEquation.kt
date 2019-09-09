package com.beviswang.customcontrols.graphics.equation

import android.graphics.PointF

/**
 * 直线方程
 * 直线公式：y = k * x + b
 * @author BevisWang
 * @date 2019/9/9 16:44
 */
class LinearEquation private constructor() : IEquation {
    private var k: Float = 0f   // 斜率
    private var b: Float = 0f   // 偏移量

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
    private fun set2Point(p1: PointF, p2: PointF) {
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
        // TODO 通过夹角求直线方程
    }

    override fun getY(x: Float) = k * x + b

    override fun getX(y: Float) = (y - b) / k

    class Builder() {
        private var mLinearEquation: LinearEquation
        private var p1: PointF? = null
        private var k: Float? = null

        init {
            mLinearEquation = LinearEquation()
        }

        fun addPoint(p: PointF): Builder {
            if (p1 == null) {
                this.p1 = p
                return this
            }
            mLinearEquation.set2Point(p1!!, p)
            return this
        }

        fun addK(k: Float): Builder {
            // TODO add k
            return this
        }

        fun create(): LinearEquation = mLinearEquation
    }
}