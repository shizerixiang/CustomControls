package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar

/**
 * 触摸工具控件演示
 * @author BevisWang
 * @date 2019/9/21 11:50
 */
class TouchToolActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch_tool)

        bindToolbar("触摸控件演示")
    }
}
