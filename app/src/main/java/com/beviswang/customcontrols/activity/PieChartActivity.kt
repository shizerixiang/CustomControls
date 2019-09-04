package com.beviswang.customcontrols.activity

import android.os.Bundle
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import kotlinx.android.synthetic.main.activity_pie_chart.*

/**
 * 原创饼状可控视图
 * @author BevisWang
 * @date 2019/9/4 15:14
 */
class PieChartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)

        bindData()
    }

    private fun bindData() {
        bindToolbar("可控饼状图")
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
        pcv_pie_chart.setData(carMap)
    }
}
