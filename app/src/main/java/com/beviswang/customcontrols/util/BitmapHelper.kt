package com.beviswang.customcontrols.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.beviswang.customcontrols.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


object BitmapHelper {
    /**
     * 根据给定的宽和高进行拉伸
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    fun scaleBitmap(origin: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        if (origin == null) {
            return null
        }
        val height = origin.height
        val width = origin.width
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)// 使用后乘
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (!origin.isRecycled) {
            origin.recycle()
        }
        return newBM
    }

    /**
     * 按比例缩放图片
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    fun scaleBitmap(origin: Bitmap?, ratio: Float): Bitmap? {
        if (origin == null) {
            return null
        }
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.preScale(ratio, ratio)
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (newBM == origin) {
            return newBM
        }
        origin.recycle()
        return newBM
    }

    /**
     * 裁剪
     * @param bitmap 原图
     * @return 裁剪后的图像
     */
    fun cropBitmap(bitmap: Bitmap): Bitmap {
        val w = bitmap.width // 得到图片的宽，高
        val h = bitmap.height
        var cropWidth = if (w >= h) h else w// 裁切后所取的正方形区域边长
        cropWidth /= 2
        val cropHeight = (cropWidth / 1.2).toInt()
        return Bitmap.createBitmap(bitmap, w / 3, 0, cropWidth, cropHeight, null, false)
    }

    /**
     * 选择变换
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    fun rotateBitmap(origin: Bitmap?, alpha: Float): Bitmap? {
        if (origin == null) {
            return null
        }
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.setRotate(alpha)
        // 围绕原地进行旋转
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (newBM == origin) {
            return newBM
        }
        origin.recycle()
        return newBM
    }

    /**
     * 偏移效果
     * @param origin 原图
     * @return 偏移后的bitmap
     */
    fun skewBitmap(origin: Bitmap?): Bitmap? {
        if (origin == null) {
            return null
        }
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.postSkew(-0.6f, -0.3f)
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (newBM == origin) {
            return newBM
        }
        origin.recycle()
        return newBM
    }

    /** 高斯模糊图片 */
    fun gaussBitmap(context: Context, image: Bitmap, listener: (Bitmap) -> Unit) {
        doAsync {
            //创建一个缩小后的bitmap
            val inputBitmap = Bitmap.createScaledBitmap(image, 200, 200, false)
            //创建将在ondraw中使用到的经过模糊处理后的bitmap
            val outputBitmap = Bitmap.createBitmap(inputBitmap)

            //创建RenderScript，ScriptIntrinsicBlur固定写法
            val rs = RenderScript.create(context)
            val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

            //根据inputBitmap，outputBitmap分别分配内存
            val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
            val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)

            //设置模糊半径取值0-25之间，不同半径得到的模糊效果不同
            blurScript.setRadius(24f)
            blurScript.setInput(tmpIn)
            blurScript.forEach(tmpOut)

            //得到最终的模糊bitmap
            tmpOut.copyTo(outputBitmap)
            uiThread {
                listener(outputBitmap)
            }
        }
    }

    /** 获取图片地址的图片的颜色 */
    fun getPaletteColor(context: Context, data: String, listener: (Int) -> Unit) {
        Glide.with(context).asBitmap().load(data).dontAnimate().into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                Palette.from(resource).generate { palette ->
                    listener(palette?.vibrantSwatch?.rgb
                            ?: ContextCompat.getColor(context, R.color.colorPrimary))
                }
            }
        })
    }

    /** 获取图片地址的图片的颜色和高斯模糊后的图片 */
    fun getGaussBitmap(context: Context, data: String, listener: (Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(data).dontAnimate().into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                gaussBitmap(context, resource) { bitmap -> listener(bitmap) }
            }
        })
    }

    /** 获取网络图片 */
    fun getNetworkBitmap(context: Context, data: String, listener: (Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(data).dontAnimate().into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                listener(resource)
            }
        })
    }
}