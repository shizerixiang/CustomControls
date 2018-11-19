package com.beviswang.customcontrols

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.beviswang.customcontrols.adapter.ZoomTabViewPagerAdapter
import com.beviswang.customcontrols.fragment.AFragment
import com.beviswang.customcontrols.util.ViewHelper
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity
 * @author BevisWang
 * @date 2018/11/14 16:14
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val carMap = HashMap<String, Float>()
        carMap["捷达"] = 0.4f
        carMap["布加迪"] = 0.08f
        carMap["丰田"] = 0.07f
        carMap["奥迪"] = 0.05f
        carMap["宝马"] = 0.3f
        carMap["奔驰"] = 0.1f
        mPieChart.setData(carMap)
    }

    override fun onResume() {
        super.onResume()
        flipping.startAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
