package com.beviswang.customcontrols.graphics.equation

import android.graphics.PointF

/**
 * 方程
 * @author BevisWang
 * @date 2019/9/9 16:46
 */
interface IEquation {
    /**
     * 通过 y 获取 x
     * @param y
     * @return x
     */
    fun getX(y: Float): Float

    /**
     * 通过 x 获取 y
     * @param x
     * @return y
     */
    fun getY(x: Float): Float

    /**
     * 根据距离进度比例，获取两点间的点
     * @param progress 进度比例
     * @return 获得的点
     */
    fun getCurPoint(progress:Float):PointF
}