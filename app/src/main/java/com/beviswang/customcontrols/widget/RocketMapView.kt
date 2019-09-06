package com.beviswang.customcontrols.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.beviswang.customcontrols.R
import com.beviswang.customcontrols.util.BitmapHelper
import com.beviswang.customcontrols.util.ViewHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

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
    private var mRocketList: ArrayList<Rocket>
    //    private var mRocket: Rocket
    private var mRocketSize: Int = 0
    private var mRocketRes: Int = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RocketMapView)
        mLineColor = typedArray.getColor(R.styleable.RocketMapView_colorLine, mLineColor)
        mRocketSize = typedArray.getDimensionPixelSize(R.styleable.RocketMapView_flyImageSize, ViewHelper.dip2px(context, 42f).toInt())
        mRocketRes = typedArray.getResourceId(R.styleable.RocketMapView_flyImage, R.mipmap.ic_rocket)
        typedArray.recycle()

        mRocketList = ArrayList()
//        mRocket = Rocket(context, this, mRocketSize, mRocketRes)
        doAsync {
            (1..1).forEach { _ ->
                Thread.sleep(200)
                uiThread {
                    mRocketList.add(Rocket.Builder(context, this@RocketMapView, mRocketSize, mRocketRes).create())
                    mRocketList.last().fly()
                    invalidate()
                }
            }
        }

//        mRocketList.forEach { it.fly() }
//        mRocket.fly()
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
//        mRocket.drawRocket(canvas)
        mRocketList.forEach { it.drawRocket(canvas) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                mRocket.flyToPoint(PointF(event.x, event.y))
                mRocketList.forEach { it.flyToPoint(PointF(event.x, event.y)) }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        mRocket.destroy()
        mRocketList.forEach { it.destroy() }
    }

    /** 火箭 */
    class Rocket(context: Context, var map: RocketMapView, var size: Int = 40, @DrawableRes private var bodyRes: Int = R.mipmap.ic_rocket) {
        private var centerPoint: PointF                     // 巡航中心点
        var body: Bitmap                                    // 火箭贴图
        private var pathNormal: Path                        // 普通指令路径
        private var pathUrgent: Path                        // 紧急指令路径
        var paint: Paint                                    // 喷漆枪
        var matrix: Matrix                                  // 控制器
        var pathMeasure: PathMeasure                        // 终端
        private var orderNormal: ValueAnimator? = null      // 普通指令（按指定路径进行重复巡航）
        private var orderUrgent: ValueAnimator? = null      // 紧急指令（优先普通指令，单纯前往目标点的指令）
        private var progressTotal: Float = 0f               // 当前指令的总路程
        private var curProgressNormal: Float = 0f           // 普通指令当前飞行进度比例
        private var curProgressUrgent: Float = -1f          // 紧急指令当前飞行进度比例
        var duration: Long = 2000                           // 指定飞行时间
        private var pos: FloatArray                         // 位置坐标
        private var tan: FloatArray                         // 位置切线
        private var isBusy: Boolean = false                 // 火箭是否繁忙（有其它优先任务）
        private var goToPointF: PointF                      // 优先任务目标点
        private var normalOrderPausePoint: PointF           // 普通指令最后执行位置
        private var urgentOrderTargetPoint: PointF          // 紧急指令最后执行位置
        private lateinit var oldOrderTargetPoint: PointF    // 最新指令的前一个指令所指定的目标点


        private var pathPaint: Paint

        init {
            centerPoint = PointF(map.width / 2f, map.height / 2f)

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
            urgentOrderTargetPoint = PointF(0f, 0f)
            initOldOrderTargetPoint()

            setPath(pathNormal)

            pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            pathPaint.style = Paint.Style.STROKE
            pathPaint.strokeWidth = 2f
            pathPaint.color = Color.GRAY
        }

        /** 飞往指定地点 */
        private fun goToPoint(cur: PointF, target: PointF) {
            pauseOrderNormal()
            // 如何判断是返回巡航的路径还是到紧急指令位置的路径（isBusy）还是普通巡航的路径
            generatePath(cur, target, getOldOrderTargetPoint())
            orderUrgent?.cancel()
            orderUrgent = ValueAnimator.ofFloat(0f, 1f)
            orderUrgent?.addUpdateListener {
                curProgressUrgent = it.animatedValue as Float
                if (curProgressUrgent == 1f) {
                    finishUrgentOrder()
                    if (!isBusy) {
                        setPath(pathNormal)
                        orderNormal?.resume()
                        return@addUpdateListener
                    }
                    isBusy = false
                    goToPoint(urgentOrderTargetPoint, normalOrderPausePoint)
                    return@addUpdateListener
                }
                map.invalidate()
            }
//            orderUrgent?.interpolator = LinearInterpolator()
            orderUrgent?.duration = 2000
            orderUrgent?.start()
        }

        /** 初始化上次指令的目标地点 */
        private fun initOldOrderTargetPoint() {
            val pathMeasure = PathMeasure(pathNormal, false)
            pathMeasure.getPosTan(pathMeasure.length, pos, tan)
            oldOrderTargetPoint = PointF(pos[0], pos[1])
        }

        /** 完成紧急指令 */
        private fun finishUrgentOrder() {
            curProgressUrgent = -1f
            orderUrgent?.cancel()
        }

        /** 获取普通指令的目标点 */
        private fun getOldOrderTargetPoint(): PointF {
            if (!isBusy) {
                // 在目标地点返回
//                initOldOrderTargetPoint()
                // 模拟一个点，用于缓冲急转弯
                // y = （ y2 - y1 ） / （ x2 - x1 ） * （ x - x1 ） + y1

                val pathMeasure = PathMeasure(pathUrgent, false)
                pathMeasure.getPosTan(pathMeasure.length, pos, tan)
                val x = if (urgentOrderTargetPoint.x <= 0) urgentOrderTargetPoint.x - 100f else urgentOrderTargetPoint.x + 100f
                val y = (urgentOrderTargetPoint.y - pos[1]) / (urgentOrderTargetPoint.x - pos[0]) * (x - pos[0]) + pos[1]

                oldOrderTargetPoint.x = x
                oldOrderTargetPoint.y = y
                return oldOrderTargetPoint
            }
            // 正在前往指定地点
            return oldOrderTargetPoint
        }

        /**
         * 生成路径
         * @param cur 当前点
         * @param newTarget 目标点
         */
        private fun generatePath(cur: PointF, newTarget: PointF, oldTarget: PointF) {
            if (isBusy) Log.e("Rocket", "接收到紧急指令！前往指定地点：（${cur.x}，${cur.y}） ----> （${newTarget.x}，${newTarget.y}）")
            else Log.e("Rocket", "紧急指令完成！返回巡航任务地点：（${cur.x}，${cur.y}） ----> （${newTarget.x}，${newTarget.y}）")
            pathUrgent = Path()
            pathUrgent.moveTo(cur.x, cur.y)
//            if (!isBusy) {
//                pathUrgent.lineTo(newTarget.x, newTarget.y)
//            } else {
            pathUrgent.quadTo(oldTarget.x, oldTarget.y, newTarget.x, newTarget.y)
//            }
            pathMeasure = PathMeasure(pathUrgent, false)
            progressTotal = pathMeasure.length
        }

        /** 暂停普通指令 */
        private fun pauseOrderNormal() {
            orderNormal?.pause()
            val pathMeasure = PathMeasure(pathNormal, false)
            pathMeasure.getPosTan(pathMeasure.length * curProgressNormal, pos, tan)
            normalOrderPausePoint.x = pos[0]
            normalOrderPausePoint.y = pos[1]
//            normalOrderPausePoint = PointF(pos[0], pos[1])
        }

        /** 设置巡航中心点 */
        fun setCenterPoint(p: PointF) {
            centerPoint = p
            initOldOrderTargetPoint()
        }

        /** 获取巡航中心点 */
        fun getCenterPoint(): PointF = centerPoint

        /** 设置火箭贴图 */
        fun setRocketBitmap(b: Bitmap) {
            body = BitmapHelper.scaleBitmap(b, size, size) ?: b
        }

        /** 绘制火箭 */
        fun drawRocket(canvas: Canvas?) {
            if (canvas == null) return
            val cur = if (curProgressUrgent < 0f) curProgressNormal else curProgressUrgent // 当前指令执行进度
            pathMeasure.getMatrix(progressTotal * cur, matrix, PathMeasure.POSITION_MATRIX_FLAG or PathMeasure.TANGENT_MATRIX_FLAG)
            canvas.save()
            canvas.translate(centerPoint.x, centerPoint.y)
            matrix.preRotate(90f)
            matrix.preTranslate(-size / 2f, -size / 2f)
            canvas.drawBitmap(body, matrix, null)
//            canvas.drawPath(pathNormal, pathPaint)
//            canvas.drawPath(pathUrgent, pathPaint)
            canvas.restore()
        }

        /** 设置路径 */
        fun setPath(p: Path) {
            initOldOrderTargetPoint()
            pathNormal = p
            pathMeasure.setPath(pathNormal, false)
            progressTotal = pathMeasure.length
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
//            orderNormal?.interpolator = LinearInterpolator()
            orderNormal?.duration = duration
            orderNormal?.repeatCount = ValueAnimator.INFINITE
            orderNormal?.start()
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
            val cur = if (curProgressUrgent < 0f) curProgressNormal else curProgressUrgent // 当前指令执行进度
            pathMeasure.getPosTan(progressTotal * cur, pos, tan)
//            val degrees: Float = (Math.atan2(tan[1].toDouble(), tan[0].toDouble()) * 180f / Math.PI).toFloat()
            return PointF(pos[0], pos[1])
        }

        /** 飞往指定地点 */
        fun flyToPoint(p: PointF) {
            p.x -= centerPoint.x
            p.y -= centerPoint.y
            // 之前的紧急任务未执行完，取前一个指令的目标点
            if (isBusy) oldOrderTargetPoint = urgentOrderTargetPoint
            // 之前的任务已完成则直接取普通指令的目标点，Q：但是不知道是在回去的路上还是已经回到普通任务中了
            else {
//                initOldOrderTargetPoint()

                val point = getCenterPoint()
                // 从原点到圆上一点的直线的斜率
                val k = point.y / point.x
                // 垂直于该直线的直线和该直线的斜率乘积为 -1
                val vk = -1 / k
                val m = point.y - (vk * point.x)
                // 公式：y = vk * x + m
                // 假定我要取此直线上距离已知点 point.x + 40 的 x 坐标，可求出 y 的坐标
                val x = point.x + 40f
                val y = vk * x + m
                oldOrderTargetPoint.x = x
                oldOrderTargetPoint.y = y
            }
            isBusy = true
            urgentOrderTargetPoint = p
            goToPoint(getCurRocketPosition(), p)
        }

        /** 销毁火箭 */
        fun destroy() {
            body.recycle()
            orderNormal?.cancel()
        }

        /** 火箭建造者 */
        class Builder(context: Context, var map: RocketMapView, var size: Int = 40, @DrawableRes private var bodyRes: Int = R.mipmap.ic_rocket) {
            private var rocket: Rocket = Rocket(context, map, size, bodyRes)

            fun addCenterPoint(p: PointF): Builder {
                rocket.setCenterPoint(p)
                return this
            }

            fun addRocketBitmap(b: Bitmap): Builder {
                rocket.setRocketBitmap(b)
                return this
            }

            fun addPath(path: Path): Builder {
                rocket.setPath(path)
                return this
            }

            fun create(): Rocket {
                return rocket
            }
        }

        /** 指令 */
        class Order(var startPoint: PointF = PointF(0f, 0f) /* 起始位置 */,
                    var targetPoint: PointF = PointF(0f, 0f) /* 目标位置 */)
    }
}