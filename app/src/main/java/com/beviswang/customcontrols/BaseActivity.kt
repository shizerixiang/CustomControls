package com.beviswang.customcontrols

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.IdRes
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
    fun requestGPHPermissions(permissions: Array<out String>, requestCode: Int) {
        val deniedPermissions = permissions.filter { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || deniedPermissions.isNullOrEmpty()) {
            onRequestPermissionsGranted(requestCode)
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, deniedPermissions[0])) {
            showMsg("请开通相关权限，否则无法正常使用本应用！")
            isFirstDeniedRequest = false
        }
        ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), requestCode)
    }

    /** 所有申请权限通过 */
    open fun onRequestPermissionsGranted(requestCode: Int) {}

    /**
     * 申请权限拒绝
     * @param requestCode 申请权限 CODE
     * @param permissions 拒绝的权限
     */
    open fun onRequestPermissionsDenied(requestCode: Int, permissions: List<String>) {}

    /**
     * 回调不在提醒的权限
     * @param requestCode 申请权限 CODE
     * @param permissions 不再提醒的权限
     */
    open fun onRequestPermissionsReject(requestCode: Int, permissions: List<String>) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (grantResults.none { it == PackageManager.PERMISSION_DENIED }) onRequestPermissionsGranted(requestCode)
//        else onRequestPermissionsDenied(requestCode, isFirstDeniedRequest)

        val rejectPermissionList = ArrayList<String>()    // 不在提醒的权限
        val deniedPermissionList = ArrayList<String>()    // 拒绝的权限
        val shouldShowRationale = permissions.map { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }
        grantResults.forEachIndexed { i, result ->
            if (result != PackageManager.PERMISSION_GRANTED && !shouldShowRationale[i]) {
                // 拒绝授权后，勾选不再提醒按钮
                rejectPermissionList.add(permissions[i])
            } else if (result != PackageManager.PERMISSION_GRANTED) {
                // 拒绝授权
                deniedPermissionList.add(permissions[i])
            }
        }
        // 有一个以上权限不再提醒就回调
        if (rejectPermissionList.size > 0) {
            onRequestPermissionsReject(requestCode,rejectPermissionList)
            return
        }
        // 有一个以上权限拒绝就回调
        if (deniedPermissionList.size >0) {
            onRequestPermissionsDenied(requestCode,deniedPermissionList)
            return
        }
        onRequestPermissionsGranted(requestCode)
    }
}