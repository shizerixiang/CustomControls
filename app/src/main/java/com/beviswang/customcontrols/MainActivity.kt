package com.beviswang.customcontrols

import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import com.beviswang.customcontrols.util.KeyboardHeightProvider
import com.beviswang.customcontrols.util.ViewHelper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.onClick

class MainActivity : AppCompatActivity(), KeyboardHeightProvider.KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mKeyboardHeightProvider = KeyboardHeightProvider(this)

        headerBar.onClick { _ ->
            waveView.startWave()
        }

        headerBar2.onClick { _ ->
            waveView2.startWave()
        }

        zoomBtn.onClick {
            mTxt1.setTextSize(ViewHelper.sp2px(this@MainActivity,24f))
        }

        mTxt1.onClick {
            mTxt1.setTextSize(ViewHelper.sp2px(this@MainActivity,14f))
        }

        mTxt1.setText("快速扩大gdF4")
        mTxt1.setTextAlign(Paint.Align.LEFT)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Handler().post { mKeyboardHeightProvider?.start() }
    }

    override fun onResume() {
        super.onResume()
        mKeyboardHeightProvider?.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        mKeyboardHeightProvider?.setKeyboardHeightObserver(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mKeyboardHeightProvider?.close()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, height)
        lp.alignParentBottom()
        lp.centerHorizontally()
        editInput.layoutParams = lp
    }
}
