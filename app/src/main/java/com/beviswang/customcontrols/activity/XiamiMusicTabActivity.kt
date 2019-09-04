package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.util.ViewHelper
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.adapter.ZoomTabViewPagerAdapter
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.fragment.AFragment
import kotlinx.android.synthetic.main.activity_xiami_music_tab.*

/**
 * 仿虾米音乐导航 Tab
 * @author BevisWang
 * @date 2019/9/4 15:02
 */
class XiamiMusicTabActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xiami_music_tab)

        bindData()
    }

    private fun bindData() {
        bindToolbar("仿虾米音乐导航")
        ztl_xiami_music.setTabTextSize(ViewHelper.sp2px(baseContext, 14f))
        ztl_xiami_music.setItems(arrayOf("乐库", "推荐", "趴间", "看点"))
        ztl_xiami_music.setItemPadding(ViewHelper.dip2px(baseContext, 20f).toInt())
        vp_xiami_music.adapter = ZoomTabViewPagerAdapter(arrayOf(
                AFragment.newInstance("乐库", "乐库"),
                AFragment.newInstance("推荐", "推荐"),
                AFragment.newInstance("趴间", "趴间"),
                AFragment.newInstance("看点", "看点")), supportFragmentManager)
        ztl_xiami_music.setupWithViewPager(vp_xiami_music)
    }
}
