package com.beviswang.customcontrols.graphics.equation

import android.graphics.PointF

/**
 * 圆的标准方程
 * (x-a)^2 + (y-b)^2 = r^2
 *
 * 原点坐标：(x0,y0)
 * 半径：r
 * 角度：ao
 * 
 * 则圆上任一点为：（x1,y1）
 * x1   =   x0   +   r   *   cos(ao   *   3.14   /180 )
 * y1   =   y0   +   r   *   sin(ao   *   3.14   /180 )
 * 
 * @author BevisWang
 * @date 2019/9/9 17:47
 */
@Deprecated("暂时无法使用")
class CircleEquation private constructor(): IEquation {
    private var o: PointF = PointF(0f, 0f)          // 圆心
    private var r: Float = 0f                               // 圆半径
    private var ao:Float = 0f

    constructor(o:PointF,r:Float):this(){
        setOR(o, r)
    }

    private fun setOR(o: PointF, r: Float) {
        this.o = o
        this.r = r
    }

    override fun getX(y: Float): Float {
        return 0f
    }

    override fun getY(x: Float): Float {
        return 0f
    }
}