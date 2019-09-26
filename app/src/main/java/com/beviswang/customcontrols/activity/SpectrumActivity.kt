package com.beviswang.customcontrols.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beviswang.customcontrols.BaseActivity
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.dialog.HintDialog
import com.beviswang.customcontrols.media.SimpleMediaPlayer
import com.beviswang.customcontrols.source.LocalMusicSource
import com.beviswang.customcontrols.source.model.MusicModel
import com.beviswang.customcontrols.source.utils.ConverterHelper
import com.beviswang.customcontrols.source.utils.ConverterHelper.ACCURATE_TO_MINUTE
import com.beviswang.customcontrols.tansform.GlideRoundTransform
import com.beviswang.customcontrols.util.BitmapHelper
import com.beviswang.customcontrols.util.TransitionHelper
import com.beviswang.customcontrols.widget.TouchProgressView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import kotlinx.android.synthetic.main.activity_spectrum.*
import kotlinx.android.synthetic.main.item_main.view.*
import kotlinx.android.synthetic.main.layout_tool_bar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread

/**
 * 频谱动画演示
 * @author BevisWang
 * @date 2019/9/17 15:16
 */
class SpectrumActivity : BaseActivity() {
    private var mMediaPlayer: SimpleMediaPlayer? = null
    private var mDataList: ArrayList<MusicModel> = ArrayList()

    private var mTbColor: Int = 0
    private var mLastBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spectrum)
        bindToolbar("频谱动画演示", menuVisibility = View.VISIBLE, menuListener = { showHintDialog() })

        requestSplashPermissions()
        mTbColor = ContextCompat.getColor(this@SpectrumActivity, R.color.colorPrimary)
    }

    private fun showHintDialog() {
        HintDialog.Builder(this@SpectrumActivity).show()
    }

    private fun requestSplashPermissions() = requestGPHPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO), 0)

    override fun onRequestPermissionsDenied(requestCode: Int, permissions: List<String>) {
        requestSplashPermissions()
    }

    override fun onRequestPermissionsGranted(requestCode: Int) {
        bindData()
    }

    override fun onRequestPermissionsReject(requestCode: Int, permissions: List<String>) {
        finish()
    }

    private fun bindData() {
        sv_spectrum1.setRushAnimator()
//        sv_spectrum1.setSoftAnimator()
        mMediaPlayer = SimpleMediaPlayer()
        mMediaPlayer?.openVisualizer()
        mMediaPlayer?.addOnPlayChanged { changeScene(it) }
        mMediaPlayer?.addFftListener { v, fft, i ->
            sv_spectrum1.onFftDataCapture(v, fft, i)
        }
        mMediaPlayer?.addSeekListener { tpv_spectrum.setProgress(it) }
        tpv_spectrum.addSeekBarChangedListener(object : TouchProgressView.OnProgressChangeListener {
            override fun onProgressChanged(tpv: TouchProgressView, progress: Float, fromUser: Boolean) {}
            override fun onStartTouch(tpv: TouchProgressView) {}
            override fun onStopTouch(tpv: TouchProgressView) {
                mMediaPlayer?.seekTo(tpv.getProgress())
            }
        })
        initMusicList()
    }

    private fun changeScene(m: MusicModel) {
        val url = m.url
        tv_tool_bar_title.text = m.title
        val bt = BitmapHelper.getBitmapFromMedia(url) ?: return
        Glide.with(baseContext).asBitmap().load(bt)
                .placeholder(BitmapDrawable(resources, mLastBitmap))
                .transition(BitmapTransitionOptions.withCrossFade())
                .centerCrop().into(iv_spectrum_bg)
        BitmapHelper.getPaletteColor(this@SpectrumActivity, bt, listener = { color ->
            TransitionHelper.changeBgColorAnimator(mTbColor, color, cl_tool_bar)
            tpv_spectrum.setProgressColor(color)
            mTbColor = color
        })
        mLastBitmap = bt
    }

    /** 初始化音乐文件列表 */
    private fun initMusicList() {
        rv_spectrum.layoutManager = LinearLayoutManager(baseContext, RecyclerView.VERTICAL, false)
        val adapter = MusicAdapter(baseContext, mDataList)
        adapter.setPlayListener { mMediaPlayer?.play(it) }
        rv_spectrum.adapter = adapter
        doAsync {
            LocalMusicSource(this@SpectrumActivity).iterators()?.forEach { mDataList.add(it) }
            uiThread { adapter.notifyDataSetChanged() }
        }
    }

    override fun onResume() {
        super.onResume()
        sv_spectrum1.resume()
    }

    override fun onPause() {
        super.onPause()
        sv_spectrum1.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.stop()
    }

    class MusicAdapter(private var context: Context, private var data: List<MusicModel>) : RecyclerView.Adapter<MusicVH>() {
        private var mPlayListener: (MusicModel) -> Unit = {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicVH = MusicVH(LayoutInflater.from(context)
                .inflate(R.layout.item_main, parent, false))

        override fun getItemCount() = data.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MusicVH, position: Int) {
            val item = data[holder.adapterPosition]
            Glide.with(context).asBitmap().load(BitmapHelper.getBitmapFromMedia(item.url, 280)).centerCrop()
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .transform(GlideRoundTransform(8)).into(holder.img)
            holder.title.text = item.title
            holder.content.text = item.artist
            holder.label.text = ConverterHelper.getConvertedTime(item.duration, ACCURATE_TO_MINUTE)
            holder.itemView.onClick { mPlayListener(item) }
        }

        fun setPlayListener(listener: (MusicModel) -> Unit) {
            mPlayListener = listener
        }
    }

    class MusicVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: ImageView = itemView.iv_item_main
        var title: TextView = itemView.tv_item_main_title
        var content: TextView = itemView.tv_item_main_content
        var label: TextView = itemView.tv_item_main_label
    }
}
