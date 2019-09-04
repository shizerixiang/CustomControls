package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import kotlinx.android.synthetic.main.activity_flipboard.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 红板报动画模仿
 * @author BevisWang
 * @date 2019/9/4 17:29
 */
class FlipBoardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flipboard)

        bindData()
    }

    private fun bindData() {
        bindToolbar("仿红板报动画")
        fv_flip_board.onClick {
            fv_flip_board.startAnimator()
        }
    }

    override fun onPause() {
        super.onPause()
        fv_flip_board.pauseAnimator()
    }
}
