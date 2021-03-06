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
     * @param degrees 获取点的角度
     * @return 圆上的点坐标
     */
    fun getPointOnCircle(centerPoint: PointF, r: Float, degrees: Float) = PointF(getPointX(
            centerPoint.x, r, degrees), getPointY(centerPoint.y, r, degrees))

    /**
     * 获取圆上点的 X 轴坐标
     * @param centerX 圆心 X 轴坐标
     * @param r 半径
     * @param degrees 角度
     * @return 圆上点的 X 轴坐标
     */
    fun getPointX(centerX: Float, r: Float, degrees: Float) = centerX + r * Math.cos(Math.toRadians(degrees.toDouble())).toFloat()

    /**
     * 获取圆上点的 Y 轴坐标
     * @param centerY 圆心 Y 轴坐标
     * @param r 半径
     * @param degrees 角度
     * @return 圆上点的 Y 轴坐标
     */
    fun getPointY(centerY: Float, r: Float, degrees: Float) = centerY + r * Math.sin(Math.toRadians(degrees.toDouble())).toFloat()

    /**
     * 获取两点间的距离
     * @param p1 点一
     * @param p2 点二
     * @return 两点间的距离
     */
    fun getPointsDistance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.x
        return Math.sqrt(Math.pow(dx.toDouble(), 2.0) + Math.pow(dy.toDouble(), 2.0)).toFloat()
    }

    /**
     * 通过指定大小和方向，获取做圆周运动的圆上一点的向量终点
     * @param o 圆心坐标（坐标原点）
     * @param sp 圆上一点的起始点
     * @param distance 向量大小（起点与终点的距离）
     * @param direction 方向（顺时针：true，逆时针：false）
     */
    fun getVectorPointInCircle(o: PointF, sp: PointF, distance: Float, direction: Boolean): PointF {
        // 这里基于 o 点的坐标轴，即：o 为原点（相对坐标系）
        val x = sp.x - o.x
        val y = sp.y - o.y
        // 从圆心（原点）到圆上一点的直线的斜率 y = kx -> k = y / x
        val k = y / x
        // 通过斜率求出圆心到圆上一点的夹角
        val degrees = Math.abs(Math.toDegrees(Math.atan(k.toDouble())))
        // 正切直线与 x 轴的交点距离
        var dx = Math.sqrt((x * x) + (y * y).toDouble()) / Math.cos(Math.toRadians(degrees))
        // 判断在 x 轴的正方向还是反方向
        dx = if (x > 0) Math.abs(dx) else -Math.abs(dx)
        val vk = y / (x - dx)
        val m = dx * y / (dx - x)
        // 切线的直线公式：ty = vk * tx + m
        val tp = getPointByDirection(PointF(x, y), vk.toFloat(), m.toFloat(), distance, direction)
        return PointF(tp.x + o.x, tp.y + o.y)
    }

    private fun getPointByDirection(sp:PointF, k:Float, m:Float, distance:Float, isXIncreasingDir:Boolean):PointF{
        val absX = distance / Math.sqrt(1 + Math.pow(k.toDouble(), 2.0)).toFloat() + sp.x
        val tx = if (if (isXIncreasingDir) sp.y < 0 else sp.y > 0) absX else getMirrorValue(sp.x, absX)
        val ty = k * tx + m
        return PointF(tx, ty)
    }

    /**
     * 通过两点间的距离和其中一点的位置获取直线上另一个点的位置
     * 直线公式：y = k * x + m
     * 求两点距离公式：d = sqrt(1+vk^2)*abs(x-tx) = sqrt(1+(1/k^2))*abs(y-ty)
     * @param sp 起始点
     * @param k 直线斜率
     * @param m 直线在 x 轴的偏移量
     * @param distance 两点距离
     * @param isXIncreasingDir 是否为正方向
     */
    fun getPointByDistance(sp: PointF, k: Float, m: Float, distance: Float, isXIncreasingDir: Boolean): PointF {
        val absX = distance / Math.sqrt(1 + Math.pow(k.toDouble(), 2.0)).toFloat() + sp.x
        val tx = if (isXIncreasingDir) absX else getMirrorValue(sp.x, absX)
        val ty = k * tx + m
        return PointF(tx, ty)
    }

    /**
     * 获取一个镜面值
     * @param normalLineX 法线
     * @param x 原值
     * @return 镜面值
     */
    fun getMirrorValue(normalLineX: Float, x: Float) = 2 * normalLineX - x

    /**
     * 获取一个镜像点
     * @param normalLineP 法线点
     * @param sp 初始点
     * @return 镜像点
     */
    fun getMirrorPoint(normalLineP: PointF, sp: PointF) = PointF(getMirrorValue(
            normalLineP.x, sp.x), getMirrorValue(normalLineP.y, sp.y))

    /**
     * 获取点的角度 （点与原点的连线相对于 X 轴正方向的夹角）
     * @param o 原点在控件中的坐标
     * @param p 点击的点
     * @param rotate 旋转角度，默认不旋转
     * @return 角度 [0 - 359]
     */
    fun getPointDegree(o: PointF, p: PointF, rotate: Float = 0f): Float {
        val arc = Math.atan2(((p.y - o.y).toDouble()), (p.x - o.x).toDouble())
        var degree = (arc * (180 / Math.PI)).toFloat()
        degree += if (degree < 0) 360 + rotate else rotate
        if (degree > 360) degree -= 360
        if (degree < 0) degree += 360
        return degree
    }
}