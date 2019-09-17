package com.beviswang.customcontrols.source

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.beviswang.customcontrols.source.model.MusicModel
import com.beviswang.customcontrols.source.utils.CloseHelper
import com.beviswang.customcontrols.source.utils.ConverterHelper

import java.util.ArrayList

/**
 * 本地数据源
 * Created by shize on 2017/3/28.
 */

class LocalMusicSource(private val mContext: Context) : MusicSource {

    override fun iterators(): Iterator<MusicModel>? {
        return searchMusicInfo(mContext).iterator()
    }

    override fun getContext(): Context? {
        return mContext
    }

    /**
     * 在系统媒体库获取音乐文件信息

     * @return 完整音乐文件信息
     */
    private fun searchMusicInfo(context: Context): List<MusicModel> {
        val musicBuilders = ArrayList<MusicModel.MusicBuilder>()
        val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER) ?: error("SearchMusic: 未查找到数据库数据")
        while (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)) < MIN_DURATION) {
                continue
            }
            musicBuilders.add(MusicModel.MusicBuilder()
                    .setId(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)))
                    .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
                    .setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                    .setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                    .setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)).toLong())
                    .setSize(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)).toLong())
                    .setUrl(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)))
                    .setDate(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))))
            Log.i(TAG, "searchMusicInfo: 在媒体库中找到了一个文件！！！ title=" + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
        }
        CloseHelper.closeQuietly(cursor)
        Log.i(TAG, "searchMusicInfo: size=" + musicBuilders.size)
        return getExtendMusicInfo(musicBuilders)
    }

    /**
     * 获取额外的媒体文件信息

     * @param builders 音乐创造者集合
     * *
     * @return list
     */
    private fun getExtendMusicInfo(builders: List<MusicModel.MusicBuilder>): List<MusicModel> {
        val modelList = ArrayList<MusicModel>()
        val retriever = MediaMetadataRetriever()
        for (musicBuilder in builders) {
            try {
                retriever.setDataSource(musicBuilder.url)
                modelList.add(musicBuilder
                        .setBit(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE))
                        .setGenre(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE))
                        .setImageUrl(ConverterHelper.getConvertedAlbumPath(musicBuilder.album)).build())
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        retriever.release()
        return modelList
    }

    companion object {
        private const val TAG:String = "LocalMusicSource"
        private const val MIN_DURATION = 60000
    }

}
