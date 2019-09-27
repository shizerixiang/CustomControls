package com.beviswang.customcontrols

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import kotlinx.android.synthetic.main.layout_tool_bar.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.internals.AnkoInternals
import org.jetbrains.anko.sdk27.coroutines.onClick
import androidx.core.widget.PopupWindowCompat.showAsDropDown
import android.R.attr.bottom
import android.graphics.Rect
import org.jetbrains.anko.layoutInflater


inline fun <reified T : Activity> BaseActivity.startActivity(vararg params: Pair<String, Any?>) {
    AnkoInternals.internalStartActivity(this, T::class.java, params)
}

inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, Any?>) {
    AnkoInternals.internalStartActivity(this, T::class.java, params)
}

inline fun <reified T : Activity> BaseActivity.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) {
    AnkoInternals.internalStartActivityForResult(this, T::class.java, requestCode, params)
}

// ****************************************** Toolbar 相关 *****************************************
/**
 * 绑定 Toolbar
 * 在导入 layout_tool_bar 才可以使之生效
 * @param title 标题
 * @param backListener 返回按钮事件
 * @param menuVisibility 菜单是否显示
 * @param menuListener 菜单点击事件
 */
inline fun BaseActivity.bindToolbar(title: String, crossinline backListener: () -> Unit = { finish() },
                                    menuVisibility: Int = View.GONE, crossinline menuListener: () -> Unit) {
    iv_tool_bar_back?.onClick { backListener() } // 返回
    tv_tool_bar_title?.text = title
    iv_tool_bar_menu?.visibility = menuVisibility
    iv_tool_bar_menu?.onClick { menuListener() }
}

/**
 * 绑定 Toolbar
 * 在导入 layout_tool_bar 才可以使之生效
 * @param title 标题
 * @param backListener 返回按钮事件
 * @param menuVisibility 菜单是否显示
 * @param menuImageRes 菜单图标资源 id
 * @param menuListener 菜单点击事件
 */
inline fun BaseActivity.bindToolbar(title: String, crossinline backListener: () -> Unit = { finish() },
                                    menuVisibility: Int = View.GONE, menuImageRes: Int = R.drawable.ic_switch,
                                    crossinline menuListener: () -> Unit) {
    iv_tool_bar_back?.onClick { backListener() } // 返回
    tv_tool_bar_title?.text = title
    iv_tool_bar_menu?.visibility = menuVisibility
    iv_tool_bar_menu?.imageResource = menuImageRes
    iv_tool_bar_menu?.onClick { menuListener() }
}

/**
 * 绑定 Toolbar
 * 在导入 layout_tool_bar 才可以使之生效
 * @param title 标题
 * @param backListener 返回按钮事件
 * @param menuVisibility 菜单是否显示
 * @param menuImageRes 菜单图标资源 id
 * @param menuView 菜单弹出框布局
 * @param menuListener 菜单点击事件
 */
inline fun BaseActivity.bindToolbarWithMenu(title: String, crossinline backListener: () -> Unit = { finish() },
                                            menuVisibility: Int = View.GONE, menuImageRes: Int = R.drawable.ic_switch,
                                            menuView: View = baseContext.layoutInflater.inflate(R.layout.layout_pop_menu, null),
                                            crossinline menuListener: () -> Unit = { bindPopupWindow(iv_tool_bar_menu, menuView) }) {
    iv_tool_bar_back?.onClick { backListener() } // 返回
    tv_tool_bar_title?.text = title
    iv_tool_bar_menu?.visibility = menuVisibility
    iv_tool_bar_menu?.imageResource = menuImageRes
    iv_tool_bar_menu?.onClick { menuListener() }
}

/**
 * 绑定 Toolbar
 * 在导入 layout_tool_bar 才可以使之生效
 * @param title 标题
 * @param backListener 返回按钮事件
 */
inline fun BaseActivity.bindToolbar(title: String, crossinline backListener: () -> Unit = { finish() }) {
    iv_tool_bar_back?.onClick { backListener() } // 返回
    tv_tool_bar_title?.text = title
}

/**
 * 绑定弹出框
 * @param anchor 锚点，确定位置 View
 * @param contentView 弹窗内容 View
 */
fun bindPopupWindow(anchor: View, contentView: View) {
    val popWindow = PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
    popWindow.isOutsideTouchable = true
    popWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    anchor.onClick { popWindow.showAsDropDown(anchor) }
}

fun loge(str: String?) {
    Log.e("CustomControls", str ?: "null")
}

fun logd(str: String?) {
    Log.d("CustomControls", str ?: "null")
}

fun logi(str: String?) {
    Log.i("CustomControls", str ?: "null")
}