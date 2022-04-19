package com.beviswang.customcontrols.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

/**
 * 绘制方法工具
 * @author BevisWong
 * @date 2022/4/18
 */
object DrawHelper {
    /**
     * 绘制裁剪
     * @param canvas canvas
     * @param mode 裁剪模式 [PorterDuff.Mode]
     * @param src 需要裁剪的图像
     * @param dst 裁剪图形
     */
    fun Paint.drawXfermode(
        canvas: Canvas,
        mode: PorterDuff.Mode = PorterDuff.Mode.CLEAR,
        src: () -> Unit,
        dst: () -> Unit
    ) = canvas.saveLayer(
        0f,
        0f,
        canvas.width.toFloat(),
        canvas.height.toFloat(),
        null
    ).apply {
        src()
        xfermode = PorterDuffXfermode(mode)
        dst()
        xfermode = null
        canvas.restoreToCount(this)
    }
}