package com.beviswang.customcontrols.util

import android.content.Context
import android.widget.Toast

/**
 * 提示工具继承类
 * @author BevisWang
 * @date 2019/4/9 16:25
 */
object ToastHelper {
    private var TOAST: Toast? = null

    fun showNoNetwork(context: Context?) = makeToast(context, "网络异常")

    fun showRefreshFailed(context: Context?) = makeToast(context, "刷新失败")

    fun showLoadFailed(context: Context?) = makeToast(context, "服务器无响应")

    fun makeToast(context: Context?, text: String) {
        TOAST?.cancel()
        TOAST = Toast.makeText(context, text, Toast.LENGTH_LONG)
        TOAST?.show()
    }

    fun makeToast(context: Context?, text: Int) {
        TOAST?.cancel()
        TOAST = Toast.makeText(context, text, Toast.LENGTH_LONG)
        TOAST?.show()
    }

    fun showLoginEmptyInput(context: Context?) = makeToast(context, "账号或密码不可为空")

    fun showLoginSucceed(context: Context?) = makeToast(context, "登录成功")

    fun showLoginFailed(context: Context?, state: String) = makeToast(context, "登录失败！code:$state")
}