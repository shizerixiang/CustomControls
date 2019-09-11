package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import kotlinx.android.synthetic.main.activity_bezier.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 贝塞尔曲线演示
 * @author BevisWang
 * @date 2019/9/11 10:35
 */
class BezierActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bezier)

        bindData()
    }

    private fun bindData() {
        bindToolbar("贝塞尔曲线演示")
        btn_bezier_start.onClick { bv_bezier.startAnimator() }
        btn_bezier_reset.onClick { bv_bezier.reset() }
    }
}
