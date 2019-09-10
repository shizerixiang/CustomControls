package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar

/**
 * 混合图形绘制画板
 * @author BevisWang
 * @date 2019/9/10 10:53
 */
class MixedBoardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mixed_board)

        bindData()
    }

    private fun bindData() {
        bindToolbar("混合图形")
    }
}
