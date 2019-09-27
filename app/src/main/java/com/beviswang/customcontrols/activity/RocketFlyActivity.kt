package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import kotlinx.android.synthetic.main.activity_rocket_fly.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 飞行的火箭
 * @author BevisWang
 * @date 2019/9/5 9:41
 */
class RocketFlyActivity : BaseActivity() {
    private var mSpeed: Float = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rocket_fly)

        bindData()
    }

    private fun bindData() {
        rmv_rocket_fly.setSpeed(mSpeed)
        bindToolbar("飞行的火箭")
        btn_rocket_fly_speed_up.onClick {
            mSpeed+=0.2f
            if (mSpeed > 10f) mSpeed = 10f
            rmv_rocket_fly.setSpeed(mSpeed)
        }
        btn_rocket_fly_speed_down.onClick {
            mSpeed -= 0.2f
            if (mSpeed < 0.1)mSpeed = 0.1f
            rmv_rocket_fly.setSpeed(mSpeed)
        }
    }
}
