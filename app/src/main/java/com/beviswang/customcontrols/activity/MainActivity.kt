package com.beviswang.customcontrols.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_main.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 自定义控件引导页
 * @author BevisWang
 * @date 2019/8/30 10:21
 */
class MainActivity : BaseActivity() {
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mDataList: Array<ViewDescModel>
    private lateinit var mAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initData()
        bindViews()
    }

    private fun initData() {
        mDataList = arrayOf(getDescModel("仿小米运动控件", "View 绘制", MiSportsViewActivity::class.java),
                getDescModel("仿虾米 Tab 导航栏", "View 绘制 | 自定义控件 | 手势控制", XiamiMusicTabActivity::class.java),
                getDescModel("可控饼状图", "View 绘制 | 手势控制", PieChartActivity::class.java),
                getDescModel("仿红板报动画", "View 绘制", FlipBoardActivity::class.java),
                getDescModel("飞行的火箭", "View 绘制", RocketFlyActivity::class.java),
                getDescModel("混合图形画板", "View 绘制", MixedBoardActivity::class.java))
        mLayoutManager = LinearLayoutManager(baseContext, RecyclerView.VERTICAL, false)
        mAdapter = MainAdapter(baseContext, mDataList)
    }

    private fun getDescModel(title: String, content: String, clazz: Class<*>, difficulty: String = "普通",
                             type: String = "View") = ViewDescModel(title = title, content = content,
            difficulty = difficulty, type = type, clazz = clazz)

    private fun bindViews() {
        rv_main.layoutManager = mLayoutManager
        rv_main.adapter = mAdapter
    }

    class MainAdapter(private var context: Context, private var data: Array<ViewDescModel>) : RecyclerView.Adapter<MainVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainVH = MainVH(LayoutInflater.from(context)
                .inflate(R.layout.item_main, parent, false))

        override fun getItemCount() = data.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MainVH, position: Int) {
            val item = data[holder.adapterPosition]
            holder.title.text = item.title
            holder.content.text = item.content
            holder.label.text = "${item.difficulty} | ${item.type}"
            holder.itemView.onClick { context.startActivity(Intent(context, item.clazz).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
    }

    class MainVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: ImageView = itemView.iv_item_main
        var title: TextView = itemView.tv_item_main_title
        var content: TextView = itemView.tv_item_main_content
        var label: TextView = itemView.tv_item_main_label
    }

    class ViewDescModel(var imgPath: String = "", var title: String = "", var content: String = "",
                        var difficulty: String = "", var type: String = "", var clazz: Class<*>)
}
