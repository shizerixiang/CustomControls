package com.beviswang.customcontrols.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager

/**
 * 自定义控件工具类
 * @author BevisWang
 * @date 2018/11/14 16:16
 */
object ViewHelper {
    /** 获取屏幕的宽 */
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(
                Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    /** 获取屏幕的高 */
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(
                Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    /** 获取状态栏高度 */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resource = context.resources
        val resourceId = resource.getIdentifier(
                "status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resource.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /** 获取虚拟按键高度 */
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resource = context.resources
        val resourceId = resource.getIdentifier(
                "navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resource.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /** 获取 ContentView 的高度 */
    fun getContentViewHeight(activity: Activity): Int {
        val rect = Rect()
        activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT).getDrawingRect(rect)
        return rect.height()
    }

    /** px 转 dp */
    fun px2dip(context: Context, px: Float): Float {
        // 系统提供的方法，仅支持 px 转各种单位
//        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value,context.resources.displayMetrics)
        val scale = context.resources.displayMetrics.density
        return px / scale + 0.5f
    }

    /** dp 转 px */
    fun dip2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    /** px 转 sp */
    fun px2sp(context: Context, px: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return px / scale + 0.5f
    }

    /** sp 转 px */
    fun sp2px(context: Context, sp: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return sp * scale + 0.5f
    }

    /**
     * 获取布局截图 bitmap
     * 官方推荐 PixelCopy 类做截屏处理
     * @param view 需要获取的 View
     */
    fun getViewScreenShot(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        return view.drawingCache
    }
}
