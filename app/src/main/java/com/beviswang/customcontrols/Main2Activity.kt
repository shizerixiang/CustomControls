package com.beviswang.customcontrols

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.constraintlayout.widget.ConstraintLayout
import com.beviswang.customcontrols.widget.KeyboardHeightProvider
import kotlinx.android.synthetic.main.activity_main2.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class Main2Activity : AppCompatActivity(), KeyboardHeightProvider.KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        mKeyboardHeightProvider = KeyboardHeightProvider(this)

        mWeatherLayout.setOnClickListener {
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