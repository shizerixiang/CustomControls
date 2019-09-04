package com.beviswang.customcontrols.util

import android.graphics.Color

/**
 * 色彩调整工具类
 * @author BevisWang
 * @date 2019/7/27 13:49
 */
object ColorHelper {
    private const val REGEX_COLOR_STRING = "[#][\\d|abcdef]{6}|[#][\\d|abcdef]{8}"

    /**
     * 获取同色系颜色
     * @param color 基色
     * @param addSaturation 需要增加的饱和度 [0-1]
     */
    fun getHomochromaticSystemColor(color: Int, addSaturation: Float): Int {
        val oldHsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(color, oldHsv)
        val newHsv = floatArrayOf(oldHsv[0], oldHsv[1] + addSaturation, oldHsv[2])
        return Color.HSVToColor(newHsv)
    }

    /**
     * 获取更深的颜色
     * @param color 基色
     */
    fun getDeepColor(color: Int): Int {
        val a = Color.alpha(color)
        var r = Color.red(color)
        var g = Color.green(color)
        var b = Color.blue(color)

        if (r < 45) r = 0 else r -= 45
        if (g < 45) g = 0 else g -= 45
        if (b < 45) b = 0 else b -= 45

        return Color.argb(a, r, g, b)
    }

    /** 是否是颜色字符串 */
    fun isColorString(colorStr: String) = Regex(REGEX_COLOR_STRING, RegexOption.IGNORE_CASE).matches(colorStr) && colorStr.length != 8
}