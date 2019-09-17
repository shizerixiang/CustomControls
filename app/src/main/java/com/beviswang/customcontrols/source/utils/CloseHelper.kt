package com.beviswang.customcontrols.source.utils

import java.io.Closeable
import java.io.IOException

/**
 * 关闭工具类
 * Created by shize on 2017/3/20.
 */

object CloseHelper {

    /**
     * 关闭Closeable对象
     * @param closeable 关闭Closeable对象
     */
    fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}
