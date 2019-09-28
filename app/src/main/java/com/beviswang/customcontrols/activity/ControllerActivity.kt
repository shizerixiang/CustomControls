package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar

/**
 * 自定义控件展示
 * @author BevisWang
 * @date 2019/9/28 16:31
 */
class ControllerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        bindData()
    }

    private fun bindData() {
        bindToolbar("控件演示")
    }
}
