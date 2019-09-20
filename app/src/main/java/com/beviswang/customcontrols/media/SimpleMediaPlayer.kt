package com.beviswang.customcontrols.media

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import com.beviswang.customcontrols.loge
import com.beviswang.customcontrols.logi
import com.beviswang.customcontrols.source.model.MusicModel
import java.io.IOException

/**
 * 简易媒体播放器
 * @author BevisWang
 * @date 2019/9/19 14:58
 */
class SimpleMediaPlayer(var mPlayList: ArrayList<MusicModel> = ArrayList())
    : MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, Visualizer.OnDataCaptureListener {
    private var mLoopModel: Int = LOOP_MODEL_LOOP_LIST
    private var mMediaPlayer: MediaPlayer? = null
    private var mVisualizer: Visualizer? = null
    private var mIsOpenVisualizer: Boolean = false
    private var mPlayListener: (MusicModel) -> Unit = {}
    private var mFftListener: (Visualizer?, ByteArray?, Int) -> Unit = { _, _, _ -> }
    private var mWaveListener: (Visualizer?, ByteArray?, Int) -> Unit = { _, _, _ -> }
    private var mCurIndex: Int = 0

    init {
        initMediaPlayer()
        initVisualizer()
    }

    private fun initMediaPlayer() {
        mMediaPlayer?.reset()
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC) // 音频流类型
        mMediaPlayer?.setVolume(1.0f, 1.0f) // 左右音频播放音量
        mMediaPlayer?.setOnCompletionListener(this)
        mMediaPlayer?.setOnErrorListener(this)
    }

    private fun initVisualizer() {
        if (!mIsOpenVisualizer) return
        mVisualizer = Visualizer(mMediaPlayer?.audioSessionId ?: return)
        mVisualizer?.captureSize = Visualizer.getCaptureSizeRange()[1]
        mVisualizer?.setDataCaptureListener(this, getMs2MHz(80), false, true)
    }

    private fun getMs2MHz(ms: Int): Int {
        return 1000000 / ms
    }

    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
        mWaveListener(visualizer, waveform, samplingRate)
    }

    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
        mFftListener(visualizer, fft, samplingRate)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        logi("播放结束")
        when (mLoopModel) {
            LOOP_MODEL_LOOP_ONE -> play()
            LOOP_MODEL_LOOP_LIST -> skipToNext()
            else -> return
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        loge("播放错误：errorType=$what   errorCode=$extra")
        mVisualizer?.enabled = false
        return true
    }

    fun play(musicModel: MusicModel) {
        if (mPlayList.contains(musicModel)) {
            mCurIndex = mPlayList.indexOf(musicModel)
            play()
            return
        }
        mPlayList.add(musicModel)
        mCurIndex = mPlayList.lastIndex
        play()
    }

    /** 开始播放歌曲 */
    fun play() {
        if (mIsOpenVisualizer && mVisualizer == null) initVisualizer()
        val musicModel = mPlayList[mCurIndex]
        try {
            mMediaPlayer?.reset()
            mMediaPlayer?.setDataSource(musicModel.url)
            mMediaPlayer?.prepare()
            mVisualizer?.enabled = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mMediaPlayer?.start()
        mPlayListener(musicModel)
    }

    /** 暂停播放 */
    fun pause() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.pause()
        }
    }

    /** 跳转到歌曲的指定时间 */
    fun seekTo(pos: Long) {
        mMediaPlayer?.seekTo(pos.toInt())
    }

    /** 播放上一首歌曲 */
    fun skipToPrevious() {
        mCurIndex--
        if (mCurIndex < 0) {
            mCurIndex = mPlayList.size - 1
        }
        stopPlayer()
        play()
    }

    /** 播放下一首歌曲 */
    fun skipToNext() {
        mCurIndex++
        if (mCurIndex > mPlayList.size - 1) {
            mCurIndex = 0
        }
        stopPlayer()
        play()
    }

    /** 跳转到播放列表的指定位置 */
    fun skipToQueueItem(index: Int) {
        if (index < mPlayList.size && index >= 0) {
            mCurIndex = index
        }
        stopPlayer()
        play()
    }

    /** 停止播放 */
    fun stop() {
        stopPlayer()
        mMediaPlayer!!.release()
        mVisualizer?.enabled = false
    }

    /** 停止播放器 */
    private fun stopPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.stop()
        }
    }

    /** 获取当前播放位置 */
    fun getIndex() = mCurIndex

    /** 开启频谱数据监听 */
    fun openVisualizer() {
        mIsOpenVisualizer = true
    }

    /** 关闭频谱数据监听 */
    fun closeVisualizer() {
        mIsOpenVisualizer = false
    }

    /** fft 数据监听 */
    fun addFftListener(listener: (Visualizer?, ByteArray?, Int) -> Unit) {
        mFftListener = listener
    }

    /** wave 数据监听 */
    fun addWaveListener(listener: (Visualizer?, ByteArray?, Int) -> Unit) {
        mWaveListener = listener
    }

    /** 监听歌曲切换 */
    fun addOnPlayChanged(listener: (MusicModel) -> Unit) {
        mPlayListener = listener
    }

    companion object {
        const val LOOP_MODEL_LOOP_ONE = 0x10
        const val LOOP_MODEL_SIMGLE = 0x11
        const val LOOP_MODEL_LOOP_LIST = 0x12
    }
}