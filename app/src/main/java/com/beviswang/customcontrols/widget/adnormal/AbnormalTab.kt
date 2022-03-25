package com.beviswang.customcontrols.widget.adnormal

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.beviswang.customcontrols.graphics.path.ViewPath

/**
 * 反常识 Tab 类
 * @author BevisWong
 * @date 2022/3/25
 */
class AbnormalTab {
    private var radius: Int = 0 // Tab 半径
    var width: Int = 0 // Tab 宽度
    var parent: AbnormalTabLayout? = null // 容器
    var centerPos: PointF = PointF() // 中心点坐标
    var drawable: Drawable? = null // Tab 图标

    /**
     * 尺寸发生改变时，进行位置更新
     * @param r tab 半径
     * @param cp 中心点
     */
    fun onSizeChanged(r: Int, cp: PointF = centerPos) {
        radius = r
        centerPos = cp
        drawable!!.setBounds(radius / 2, radius / 2, radius / 2 + radius, radius / 2 + radius)
    }

    /** 设置图像资源 */
    fun setDrawableId(@DrawableRes icId: Int) {
        if (parent?.context == null) return
        drawable = ContextCompat.getDrawable(parent!!.context, icId)
    }

    /** 绘制 Tab */
    fun draw(canvas: Canvas?) {
        if (canvas == null) return
        canvas.translate(centerPos.x - radius, centerPos.y - radius)
        drawable?.draw(canvas)
        canvas.translate(radius - centerPos.x, radius - centerPos.y)
    }
}