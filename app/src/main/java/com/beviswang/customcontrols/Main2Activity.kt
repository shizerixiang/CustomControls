package com.beviswang.customcontrols

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.widget.RelativeLayout
import com.beviswang.customcontrols.util.KeyboardHeightProvider
import kotlinx.android.synthetic.main.activity_main2.*
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class Main2Activity : AppCompatActivity(), KeyboardHeightProvider.KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        mKeyboardHeightProvider = KeyboardHeightProvider(this)

        mWeatherLayout.setOnClickListener { it ->
            doAsync {
                (0..100).forEach { i ->
                    uiThread {
                        mWeatherLayout.setScale(i / 100f)
                    }
                    Thread.sleep(4)
                }
                (0..100).forEach { i ->
                    uiThread {
                        mWeatherLayout.setScale(1 - (i / 100f))
                    }
                    Thread.sleep(4)
                }
            }
        }
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
        val lp = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, height)
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        editInput.layoutParams = lp
    }
}