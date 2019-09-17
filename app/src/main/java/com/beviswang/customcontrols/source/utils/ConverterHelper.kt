package com.beviswang.customcontrols.source.utils

import android.content.Context

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

/**
 * 显示字符串的转换
 * Created by shize on 2017/3/1.
 */
object ConverterHelper {
    var CACHE_DIR:String = ""

    /**
     * 精确时间显示位置
     */
    val ACCURATE_TO_SECOND = 0x10 // 精确到秒
    val ACCURATE_TO_MINUTE = 0x20 // 精确到分
    val ACCURATE_TO_HOUR = 0x30 // 精确到小时

    private val BYTE = 1024f

    val EX_NAME = ".webp" // 文件扩展名
    private val BIT_EX = " kbit/s"

    // 播放音乐的循环模式
    val PLAY_MODE_ONE_LOOP = 0x123 // 单曲循环
    val PLAY_MODE_LIST_LOOP = 0x124 // 列表循环
    val PLAY_MODE_RANDOM_LOOP = 0x125 // 随机循环

    /**
     * 从dp转换为px

     * @param context  上下文
     * *
     * @param dipValue dp值
     * *
     * @return px值
     */
    fun dipToPx(context: Context, dipValue: Float): Float {
        // 获取比例
        val scale = context.resources.displayMetrics.density
        return dipValue * scale + 0.5f
    }

    /**
     * 从px转换为dp

     * @param context 上下文
     * *
     * @param pxValue px值
     * *
     * @return dp值
     */
    fun pxToDip(context: Context, pxValue: Float): Float {
        // 获取比例
        val scale = context.resources.displayMetrics.density
        return pxValue / scale + 0.5f
    }

    /**
     * 将size转为符合的字符串表示出来

     * @param size 大小
     * *
     * @return 合适的字符串
     */
    fun getConvertedSize(size: Double): String {
        var size = size
        val df = DecimalFormat(".00")
        val base = (Math.log(size) / Math.log(BYTE.toDouble())).toInt()
        for (i in 0 until base) {
            size /= BYTE.toDouble()
        }
        return when (base) {
            1 -> df.format(size) + "KB"
            2 -> df.format(size) + "MB"
            3 -> df.format(size) + "GB"
            else -> df.format(size) + "Byte"
        }
    }

    /**
     * 将毫秒转换为显示时间字符串

     * @param timeValue   总时间
     * *
     * @param displayMode One of [.ACCURATE_TO_SECOND],[.ACCURATE_TO_MINUTE],
     * *                    [.ACCURATE_TO_HOUR]
     * *
     * @return 显示时间字符串
     */
    fun getConvertedTime(timeValue: Long, displayMode: Int): String {
        // 将毫秒转化为秒
        val durationS = (timeValue / 1000).toInt()
        return when (displayMode) {
            ACCURATE_TO_MINUTE -> getTimeString(durationS / 60) + ":" + getTimeString(durationS % 60)
            ACCURATE_TO_HOUR -> getTimeString(durationS / 3600) + ":" + getTimeString(durationS / 60 % 60) +
                    ":" + getTimeString(durationS % 60)
            else -> getTimeString(durationS)
        }
    }

    /**
     * 将时间转化为字符串

     * @param time 时间
     * *
     * @return String
     */
    fun getTimeString(time: Int): String {
        return if (time < 10) "0" + time else time.toString()
    }

    /**
     * 获取存放该地址的文件夹

     * @param url 地址
     * *
     * @return 文件夹地址
     */
    fun getFolderFromUrl(url: String): String {
        return url.substring(0, url.lastIndexOf('/'))
    }

    /**
     * @param folderUrl 文件夹地址
     * *
     * @return 返回文件夹名称
     */
    fun getFolderName(folderUrl: String): String {
        return folderUrl.substring(folderUrl.lastIndexOf('/'), folderUrl.length)
    }

    /**
     * 利用专辑名缓存图片

     * @param albumName 专辑名
     */
    fun getConvertedAlbumPath(albumName: String): String {
        return CACHE_DIR + getImageNameByAlbum(albumName)
    }

    /**
     * 通过音乐地址获取缓存图片名称
     */
    fun getImageNameByAlbum(album: String): String {
        return album.replace('/', '_') + EX_NAME
    }

    /**
     * 将规则字符串转换为list字符串集合

     * @param idText 规则字符串(以逗号隔开每个id字符串)
     * *
     * @return 字符串list
     */
    fun getMusicIdList(idText: String): List<String> {
        val strList = ArrayList<String>()
        var start = -1
        while (idText.lastIndexOf(",") != start && idText.lastIndexOf(",") > 0) {
            start++
            val last = idText.indexOf(",", start)
            val str = idText.substring(start, last)
            strList.add(str)
            start = last
        }
        if (idText.isNotEmpty()) {
            strList.add(idText.substring(start + 1, idText.length))
        }
        return strList
    }

    /**
     * 将字符串集合转换为单个字符串

     * @param musicIdList 字符串集合
     * *
     * @return 单个字符串
     */
    fun getMusicIdString(musicIdList: List<String>): String {
        var musicIdString = ""
        for (string in musicIdList) {
            musicIdString += string + ","
        }
        if (musicIdList.isEmpty()) {
            return musicIdString
        }
        return musicIdString.substring(0, musicIdString.length - 1)
    }

    /**
     * 转换字符串为bool
     */
    fun strToBoolean(string: String): Boolean {
        return string == "1"
    }

    val nowTime: String
        get() {
            val format = SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.CHINA)
            return format.format(Date())
        }

    /**
     * 切换循环模式

     * @param mode 模式 one of [.PLAY_MODE_ONE_LOOP], [.PLAY_MODE_LIST_LOOP],
     * *             [.PLAY_MODE_RANDOM_LOOP]
     * *
     * @return mode
     */
    fun getChangedMode(mode: Int): Int {
        var mode = mode
        when (mode) {
            PLAY_MODE_ONE_LOOP -> mode = PLAY_MODE_LIST_LOOP
            PLAY_MODE_LIST_LOOP -> mode = PLAY_MODE_RANDOM_LOOP
            PLAY_MODE_RANDOM_LOOP -> mode = PLAY_MODE_ONE_LOOP
        }
        return mode
    }

    /**
     * 将获取的week数字转换为中文星期
     */
    fun getWeekNum(num: Int): String = when (num) {
        1 -> "星期日"
        2 -> "星期一"
        3 -> "星期二"
        4 -> "星期三"
        5 -> "星期四"
        6 -> "星期五"
        7 -> "星期六"
        else -> "星期一"
    }

    /**
     * 转换bit率为可显示的字符串
     */
    fun getConvertedBit(bitStr: String): String {
        var bit = 0
        try {
            bit = Integer.parseInt(bitStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return (bit / 1000).toString() + BIT_EX
    }
}
