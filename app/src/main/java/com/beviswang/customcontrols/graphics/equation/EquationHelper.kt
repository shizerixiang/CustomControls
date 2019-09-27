package com.beviswang.customcontrols.graphics.equation

import android.graphics.PointF

/**
 * 方程工具类
 * @author BevisWang
 * @date 2019/9/27 15:34
 */
object EquationHelper {
    /**
     * 通过直线上的点及直线方程，获取该点在直线上指定距离的两点
     * @param line 直线方程
     * @param distance 距离
     * @param p 基点
     * @return 对应线条上的两点
     */
    fun getPointsByDistanceInLine(line: LinearEquation, distance: Float, p: PointF): Array<PointF> {
//        val x1 = distance / Math.sqrt(1 + Math.pow(line.k.toDouble(), 2.0)).toFloat() + p.x
//        val x2 = PointHelper.getMirrorValue(p.x, x1)
//        return arrayOf(PointF(x1, line.k * x1 + line.b), PointF(x2, line.k * x2 + line.b))

        /**
         * 设定基点在原点 o 上，则得到经过基点的直线公式为 y = kx
         * 由斜率 k = tanA = y/x 三角函数可得出：distance^2 = k^2*x^2+x^2 = x^2(k^2+1)
         * x^2 = distance^2/(k^2+1)
         * 即： x1 = Math.sqrt(distance^2/(k^2+1))
         *      x2 = -Math.sqrt(distance^2/(k^2+1))
         * 最后将基点坐标加上
         */
        val absX = Math.sqrt(Math.pow(distance.toDouble(), 2.0) / (Math.pow(line.k.toDouble(), 2.0) + 1)).toFloat()
        val x1 = absX + p.x
        val x2 = -absX + p.x
        return arrayOf(PointF(x1, line.k * x1 + line.b), PointF(x2, line.k * x2 + line.b))
    }
}