package com.beviswang.customcontrols

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.beviswang.customcontrols.util.ToastHelper

/**
 * BaseActivity
 * @author BevisWang
 * @date 2019/8/30 10:18
 */
abstract class BaseActivity : AppCompatActivity() {
    private var isFirstDeniedRequest: Boolean = true // 是否第一次拒绝权限申请

    fun showMsg(msg: String?) = ToastHelper.makeToast(applicationContext, msg ?: "null")

    /** 请求权限 */
    fun requestBasePermissions(permissions: Array<out String>, requestCode: Int) {
        val deniedPermissions = permissions.filter { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || deniedPermissions.isNullOrEmpty()) {
            onBaseRequestGranted(requestCode)
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, deniedPermissions[0])) {
            showMsg("请开通相关权限，否则无法正常使用本应用！")
            isFirstDeniedRequest = false
        }
        ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), requestCode)
    }

    /** 申请权限通过 */
    open fun onBaseRequestGranted(requestCode: Int) {}

    /**
     * 申请权限拒绝
     * @param requestCode 申请权限 CODE
     * @param isFirstDeniedRequest 是否第一次拒绝
     */
    open fun onBaseRequestDenied(requestCode: Int, isFirstDeniedRequest: Boolean) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.none { it == PackageManager.PERMISSION_DENIED }) onBaseRequestGranted(requestCode)
        else onBaseRequestDenied(requestCode, isFirstDeniedRequest)
    }
}