package com.beviswang.customcontrols.graphics.equation

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
}