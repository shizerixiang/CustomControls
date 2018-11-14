package com.beviswang.customcontrols

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import com.beviswang.customcontrols.adapter.ZoomTabViewPagerAdapter
import com.beviswang.customcontrols.fragment.AFragment
import com.beviswang.customcontrols.util.KeyboardHeightProvider
import com.beviswang.customcontrols.util.ViewHelper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.centerHorizontally

/**
 * MainActivity
 * @author BevisWang
 * @date 2018/11/14 16:14
 */
class MainActivity : AppCompatActivity(), KeyboardHeightProvider.KeyboardHeightObserver {
    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mKeyboardHeightProvider = KeyboardHeightProvider(this)

        headerBar.setOnClickListener {
            waveView.startWave()
        }

        zoomTabLayout.setTabTextSize(ViewHelper.sp2px(this@MainActivity, 14f))
        zoomTabLayout.setItems(arrayOf("乐库", "推荐", "趴间", "看点"))
        zoomTabLayout.setItemPadding(ViewHelper.dip2px(this@MainActivity, 20f).toInt())

        viewPager.adapter = ZoomTabViewPagerAdapter(arrayOf(
                AFragment.newInstance("乐库", "乐库"),
                AFragment.newInstance("推荐", "推荐"),
                AFragment.newInstance("趴间", "趴间"),
                AFragment.newInstance("看点", "看点")), supportFragmentManager)
        zoomTabLayout.setupWithViewPager(viewPager)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Handler().post { mKeyboardHeightProvider?.start() }
    }

    override fun onResume() {
        super.onResume()
        mKeyboardHeightProvider?.setKeyboardHeightObserver(this)
        flipping.startAnimation()
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
