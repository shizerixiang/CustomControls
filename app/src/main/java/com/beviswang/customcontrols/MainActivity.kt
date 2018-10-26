package com.beviswang.customcontrols

import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import com.beviswang.customcontrols.util.KeyboardHeightProvider
import com.beviswang.customcontrols.util.ViewHelper
import com.beviswang.customcontrols.widget.TabView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), KeyboardHeightProvider.KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
//    private var mSelectedIndex = 2

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

        mTxt1.setSelectTextSize(ViewHelper.sp2px(this@MainActivity, 24f))
        zoomBBtn.onClick { open(mTxt1) }
        zoomSBtn.onClick { close(mTxt1) }

        mTxt1.onClick {
            mTxt1.startTabAnimation()
        }

        mTxt1.setText("推荐")
        mTxt1.setTextAlign(Paint.Align.LEFT)

        zoomTabLayout.setItems(arrayOf("乐库","推荐","趴间","看点"))
    }

    fun testTabView() {
//        zoomTab1.setText("乐库")
//        zoomTab1.onClick {
//            when (mSelectedIndex) {
//                1 -> {
//                }
//                2 -> {
//                    open(zoomTab1)
//                    close(zoomTab2)
//                }
//                3 -> {
//                    open(zoomTab1)
//                    close(zoomTab3)
//                }
//                4 -> {
//                    open(zoomTab1)
//                    close(zoomTab4)
//                }
//            }
//            mSelectedIndex = 1
//        }
//        zoomTab2.setText("推荐")
//        zoomTab2.onClick {
//            when (mSelectedIndex) {
//                1 -> {
//                    open(zoomTab2)
//                    close(zoomTab1)
//                }
//                2 -> {
//                }
//                3 -> {
//                    open(zoomTab2)
//                    close(zoomTab3)
//                }
//                4 -> {
//                    open(zoomTab2)
//                    close(zoomTab4)
//                }
//            }
//            mSelectedIndex = 2
//        }
//        zoomTab3.setText("趴间")
//        zoomTab3.onClick {
//            when (mSelectedIndex) {
//                1 -> {
//                    open(zoomTab3)
//                    close(zoomTab1)
//                }
//                2 -> {
//                    open(zoomTab3)
//                    close(zoomTab2)
//                }
//                3 -> {
//                }
//                4 -> {
//                    open(zoomTab3)
//                    close(zoomTab4)
//                }
//            }
//            mSelectedIndex = 3
//        }
//        zoomTab4.setText("看点")
//        zoomTab4.onClick {
//            when (mSelectedIndex) {
//                1 -> {
//                    open(zoomTab4)
//                    close(zoomTab1)
//                }
//                2 -> {
//                    open(zoomTab4)
//                    close(zoomTab2)
//                }
//                3 -> {
//                    open(zoomTab4)
//                    close(zoomTab3)
//                }
//                4 -> {
//                }
//            }
//            mSelectedIndex = 4
//        }
    }

    private fun open(tab: TabView) {
        doAsync {
            (0..50).forEach { it ->
                uiThread { _ ->
                    tab.setScrollScale(it / 50f)
                }
                Thread.sleep(8)
            }
            uiThread {
                tab.startTabAnimation()
            }
        }
    }

    private fun close(tab: TabView) {
        doAsync {
            (0..50).forEach { it ->
                uiThread { _ ->
                    tab.setScrollScale((50 - it) / 50f)
                }
                Thread.sleep(8)
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
//        open(zoomTab2)
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
