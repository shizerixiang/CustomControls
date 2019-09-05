package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar

/**
 * 飞行的火箭
 * @author BevisWang
 * @date 2019/9/5 9:41
 */
class RocketFlyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rocket_fly)

        bindData()
    }

    private fun bindData() {
        bindToolbar("飞行的火箭")
    }
}
