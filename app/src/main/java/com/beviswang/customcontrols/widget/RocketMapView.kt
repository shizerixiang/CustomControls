package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.util.BitmapHelper

/**
 * 飞行的火箭
 * @author BevisWang
 * @date 2019/9/5 15:36
 */
class RocketMapView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : View(context, attrs, def) {
    // 用于 wrap_content 的默认宽高 px
    private val mDefWidth = 300
    private val mDefHeight = 300
    @ColorInt
    private var mLineColor = Color.RED
    private var mRocket: Rocket
    private var mRocketSize: Int = 0
    private var mRocketRes: Int = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RocketMapView)
        mLineColor = typedArray.getColor(R.styleable.RocketMapView_colorLine, mLineColor)
        mRocketSize = typedArray.getDimensionPixelSize(R.styleable.RocketMapView_flyImageSize, 38)
        mRocketRes = typedArray.getResourceId(R.styleable.RocketMapView_flyImage, R.mipmap.ic_rocket)
        typedArray.recycle()

        mRocket = Rocket(context, this, mRocketSize, mRocketRes)

        mRocket.fly()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 针对 wrap_content 的处理，使 wrap_content 生效
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefWidth, mDefHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mRocket.drawRocket(canvas)
        val point = mRocket.getCurRocketPosition()
        Log.e("火箭坐标信息", "当前坐标：x:${point.x},y:${point.y}")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mRocket.destroy()
    }

    /** 火箭 */
    class Rocket(context: Context, var map: RocketMapView, var size: Int = 40, @DrawableRes private var bodyRes: Int = R.mipmap.ic_rocket) {
        var body: Bitmap                                    // 火箭贴图
        private var pathNormal: Path                        // 普通指令路径
        private var pathUrgent: Path                        // 紧急指令路径
        var paint: Paint                                    // 喷漆枪
        var matrix: Matrix                                  // 控制器
        var pathMeasure: PathMeasure                        // 终端
        private var orderNormal: ValueAnimator? = null      // 普通指令（按指定路径进行重复巡航）
        private var orderUrgent: ValueAnimator? = null      // 紧急指令（优先普通指令，单纯前往目标点的指令）
        private var progressNormal: Float = 0f              // 普通指令总路程
        private var curProgressNormal: Float = 0f           // 普通指令当前飞行进度
        private var progressUrgent: Float = 0f              // 紧急指令总路程
        private var curProgressUrgent: Float = 0f           // 紧急指令当前飞行进度
        var duration: Long = 1800                           // 指定飞行时间
        private var pos: FloatArray                         // 位置坐标
        private var tan: FloatArray                         // 位置切线
        private var isBusy: Boolean = false                 // 火箭是否繁忙（有其它优先任务）
        private var goToPointF: PointF                      // 优先任务目标点
        private lateinit var normalOrderPausePoint: PointF  // 普通指令最后执行位置

        init {
            val bitmap = BitmapFactory.decodeResource(context.resources, bodyRes)
            body = BitmapHelper.scaleBitmap(bitmap, size, size) ?: bitmap

            pathNormal = Path()
            pathNormal.addCircle(0f, 0f, 160f, Path.Direction.CW)
            pathUrgent = Path()

            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            map.setLayerType(LAYER_TYPE_HARDWARE, paint)

            matrix = Matrix()

            pathMeasure = PathMeasure()

            pos = floatArrayOf(0f, 0f)
            tan = floatArrayOf(0f, 0f)

            goToPointF = PointF(0f, 0f)
            normalOrderPausePoint = PointF(0f, 0f)

            setPath(pathNormal)
        }

        /** 设置火箭贴图 */
        fun setRocketBitmap(b: Bitmap) {
            body = BitmapHelper.scaleBitmap(b, size, size) ?: b
        }

        /**
         * 获取坐标
         * 弧度 = Math.atan2（邻边边长，对边边长）
         * 角度 = 弧度 * 180 / π
         * 圆的一周为 2π 的弧度
         * 故：弧度 = 角度 / 360 * 2π
         * 在绘制圆时的角度范围为（-180，180）
         * 故：弧度范围为（-π，π）
         * 即：在这里的角度和弧度关系的计算公式为：角度 = 弧度 * 180 / π
         */
        fun getCurRocketPosition(): PointF {
            pathMeasure.getPosTan(progressNormal * curProgressNormal, pos, tan)
//            val degrees: Float = (Math.atan2(tan[1].toDouble(), tan[0].toDouble()) * 180f / Math.PI).toFloat()
            return PointF(pos[0], pos[1])
        }

        /** 设置路径 */
        fun setPath(p: Path) {
            pathNormal = p
            pathMeasure.setPath(pathNormal, false)
            progressNormal = pathMeasure.length
        }

        /** 起飞 */
        fun fly() {
            orderNormal?.cancel()
            orderNormal = ValueAnimator.ofFloat(0f, 1f)
            orderNormal?.addUpdateListener {
                if (isBusy) {
                    pauseOrderNormal()
                    return@addUpdateListener
                }
                curProgressNormal = it.animatedValue as Float
                map.invalidate()
            }
            orderNormal?.duration = duration
            orderNormal?.repeatCount = ValueAnimator.INFINITE
            orderNormal?.start()
        }

        /** 绘制火箭 */
        fun drawRocket(canvas: Canvas?) {
            if (canvas == null) return
            pathMeasure.getMatrix(progressNormal * curProgressNormal, matrix, PathMeasure.POSITION_MATRIX_FLAG or PathMeasure.TANGENT_MATRIX_FLAG)
            canvas.save()
            canvas.translate(map.width / 2f, map.height / 2f)
            matrix.preRotate(90f)
            matrix.preTranslate(-size / 2f, -size / 2f)
            canvas.drawBitmap(body, matrix, null)
            canvas.restore()
        }

        /** 飞往指定地点 */
        fun goToPoint(p: PointF) {
            if (p.x != normalOrderPausePoint.x || p.y != normalOrderPausePoint.y) isBusy = true
            pauseOrderNormal()
            generatePath(p)
            orderUrgent?.cancel()
            orderUrgent = ValueAnimator.ofFloat(0f, 1f)
            orderUrgent?.addUpdateListener {
                curProgressUrgent = it.animatedValue as Float
                if (curProgressUrgent == 1f) {
                    if (!isBusy) {
                        setPath(pathNormal)
                        orderNormal?.resume()
                        return@addUpdateListener
                    }
                    isBusy = false
                    goToPoint(normalOrderPausePoint)
                    return@addUpdateListener
                }
                map.invalidate()
            }
            orderUrgent?.repeatCount = 1
            orderUrgent?.duration = 1000
            orderUrgent?.start()
        }

        /**
         * 生成路径
         * @param p 目标点
         */
        private fun generatePath(p: PointF) {
            pathUrgent = Path()
            pathUrgent.moveTo(normalOrderPausePoint.x, normalOrderPausePoint.y)
            pathUrgent.lineTo(p.x, p.y)
            pathMeasure = PathMeasure(pathUrgent, false)
        }

        /** 暂停普通指令 */
        private fun pauseOrderNormal() {
            orderNormal?.pause()
            normalOrderPausePoint = getCurRocketPosition()
        }

        /** 销毁火箭 */
        fun destroy() {
            body.recycle()
            orderNormal?.cancel()
        }
    }
}