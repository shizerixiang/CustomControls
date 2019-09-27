package com.beviswang.customcontrols.activity

import android.os.Bundle
import android.view.View
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.bindToolbarWithMenu
import kotlinx.android.synthetic.main.activity_fireworks.*

/**
 * 烟花绘制
 * @author BevisWang
 * @date 2019/9/26 11:37
 */
class FireworksActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fireworks)

//        bindToolbar("烟花绘制")
        bindToolbarWithMenu("烟花绘制", menuVisibility = View.VISIBLE, menuView = layoutInflater.inflate(R.layout.layout_pop_menu,null))
    }
}
