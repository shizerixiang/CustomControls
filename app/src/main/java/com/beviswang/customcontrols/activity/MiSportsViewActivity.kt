package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import kotlinx.android.synthetic.main.activity_mi_sports_view.*

/**
 * 仿小米运动自定义控件展示
 * @author BevisWang
 * @date 2019/9/4 10:37
 */
class MiSportsViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_sports_view)

        bindToolbar("仿小米运动控件")
    }

    override fun onResume() {
        super.onResume()
        fv_mi_sports_flipping.startLoadedAnimation()
    }

    override fun onPause() {
        super.onPause()
        fv_mi_sports_flipping.pauseLoadedAnimation()
    }
}
