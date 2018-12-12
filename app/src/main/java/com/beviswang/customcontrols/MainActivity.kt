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
        // TODO 转动卡顿时，请把仿小米运动首页的动画关掉，谢谢！！！
        val carMap = HashMap<String, Float>()
        carMap["捷达"] = 0.12f
        carMap["标志"] = 0.13f
        carMap["雪铁龙"] = 0.1f
        carMap["保时捷"] = 0.05f
        carMap["布加迪"] = 0.0549f
        carMap["丰田"] = 0.0451f
        carMap["奥迪"] = 0.15f
        carMap["宝马"] = 0.05f
        carMap["大众"] = 0.14f
        carMap["奔驰"] = 0.16f
        mPieChart.setData(carMap)
        // TODO 红板报动画另类仿制
        mFlipBoardView.setOnClickListener {
            mFlipBoardView.startAnimator()
        }
    }

    override fun onResume() {
        super.onResume()
        flipping.startLoadedAnimation()
    }

    override fun onPause() {
        super.onPause()
        flipping.pauseLoadedAnimation()
        mFlipBoardView.pauseAnimator()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
