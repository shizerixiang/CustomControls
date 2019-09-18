package com.beviswang.customcontrols.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
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
import com.beviswang.customcontrols.bindToolbar
import com.beviswang.customcontrols.source.LocalMusicSource
import com.beviswang.customcontrols.source.model.MusicModel
import com.beviswang.customcontrols.source.utils.ConverterHelper
import com.beviswang.customcontrols.source.utils.ConverterHelper.ACCURATE_TO_MINUTE
import com.beviswang.customcontrols.tansform.GlideRoundTransform
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_spectrum.*
import kotlinx.android.synthetic.main.item_main.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import java.io.IOException

/**
 * 频谱动画演示
 * @author BevisWang
 * @date 2019/9/17 15:16
 */
class SpectrumActivity : BaseActivity(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,Visualizer.OnDataCaptureListener {
    private var mMediaPlayer: MediaPlayer? = null
    private var mVisualizer: Visualizer? = null
    private var mDataList: ArrayList<MusicModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spectrum)
        bindToolbar("频谱动画演示")

        requestSplashPermissions()
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
        initMediaPlayer()
        initVisualizer()
        initMusicList()
    }

    private fun initVisualizer() {
        mVisualizer = Visualizer(mMediaPlayer?.audioSessionId ?: return)
        mVisualizer?.captureSize = Visualizer.getCaptureSizeRange()[1]
        sv_spectrum1.setSoftAnimator()
        sv_spectrum2.setRushAnimator()
        mVisualizer?.setDataCaptureListener(this, 6250, false, true)
    }

    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) { }

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        sv_spectrum1.onFftDataCapture(visualizer,fft,samplingRate)
        sv_spectrum2.onFftDataCapture(visualizer,fft,samplingRate)
    }

    /** 初始化播放器 */
    private fun initMediaPlayer() {
        mMediaPlayer?.reset()
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC) // 音频流类型
        mMediaPlayer?.setVolume(1.0f, 1.0f) // 左右音频播放音量
        mMediaPlayer?.setOnCompletionListener(this)
        mMediaPlayer?.setOnErrorListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        showMsg("播放结束")
        mVisualizer?.enabled = false
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        showMsg("播放错误：errorType=$what   errorCode=$extra")
        mVisualizer?.enabled = false
        return true
    }

    private fun playMusic(url: String) {
        if (mMediaPlayer == null || url.isEmpty()) return
        try {
            mVisualizer?.enabled = false
            mMediaPlayer?.reset()
            mMediaPlayer?.setDataSource(url)
            mMediaPlayer?.prepare()
            mMediaPlayer?.start()
            mVisualizer?.enabled = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopMusic() {
        if (mMediaPlayer == null) return
        try {
            mVisualizer?.enabled = false
            mMediaPlayer?.pause()
            mMediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 初始化音乐文件列表 */
    private fun initMusicList() {
        rv_spectrum.layoutManager = LinearLayoutManager(baseContext, RecyclerView.VERTICAL, false)
        val adapter = MusicAdapter(baseContext, mDataList)
        adapter.setPlayListener { playMusic(it) }
        rv_spectrum.adapter = adapter
        doAsync {
            LocalMusicSource(this@SpectrumActivity).iterators()?.forEach { mDataList.add(it) }
            uiThread { adapter.notifyDataSetChanged() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
    }

    class MusicAdapter(private var context: Context, private var data: List<MusicModel>) : RecyclerView.Adapter<MusicVH>() {
        private var mPlayListener: (String) -> Unit = {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicVH = MusicVH(LayoutInflater.from(context)
                .inflate(R.layout.item_main, parent, false))

        override fun getItemCount() = data.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MusicVH, position: Int) {
            val item = data[holder.adapterPosition]
            Glide.with(context).asBitmap().load(item.imageUrl).centerCrop()
                    .transform(GlideRoundTransform(8)).into(holder.img)
            holder.title.text = item.title
            holder.content.text = item.artist
            holder.label.text = ConverterHelper.getConvertedTime(item.duration, ACCURATE_TO_MINUTE)
            holder.itemView.onClick { mPlayListener(item.url) }
        }

        fun setPlayListener(listener: (String) -> Unit) {
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
