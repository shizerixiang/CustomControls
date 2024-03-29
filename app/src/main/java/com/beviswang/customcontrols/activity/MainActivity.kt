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
import com.beviswang.customcontrols.loge
import com.beviswang.customcontrols.tansform.GlideRoundTransform
import com.beviswang.customcontrols.widget.maskimage.MaskImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_main2.view.*
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
        mDataList = arrayOf(
            getDescModel(
                "仿小米运动控件",
                "View 绘制 \n手势控制",
                MiSportsViewActivity::class.java,
                imgPath = R.mipmap.img_xiaomi_sport_header
            ),
            getDescModel(
                "仿虾米 Tab 导航栏",
                "View 绘制 \n自定义控件 \n手势控制",
                XiamiMusicTabActivity::class.java,
                imgPath = R.mipmap.img_xiami_nav
            ),
            getDescModel(
                "可控饼状图",
                "View 绘制 \n手势控制",
                PieChartActivity::class.java,
                imgPath = R.mipmap.img_pie_chart
            ),
            getDescModel(
                "仿红板报动画",
                "View 绘制",
                FlipBoardActivity::class.java,
                imgPath = R.mipmap.img_flipboard
            ),
            getDescModel(
                "飞行的火箭",
                "View 绘制",
                RocketFlyActivity::class.java,
                imgPath = R.mipmap.img_rocket_fly
            ),
            getDescModel(
                "混合图形画板",
                "View 绘制",
                MixedBoardActivity::class.java,
                imgPath = R.mipmap.img_mixed_board
            ),
            getDescModel(
                "贝塞尔曲线演示",
                "View 绘制",
                BezierActivity::class.java,
                imgPath = R.mipmap.img_bezier
            ),
            getDescModel(
                "音频频谱演示",
                "View 绘制",
                SpectrumActivity::class.java,
                imgPath = R.mipmap.img_spectrum
            ),
            getDescModel(
                "触摸控件演示",
                "View 绘制 \n手势控制",
                TouchToolActivity::class.java,
                imgPath = R.mipmap.img_touch_progress
            ),
            getDescModel(
                "拖影绘制",
                "View 绘制",
                SmearActivity::class.java,
                imgPath = R.mipmap.img_touch_progress
            ),
            getDescModel(
                "烟花绘制",
                "View 绘制",
                FireworksActivity::class.java,
                imgPath = R.mipmap.img_touch_progress
            ),
            getDescModel(
                "控件演示",
                "自定义控件",
                ControllerActivity::class.java,
                imgPath = R.mipmap.ic_rocket
            )
        )
        mLayoutManager = LinearLayoutManager(baseContext, RecyclerView.VERTICAL, false)
        mAdapter = MainAdapter(baseContext, mDataList)

        cel_main.onClick { }
        iv_tool_bar_menu.onClick {
            if (cel_main.visibility == View.VISIBLE) {
                cel_main.loadFinished {
                    cel_main.visibility = View.GONE
                }
                miv_main_menu.close()
                cel_main.hide()
            } else {
                cel_main.visibility = View.VISIBLE
                cel_main.loadFinished {
                    miv_main_menu.open()
                }
                cel_main.show()
            }
        }
    }

    private fun getDescModel(
        title: String, content: String, clazz: Class<*>, difficulty: String = "Normal",
        type: String = "View", imgPath: Int = -1
    ) = ViewDescModel(
        imgPath = imgPath, title = title, content = content,
        difficulty = difficulty, type = type, clazz = clazz
    )

    private fun bindViews() {
        rv_main.layoutManager = mLayoutManager
        rv_main.adapter = mAdapter
    }

    class MainAdapter(private var context: Context, private var data: Array<ViewDescModel>) :
        RecyclerView.Adapter<MainVH>() {
        private var mScrollerListener: RecyclerView.OnScrollListener? = null
        private var mRv: RecyclerView? = null
        private var mScrollOffset: Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainVH = MainVH(
            LayoutInflater.from(context)
                .inflate(R.layout.item_main2, parent, false)
        )

        override fun getItemCount() = data.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MainVH, position: Int) {
            val item = data[holder.adapterPosition]
            holder.img.reverse(position % 2 == 0)
            if (item.imgPath != -1) Glide.with(context).asBitmap().load(item.imgPath).centerCrop()
                .transform(GlideRoundTransform(0)).into(holder.img)
            holder.title.text = item.title
            holder.content.text = item.content
            holder.label.text = "${item.difficulty} | ${item.type}"
            holder.itemView.onClick {
                context.startActivity(
                    Intent(context, item.clazz).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                )
            }


            if (mRv?.layoutManager !is LinearLayoutManager) return
            val lm: LinearLayoutManager = mRv?.layoutManager as LinearLayoutManager
            val top  = lm.findFirstVisibleItemPosition()
            val bottom = lm.findLastVisibleItemPosition()
            if (holder.itemView.height != 0) {
                if (position in top..bottom) { // 均是显示的 Item

                }
                var scroller = mScrollOffset % holder.itemView.height // 最顶部 Item 偏移量
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            mRv = recyclerView
            mScrollerListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    mScrollOffset = recyclerView.computeVerticalScrollOffset()
                    loge("offset=$mScrollOffset   dx=$dx   dy=$dy")
                }
            }
            recyclerView.addOnScrollListener(mScrollerListener ?: return)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            mRv = recyclerView
            recyclerView.removeOnScrollListener(mScrollerListener ?: return)
        }
    }

    class MainVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: MaskImageView = itemView.iv_item_main
        var title: TextView = itemView.tv_item_main_title
        var content: TextView = itemView.tv_item_main_content
        var label: TextView = itemView.tv_item_main_label
    }

    class ViewDescModel(
        var imgPath: Int = -1, var title: String = "", var content: String = "",
        var difficulty: String = "", var type: String = "", var clazz: Class<*>
    )
}
