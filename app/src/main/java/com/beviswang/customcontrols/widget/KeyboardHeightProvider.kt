package com.beviswang.customcontrols.widget

import android.widget.PopupWindow
import android.view.Gravity
import android.graphics.drawable.ColorDrawable
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.beviswang.customcontrols.R

/**
 * 获取输入法高度
 * 使用方法：
 * onCreated 创建实例
 * onWindowFocusChanged 开始检测
 * onResume 绑定回调
 * onPause 解绑回调
 * onDestroy 关闭检测
 * 注意：启动时，使用 Handler 提交到 UI 线程队列的方式开始检测！
 * @author BevisWang
 */
class KeyboardHeightProvider(activity: Activity) : PopupWindow(activity) {
    /** The keyboard height observer  */
    private var observer: KeyboardHeightObserver? = null

    /** The cached landscape height of the keyboard  */
    private var keyboardLandscapeHeight: Int = 0

    /** The cached portrait height of the keyboard  */
    private var keyboardPortraitHeight: Int = 0

    /** The view that is used to calculate the keyboard height  */
    private var popupView: View? = null

    /** The parent view  */
    private var parentView: View? = null

    /** The root activity that uses this KeyboardHeightProvider  */
    private var activity: Activity? = activity

    init {
        val inflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.popupView = inflater.inflate(R.layout.keyboard_popup_window, null, false)
        contentView = popupView
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        parentView = activity.findViewById(android.R.id.content)
        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT
        popupView?.viewTreeObserver?.addOnGlobalLayoutListener {
            if (popupView != null) {
                handleOnGlobalLayout()
            }
        }
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun start() {
        if (!isShowing && parentView?.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    fun close() {
        this.observer = null
        dismiss()
    }

    /**
     * Set the keyboard height observer to this provider. The
     * observer will be notified when the keyboard height has changed.
     * For example when the keyboard is opened or closed.
     *
     * @param observer The observer to be added to this provider.
     */
    fun setKeyboardHeightObserver(observer: KeyboardHeightObserver?) {
        this.observer = observer
    }

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    private fun getScreenOrientation(): Int {
        return activity!!.resources.configuration.orientation
    }

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity window height.
     */
    private fun handleOnGlobalLayout() {

        val screenSize = Point()
        activity!!.windowManager.defaultDisplay.getSize(screenSize)

        val rect = Rect()
        popupView!!.getWindowVisibleDisplayFrame(rect)

        // REMIND, you may like to change this using the fullscreen size of the phone
        // and also using the status bar and navigation bar heights of the phone to calculate
        // the keyboard height. But this worked fine on a Nexus.
        val orientation = getScreenOrientation()
        val keyboardHeight = screenSize.y - rect.bottom

        when {
            keyboardHeight == 0 -> notifyKeyboardHeightChanged(0, orientation)
            orientation == Configuration.ORIENTATION_PORTRAIT -> {
                this.keyboardPortraitHeight = keyboardHeight
                notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation)
            }
            else -> {
                this.keyboardLandscapeHeight = keyboardHeight
                notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation)
            }
        }
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        if (observer != null) {
            observer!!.onKeyboardHeightChanged(height, orientation)
        }
    }

    interface KeyboardHeightObserver {
        /**
         * 输入法高度发生改变时回调
         * @param height 输入法高度
         * @param orientation 方向：[Configuration.ORIENTATION_PORTRAIT]
         * or [Configuration.ORIENTATION_LANDSCAPE]
         */
        fun onKeyboardHeightChanged(height: Int, orientation: Int)
    }
}