package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.fragment.AFragment
import com.beviswang.customcontrols.loge
import com.beviswang.customcontrols.util.displayFragment
import kotlinx.android.synthetic.main.activity_controller.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 自定义控件展示
 * @author BevisWang
 * @date 2019/9/28 16:31
 */
class ControllerActivity : BaseActivity() {

    private var mFragments: Array<AFragment> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        bindData()
    }

    private fun bindData() {
        bindToolbar("控件演示")
        mFragments = arrayOf(
            AFragment.newInstance("1","1"),
            AFragment.newInstance("2","2"),
            AFragment.newInstance("3","3"),
            AFragment.newInstance("4","4"),
            AFragment.newInstance("5","5")
        )
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
                otv_controller.newTab(R.drawable.ic_play),
                otv_controller.newTab(R.drawable.ic_tab_my_selected)
            )
        )
        supportFragmentManager.beginTransaction().apply {
            mFragments.forEach {
                add(R.id.fl_controller, it)
                hide(it)
            }
            show(mFragments[0])
        }.commit()
        otv_controller.setOnTabSelectedChanged {
            loge("当前选中的Tab：$it")
            supportFragmentManager.beginTransaction().apply {
                mFragments.forEachIndexed { index, aFragment ->
                    if (index == it) show(aFragment) else hide(aFragment)
                }
            }.commit()
        }
//        iv_controller.onClick {
//            displayFragment(
//                R.id.fl_controller,
//                AFragment::class.java,
//                iv_controller
//            )
//        }
    }
}
