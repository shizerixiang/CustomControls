package com.beviswang.customcontrols.graphics

import android.graphics.PointF

/**
 * 绘制点的辅助类
 * @author BevisWang
 * @date 2018/12/11 11:38
 */
object PointHelper {
    /**
     * 获取圆上的点坐标
     *
     * 原点坐标：(x0,y0)
     * 半径：r
     * 角度：ao
     *
     * 则圆上任一点为：（x1,y1）
     * x1   =   x0   +   r   *   cos(ao   *   3.14   /180 )
     * y1   =   y0   +   r   *   sin(ao   *   3.14   /180 )
     *
     * @param centerPoint 圆心坐标
     * @param r 圆的半径
     * @param angel 获取点的角度
     * @return 圆上的点坐标
     */
    fun getPointOnCircle(centerPoint: PointF, r: Float, angel: Float): PointF {
        return PointF(getPointX(centerPoint.x, r, angel), getPointY(centerPoint.y, r, angel))
    }

    /**
     * 获取圆上点的 X 轴坐标
     * @param centerX 圆心 X 轴坐标
     * @param r 半径
     * @param angel 角度
     * @return 圆上点的 X 轴坐标
     */
    fun getPointX(centerX: Float, r: Float, angel: Float): Float {
        return centerX + r * Math.cos(angel * Math.PI / 180).toFloat()
    }

    /**
     * 获取圆上点的 Y 轴坐标
     * @param centerY 圆心 Y 轴坐标
     * @param r 半径
     * @param angel 角度
     * @return 圆上点的 Y 轴坐标
     */
    fun getPointY(centerY: Float, r: Float, angel: Float): Float {
        return centerY + r * Math.sin(angel * Math.PI / 180).toFloat()
    }
}