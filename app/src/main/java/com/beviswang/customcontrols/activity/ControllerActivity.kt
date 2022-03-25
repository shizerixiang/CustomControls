package com.beviswang.customcontrols.activity

import android.os.Bundle
import android.widget.ImageView
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.widget.adnormal.AbnormalTab
import kotlinx.android.synthetic.main.activity_controller.*

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
//        otv_controller.setTabImageIcons(
//            arrayOf(
//                R.mipmap.ic_tab_homepage_selected,
//                R.mipmap.ic_tab_my_selected,
//                R.mipmap.ic_tab_task_selected,
//                R.mipmap.ic_tab_world_selected
//            )
//        )
//        otv_controller.setTabDrawableIcons(
//            arrayOf(
//                R.drawable.ic_tab_homepage_selected,
//                R.drawable.ic_tab_world_selected,
//                R.drawable.ic_tab_task_selected,
//                R.drawable.ic_tab_my_selected
//            )
//        )
        otv_controller.setTabs(
            arrayOf(
                otv_controller.newTab(R.drawable.ic_tab_homepage_selected),
                otv_controller.newTab(R.drawable.ic_tab_world_selected),
                otv_controller.newTab(R.drawable.ic_tab_task_selected),
                otv_controller.newTab(R.drawable.ic_tab_my_selected)
            )
        )
    }
}
