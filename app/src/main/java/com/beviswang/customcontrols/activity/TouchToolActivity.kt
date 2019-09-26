package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.widget.TouchProgressView
import kotlinx.android.synthetic.main.activity_touch_tool.*

/**
 * 触摸工具控件演示
 * @author BevisWang
 * @date 2019/9/21 11:50
 */
class TouchToolActivity : BaseActivity(), TouchProgressView.OnProgressChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_touch_tool)

        bindData()
    }

    private fun bindData() {
        bindToolbar("触摸控件演示")

        tpv_touch_tool.addSeekBarChangedListener(this)
    }

    override fun onProgressChanged(tpv: TouchProgressView, progress: Float, fromUser: Boolean) {
//        tv_touch_progress.text = (progress * 100).toInt().toString()
    }

    override fun onStartTouch(tpv: TouchProgressView) {
        tv_touch_progress.text = (tpv.getProgress() * 100).toInt().toString()
    }

    override fun onStopTouch(tpv: TouchProgressView) {
        tv_touch_progress.text = (tpv.getProgress() * 100).toInt().toString()
    }
}
