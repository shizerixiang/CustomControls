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
    private var mRocketList: ArrayList<SuperRocket>
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
                    mRocketList.add(SuperRocket(context, this@RocketMapView, mRocketSize))
                    mRocketList.last().flyAuto()
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
        mRocketList.forEach { it.draw(canvas) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                mRocket.flyToPoint(PointF(event.x, event.y))
                mRocketList.forEach { it.flyOrder(PointF(event.x, event.y)) }
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

//    /** 火箭 */
//    class Rocket(context: Context, var map: RocketMapView, var size: Int = 40, @DrawableRes private var bodyRes: Int = R.mipmap.ic_rocket) {
//        private var centerPoint: PointF                     // 巡航中心点
//        var body: Bitmap                                    // 火箭贴图
//        private var pathNormal: Path                        // 普通指令路径
//        private var pathUrgent: Path                        // 紧急指令路径
//        var paint: Paint                                    // 喷漆枪
//        var matrix: Matrix                                  // 控制器
//        var pathMeasure: PathMeasure                        // 终端
//        private var orderNormal: ValueAnimator? = null      // 普通指令（按指定路径进行重复巡航）
//        private var orderUrgent: ValueAnimator? = null      // 紧急指令（优先普通指令，单纯前往目标点的指令）
//        private var progressTotal: Float = 0f               // 当前指令的总路程
//        private var curProgressNormal: Float = 0f           // 普通指令当前飞行进度比例
//        private var curProgressUrgent: Float = -1f          // 紧急指令当前飞行进度比例
//        var duration: Long = 2000                           // 指定飞行时间
//        private var pos: FloatArray                         // 位置坐标
//        private var tan: FloatArray                         // 位置切线
//        private var isBusy: Boolean = false                 // 火箭是否繁忙（有其它优先任务）
//        private var goToPointF: PointF                      // 优先任务目标点
//        private var normalOrderPausePoint: PointF           // 普通指令最后执行位置
//        private var urgentOrderTargetPoint: PointF          // 紧急指令最后执行位置
//        private lateinit var oldOrderTargetPoint: PointF    // 最新指令的前一个指令所指定的目标点
//
//        private var pathPaint: Paint
//
//        init {
//            centerPoint = PointF(map.width / 2f, map.height / 2f)
//
//            val bitmap = BitmapFactory.decodeResource(context.resources, bodyRes)
//            body = BitmapHelper.scaleBitmap(bitmap, size, size) ?: bitmap
//
//            pathNormal = Path()
//            pathNormal.addCircle(0f, 0f, 160f, Path.Direction.CW)
//            pathUrgent = Path()
//
//            paint = Paint(Paint.ANTI_ALIAS_FLAG)
//            map.setLayerType(LAYER_TYPE_HARDWARE, paint)
//
//            matrix = Matrix()
//
//            pathMeasure = PathMeasure()
//
//            pos = floatArrayOf(0f, 0f)
//            tan = floatArrayOf(0f, 0f)
//
//            goToPointF = PointF(0f, 0f)
//            normalOrderPausePoint = PointF(0f, 0f)
//            urgentOrderTargetPoint = PointF(0f, 0f)
//            initOldOrderTargetPoint()
//
//            setPath(pathNormal)
//
//            pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//            pathPaint.style = Paint.Style.STROKE
//            pathPaint.strokeWidth = 2f
//            pathPaint.color = Color.GRAY
//        }
//
//        /** 飞往指定地点 */
//        private fun goToPoint(cur: PointF, target: PointF) {
//            pauseOrderNormal()
//            // 如何判断是返回巡航的路径还是到紧急指令位置的路径（isBusy）还是普通巡航的路径
//            generatePath(cur, target, getOldOrderTargetPoint())
//            orderUrgent?.cancel()
//            orderUrgent = ValueAnimator.ofFloat(0f, 1f)
//            orderUrgent?.addUpdateListener {
//                curProgressUrgent = it.animatedValue as Float
//                if (curProgressUrgent == 1f) {
//                    finishUrgentOrder()
//                    if (!isBusy) {
//                        setPath(pathNormal)
//                        orderNormal?.resume()
//                        return@addUpdateListener
//                    }
//                    isBusy = false
//                    goToPoint(urgentOrderTargetPoint, normalOrderPausePoint)
//                    return@addUpdateListener
//                }
//                map.invalidate()
//            }
////            orderUrgent?.interpolator = LinearInterpolator()
//            orderUrgent?.duration = 2000
//            orderUrgent?.start()
//        }
//
//        /** 初始化上次指令的目标地点 */
//        private fun initOldOrderTargetPoint() {
//            val pathMeasure = PathMeasure(pathNormal, false)
//            pathMeasure.getPosTan(pathMeasure.length, pos, tan)
//            oldOrderTargetPoint = PointF(pos[0], pos[1])
//        }
//
//        /** 完成紧急指令 */
//        private fun finishUrgentOrder() {
//            curProgressUrgent = -1f
//            orderUrgent?.cancel()
//        }
//
//        /** 获取普通指令的目标点 */
//        private fun getOldOrderTargetPoint(): PointF {
//            if (!isBusy) {
//                // 在目标地点返回
////                initOldOrderTargetPoint()
//                // 模拟一个点，用于缓冲急转弯
//                // y = （ y2 - y1 ） / （ x2 - x1 ） * （ x - x1 ） + y1
//
//                val pathMeasure = PathMeasure(pathUrgent, false)
//                pathMeasure.getPosTan(pathMeasure.length, pos, tan)
//                val x = if (urgentOrderTargetPoint.x <= 0) urgentOrderTargetPoint.x - 100f else urgentOrderTargetPoint.x + 100f
//                val y = (urgentOrderTargetPoint.y - pos[1]) / (urgentOrderTargetPoint.x - pos[0]) * (x - pos[0]) + pos[1]
//
//                oldOrderTargetPoint.x = x
//                oldOrderTargetPoint.y = y
//                return oldOrderTargetPoint
//            }
//            // 正在前往指定地点
//            return oldOrderTargetPoint
//        }
//
//        /**
//         * 生成路径
//         * @param cur 当前点
//         * @param newTarget 目标点
//         */
//        private fun generatePath(cur: PointF, newTarget: PointF, oldTarget: PointF) {
//            if (isBusy) Log.e("Rocket", "接收到紧急指令！前往指定地点：（${cur.x}，${cur.y}） ----> （${newTarget.x}，${newTarget.y}）")
//            else Log.e("Rocket", "紧急指令完成！返回巡航任务地点：（${cur.x}，${cur.y}） ----> （${newTarget.x}，${newTarget.y}）")
//            pathUrgent = Path()
//            pathUrgent.moveTo(cur.x, cur.y)
////            if (!isBusy) {
////                pathUrgent.lineTo(newTarget.x, newTarget.y)
////            } else {
//            pathUrgent.quadTo(oldTarget.x, oldTarget.y, newTarget.x, newTarget.y)
////            }
//            pathMeasure = PathMeasure(pathUrgent, false)
//            progressTotal = pathMeasure.length
//        }
//
//        /** 暂停普通指令 */
//        private fun pauseOrderNormal() {
//            orderNormal?.pause()
//            val pathMeasure = PathMeasure(pathNormal, false)
//            pathMeasure.getPosTan(pathMeasure.length * curProgressNormal, pos, tan)
//            normalOrderPausePoint.x = pos[0]
//            normalOrderPausePoint.y = pos[1]
////            normalOrderPausePoint = PointF(pos[0], pos[1])
//        }
//
//        /** 设置巡航中心点 */
//        fun setCenterPoint(p: PointF) {
//            centerPoint = p
//            initOldOrderTargetPoint()
//        }
//
//        /** 获取巡航中心点 */
//        fun getCenterPoint(): PointF = centerPoint
//
//        /** 设置火箭贴图 */
//        fun setRocketBitmap(b: Bitmap) {
//            body = BitmapHelper.scaleBitmap(b, size, size) ?: b
//        }
//
//        /** 绘制火箭 */
//        fun drawRocket(canvas: Canvas?) {
//            if (canvas == null) return
//            val cur = if (curProgressUrgent < 0f) curProgressNormal else curProgressUrgent // 当前指令执行进度
//            pathMeasure.getMatrix(progressTotal * cur, matrix, PathMeasure.POSITION_MATRIX_FLAG or PathMeasure.TANGENT_MATRIX_FLAG)
//            canvas.save()
//            canvas.translate(centerPoint.x, centerPoint.y)
//            matrix.preRotate(90f)
//            matrix.preTranslate(-size / 2f, -size / 2f)
//            canvas.drawBitmap(body, matrix, null)
////            canvas.drawPath(pathNormal, pathPaint)
////            canvas.drawPath(pathUrgent, pathPaint)
//            canvas.restore()
//        }
//
//        /** 设置路径 */
//        fun setPath(p: Path) {
//            initOldOrderTargetPoint()
//            pathNormal = p
//            pathMeasure.setPath(pathNormal, false)
//            progressTotal = pathMeasure.length
//        }
//
//        /** 起飞 */
//        fun fly() {
//            orderNormal?.cancel()
//            orderNormal = ValueAnimator.ofFloat(0f, 1f)
//            orderNormal?.addUpdateListener {
//                if (isBusy) {
//                    pauseOrderNormal()
//                    return@addUpdateListener
//                }
//                curProgressNormal = it.animatedValue as Float
//                map.invalidate()
//            }
////            orderNormal?.interpolator = LinearInterpolator()
//            orderNormal?.duration = duration
//            orderNormal?.repeatCount = ValueAnimator.INFINITE
//            orderNormal?.start()
//        }
//
//        /**
//         * 获取坐标
//         * 弧度 = Math.atan2（邻边边长，对边边长）
//         * 角度 = 弧度 * 180 / π
//         * 圆的一周为 2π 的弧度
//         * 故：弧度 = 角度 / 360 * 2π
//         * 在绘制圆时的角度范围为（-180，180）
//         * 故：弧度范围为（-π，π）
//         * 即：在这里的角度和弧度关系的计算公式为：角度 = 弧度 * 180 / π
//         */
//        fun getCurRocketPosition(): PointF {
//            val cur = if (curProgressUrgent < 0f) curProgressNormal else curProgressUrgent // 当前指令执行进度
//            pathMeasure.getPosTan(progressTotal * cur, pos, tan)
////            val degrees: Float = (Math.atan2(tan[1].toDouble(), tan[0].toDouble()) * 180f / Math.PI).toFloat()
//            return PointF(pos[0], pos[1])
//        }
//
//        /** 飞往指定地点 */
//        fun flyToPoint(p: PointF) {
//            p.x -= centerPoint.x
//            p.y -= centerPoint.y
//            // 之前的紧急任务未执行完，取前一个指令的目标点
//            if (isBusy) oldOrderTargetPoint = urgentOrderTargetPoint
//            // 之前的任务已完成则直接取普通指令的目标点，Q：但是不知道是在回去的路上还是已经回到普通任务中了
//            else {
////                initOldOrderTargetPoint()
//
//                val point = getCenterPoint()
//                // 从原点到圆上一点的直线的斜率
//                val k = point.y / point.x
//                // 垂直于该直线的直线和该直线的斜率乘积为 -1
//                val vk = -1 / k
//                val m = point.y - (vk * point.x)
//                // 公式：y = vk * x + m
//                // 假定我要取此直线上距离已知点 point.x + 40 的 x 坐标，可求出 y 的坐标
//                val x = point.x + 40f
//                val y = vk * x + m
//                oldOrderTargetPoint.x = x
//                oldOrderTargetPoint.y = y
//            }
//            isBusy = true
//            urgentOrderTargetPoint = p
//            goToPoint(getCurRocketPosition(), p)
//        }
//
//        /** 销毁火箭 */
//        fun destroy() {
//            body.recycle()
//            orderNormal?.cancel()
//        }
//
//        /** 火箭建造者 */
//        class Builder(context: Context, var map: RocketMapView, var size: Int = 40, @DrawableRes private var bodyRes: Int = R.mipmap.ic_rocket) {
//            private var rocket: Rocket = Rocket(context, map, size, bodyRes)
//
//            fun addCenterPoint(p: PointF): Builder {
//                rocket.setCenterPoint(p)
//                return this
//            }
//
//            fun addRocketBitmap(b: Bitmap): Builder {
//                rocket.setRocketBitmap(b)
//                return this
//            }
//
//            fun addPath(path: Path): Builder {
//                rocket.setPath(path)
//                return this
//            }
//
//            fun create(): Rocket {
//                return rocket
//            }
//        }
//    }


    /**
     * 需要完成的任务：
     * 1、紧急指令改为三阶贝塞尔曲线，每次紧急指令都考虑返回为前提，将返程点的镜像点作为第二个控制点；
     * 2、返程指令改为二阶贝塞尔曲线，以目标方向的特定点的镜像点作为控制点；
     * 3、给火箭添加速度和固定的加速度参数，在火箭接受紧急指令时，继承当前速度，同时设置一个速度峰值，在速度峰值时保持匀速；
     * 不合适3、给火箭添加一个速度参数，让火箭能够在接受紧急指令时，做一个速度的缓冲处理；
     * 4、指令中断时，将火箭方向延长为加速度缓冲机制缓冲完成所需的路程，并将抵达的位置点作为第一控制点（好处：保证火箭在下一个路径上有足够的长度做减速缓冲；坏处：）
     */

    /** 超级火箭 */
    class SuperRocket(context: Context, val map: RocketMapView, var size: Int, @DrawableRes private var bodyRes: Int = R.mipmap.ic_rocket) {
        companion object {
            const val ROCKET_STATE_NOT_FLY = -1                             // 未启动
            const val ROCKET_STATE_NORMAL_ORDER = 0                         // 执行普通指令
            const val ROCKET_STATE_URGENT_ORDER = 1                         // 执行紧急指令
            const val ROCKET_STATE_NORMAL_ORDER_RETURN = 2                  // 返回普通指令
        }

        private var centerPoint: PointF                                     // 普通指令执行位置
        private var body: Bitmap                                            // 火箭本体
        private var paint: Paint                                            // 喷漆枪
        private var matrix: Matrix                                          // 控制器
        private var normalOrderProgress: Float = 0f                         // 普通指令进度
        private var urgentOrderProgress: Float = 0f                         // 紧急指令进度
        /**
         * 火箭状态 one of [ROCKET_STATE_NOT_FLY] [ROCKET_STATE_NORMAL_ORDER]
         * [ROCKET_STATE_URGENT_ORDER] [ROCKET_STATE_NORMAL_ORDER_RETURN]
         */
        private var state: Int
//        private var normalOrder: Order                                      // 普通指令
        private var normalOrder: NormalOrder                                      // 普通指令
        private var urgentOrder: Order? = null                              // 紧急指令
        private var normalOrderAnimator: ValueAnimator? = null              // 普通指令动画
        private var urgentOrderAnimator: ValueAnimator? = null              // 紧急指令动画

        private var pathPaint: Paint

        init {
            centerPoint = PointF(map.width / 2f, map.height / 2f)
            val bitmap = BitmapFactory.decodeResource(context.resources, bodyRes)
            body = BitmapHelper.scaleBitmap(bitmap, size, size) ?: bitmap
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
            map.setLayerType(LAYER_TYPE_HARDWARE, paint)
            matrix = Matrix()

            state = ROCKET_STATE_NOT_FLY
            normalOrder = NormalOrder(centerPoint)


            pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            pathPaint.style = Paint.Style.STROKE
            pathPaint.strokeWidth = 2f
            pathPaint.color = Color.GRAY
        }

        /** 自动巡逻飞行 */
        fun flyAuto(target: PointF = PointF(0f, 0f)) {
            state = ROCKET_STATE_NORMAL_ORDER
            normalOrder = NormalOrder(target)
            doNormalFly()
        }

        /** 执行普通指令 */
        private fun doNormalFly() {
            state = ROCKET_STATE_NORMAL_ORDER
            normalOrderAnimator?.cancel()
            normalOrderAnimator = ValueAnimator.ofFloat(0f, 1f)
            normalOrderAnimator?.addUpdateListener {
                normalOrderProgress = it.animatedValue as Float
                if (state != ROCKET_STATE_NORMAL_ORDER) {
                    it.pause()
                    return@addUpdateListener
                }
                map.invalidate()
            }
            normalOrderAnimator?.interpolator = LinearInterpolator()
            normalOrderAnimator?.duration = 1800
            normalOrderAnimator?.repeatCount = ValueAnimator.INFINITE
            normalOrderAnimator?.start()
        }

        /** 飞往指定地点，人为改变目的地，需要根据当前状态判断如何做更改，更改执行完成后修改状态为 [ROCKET_STATE_URGENT_ORDER] */
        fun flyOrder(target: PointF) {
            target.x -= centerPoint.x
            target.y -= centerPoint.y
            parseOrder(target)
            state = ROCKET_STATE_URGENT_ORDER
            doUrgentFly()
        }

        /** 火箭自行更改目的地，一般指火箭完成了当前任务，开始自行返程，调用该方法之前应该确定好状态 */
        private fun flyOrderBySelf(target: PointF) {
            parseOrder(target)
            doUrgentFly()
        }

        /** 解析指令 */
        private fun parseOrder(target: PointF) {
            if (state == ROCKET_STATE_NOT_FLY) flyAuto()
            when (state) {
                ROCKET_STATE_NORMAL_ORDER -> {
                    val sp = normalOrder.getCurPointOnPath()
                    val stp = normalOrder.getTargetPoint()
                    val etp = PointF()
                    // C 式
                    etp.x = target.x - (stp.x - sp.x)
                    etp.y = target.y - (stp.y - sp.y)
                    // S 式
//                    etp.x = target.x + (stp.x - sp.x)
//                    etp.y = target.y + (stp.y - sp.y)
                    urgentOrder = UrgentOrder(sp, target, etp, stp)
                }
                ROCKET_STATE_URGENT_ORDER -> {
                    val sp = urgentOrder!!.getCurPointOnPath()
                    val stp = urgentOrder!!.getTargetPoint()
                    val etp = normalOrder.getCurPointOnPath()
                    urgentOrder = UrgentOrder(sp, target, etp, stp)
                }
                ROCKET_STATE_NORMAL_ORDER_RETURN -> {
                    val sp = urgentOrder!!.getCurPointOnPath()
                    val etp = normalOrder.getTargetPoint()
                    urgentOrder = UrgentOrder(sp, target, etp)
                }
            }
        }

        /** 执行紧急指令 */
        private fun doUrgentFly() {
            urgentOrderAnimator?.cancel()
            urgentOrderAnimator = ValueAnimator.ofFloat(0f, 1f)
            urgentOrderAnimator?.addUpdateListener {
                urgentOrderProgress = it.animatedValue as Float
                if (urgentOrderProgress != 1f) {
                    map.invalidate()
                    return@addUpdateListener
                }
                if (state == ROCKET_STATE_URGENT_ORDER) {
                    state = ROCKET_STATE_NORMAL_ORDER_RETURN
                    flyOrderBySelf(normalOrder.getCurPointOnPath())
                    return@addUpdateListener
                }
                if (state == ROCKET_STATE_NORMAL_ORDER_RETURN) {
                    state = ROCKET_STATE_NORMAL_ORDER
                    normalOrderAnimator?.resume()
                    it.cancel()
                    return@addUpdateListener
                }
            }
            urgentOrderAnimator?.interpolator = LinearInterpolator()
            urgentOrderAnimator?.duration = 1800
            urgentOrderAnimator?.start()
        }

        /** 绘制火箭 */
        fun draw(canvas: Canvas?) {
            if (canvas == null) return
            matrix = when (state) {
                ROCKET_STATE_NOT_FLY -> return
                ROCKET_STATE_NORMAL_ORDER -> normalOrder.getMatrix(normalOrderProgress)
                else -> urgentOrder?.getMatrix(urgentOrderProgress) ?: return
            }
            canvas.save()
            canvas.translate(centerPoint.x, centerPoint.y)
            matrix.preRotate(90f)
            matrix.preTranslate(-size / 2f, -size / 2f)
            canvas.drawBitmap(body, matrix, null)
            when (state) {
                ROCKET_STATE_NOT_FLY -> return
                ROCKET_STATE_NORMAL_ORDER -> {
                    canvas.drawPath(normalOrder.getOrderPath(), pathPaint)
                    canvas.drawPath(normalOrder.mTanPath, pathPaint)
                }
                else -> {
                    canvas.drawPath(normalOrder.getOrderPath(), pathPaint)
                    canvas.drawPath(urgentOrder!!.getOrderPath(), pathPaint)
                    canvas.drawPath(normalOrder.mTanPath, pathPaint)
                }
            }
            canvas.restore()
        }

        /** 销毁火箭 */
        fun destroy() {
            body.recycle()
            normalOrderAnimator?.cancel()
            urgentOrderAnimator?.cancel()
        }

        /** 指令 */
        interface Order {
            /**
             * 获取路径矩阵
             * @param progress 路径内当前的进度
             * @return 矩阵
             */
            fun getMatrix(progress: Float): Matrix

            /**
             * @return 获取当前路程进度
             */
            fun getCurProgress(): Float

            /**
             * @return 获取总路程
             */
            fun getTotalDistance(): Float

            /**
             * @return 获取指令路径
             */
            fun getOrderPath(): Path

            /**
             * @return 获取当前位置
             */
            fun getCurPointOnPath(): PointF

            /**
             * @return 获取当前进度的点的目标
             */
            fun getTargetPoint(): PointF
        }

        /** 普通指令，没给出特定的 path ，则会默认在起始点周围转圈 */
        class NormalOrder(var startPoint: PointF = PointF(0f, 0f) /* 执行命令的起始位置 */, var path: Path? = null) : Order {
            private lateinit var pathMeasure: PathMeasure                   /* 路径测量 */
            private var totalDistance: Float = 0f                           /* 总路程 */
            private var curProgress: Float = 0f                             /* 当前普通指令的进度 */
            private var curTotalProgress: Float = 0f                        // 当前普通指令和移动指令的总进度（没有移动命令则和普通指令的进度相等）
            private var moveOrderScale: Float = 0f                          // 移动指令的比重，默认为 0 比重；如果有移动指令，则计算移动指令的比重
            private var moveOrder: Order? = null                            // 执行普通指令时，没有处于普通指令的路径中，则需要该指令过渡到普通指令
            private var orderMatrix: Matrix

            private val bufferFactor: Float = 90f                           // 缓冲因子，即：在只确定方向的情况下，转向该方向所损耗的路程


            var mTanPath:Path = Path()

            init {
                generatePath(startPoint)
                orderMatrix = Matrix()
            }

            private fun generatePath(sp: PointF) {
                if (path == null) generateNormalPath(sp)
                pathMeasure = PathMeasure(path, false)
                totalDistance = pathMeasure.length
                val pathStartPoint = getPointOnPath(0f)
                val etp = getStartTargetPoint(pathStartPoint, sp)
                moveOrder = UrgentOrder(sp, pathStartPoint, etp)
            }

            /**
             * 获取圆上点的目标点
             * @param p 圆上一点
             * @param o 原点
             */
            private fun getStartTargetPoint(p: PointF, o: PointF): PointF {
//                // 这里基于 o 点的坐标轴，即：o 为原点（相对坐标系）
//                val x = p.x - o.x
//                val y = p.y - o.y
//                // 从原点到圆上一点的直线的斜率 y = kx -> k = y / x
//                val k = y / x
//                // 垂直于该直线的直线和该直线的斜率乘积为 -1      vk * k = -1 -> vk = -1 / k
//                val vk = -1 / k
//                val m = y - (vk * x)
//                // 公式：y = vk * x + m
//                // 假定我要取此直线上距离已知点 point.x + 40 的 x 坐标，可求出 y 的坐标
//                val tx = x + bufferFactor
//                val ty = vk * x + m
//                // 转回 map 的绝对坐标系
//                return PointF(tx + o.x, ty + o.y)

                // 这里基于 o 点的坐标轴，即：o 为原点（相对坐标系）
                val x = p.x
                val y = p.y
                // 从原点到圆上一点的直线的斜率 y = kx -> k = y / x
                val k = y / x
                // 垂直于该直线的直线和该直线的斜率乘积为 -1      vk * k = -1 -> vk = -1 / k
                val vk = -1 / k
                val m = y - (vk * x)
                // 公式：y = vk * x + m
                // 假定我要取此直线上距离已知点 point.x + 40 的 x 坐标，可求出 y 的坐标
                val tx = x + bufferFactor
                val ty = vk * x + m

                mTanPath = Path()
                mTanPath.moveTo(x,y)
                mTanPath.lineTo(tx,ty)

                return PointF(tx, ty)
            }

            private fun getPointOnPath(progress: Float): PointF {
                val pos = floatArrayOf(0f, 0f)
                val tan = floatArrayOf(0f, 0f)
                pathMeasure.getPosTan(totalDistance * progress, pos, tan)
                return PointF(pos[0], pos[1])
            }

            private fun generateNormalPath(sp: PointF) {
                path = Path()
                Log.e("坐标位置", "normalOrder:x=${sp.x},y=${sp.y}")
                path?.addCircle(sp.x, sp.y, 160f, Path.Direction.CW)
            }

            override fun getMatrix(progress: Float): Matrix {
                curTotalProgress = progress
                // 执行移动指令
                if (moveOrder != null) return getMoveMatrix(progress)
                // 执行普通指令
                curProgress = (progress - moveOrderScale) / (1 - moveOrderScale)
                pathMeasure.getMatrix(curProgress * totalDistance, orderMatrix,
                        PathMeasure.TANGENT_MATRIX_FLAG or PathMeasure.POSITION_MATRIX_FLAG)
                return orderMatrix
            }

            /**
             * 获取移动指令的矩阵
             * @param progress 当前总进度
             * @return 矩阵
             */
            private fun getMoveMatrix(progress: Float): Matrix {
                moveOrderScale = moveOrder!!.getTotalDistance() / (moveOrder!!.getTotalDistance() + totalDistance)
                val moveProgress = progress / moveOrderScale
                val matrix = moveOrder!!.getMatrix(moveProgress)
                if (moveProgress > 1f) {
                    moveOrderScale = 0f
                    moveOrder = null
                }
                return matrix
            }

            override fun getCurProgress() = curProgress

            override fun getTotalDistance() = totalDistance

            override fun getOrderPath() = path!!

            override fun getCurPointOnPath(): PointF {
                if (curProgress == 0f && moveOrder != null) return moveOrder!!.getCurPointOnPath()
                return getPointOnPath(curProgress)
            }

            override fun getTargetPoint(): PointF {
                if (curProgress == 0f && moveOrder != null) return moveOrder!!.getTargetPoint()
                return getStartTargetPoint(getPointOnPath(curProgress), startPoint)
            }
        }

        /** 紧急指令，通过存不存在原目标点判断使用 二阶 还是 三阶 贝塞尔曲线 */
        class UrgentOrder(var startPoint: PointF, var endPoint: PointF, var endTargetPoint: PointF, var startTargetPoint: PointF? = null) : Order {
            private var path: Path = Path()
            private var pathMeasure: PathMeasure
            private var totalDistance: Float = 0f                               // 总路程
            private var curProgress: Float = 0f                                 // 当前进度
            private var orderMatrix: Matrix                                     // 矩阵

            init {
                generatePath(startPoint, endPoint, startTargetPoint, endTargetPoint)
                pathMeasure = PathMeasure(path, false)
                totalDistance = pathMeasure.length
                orderMatrix = Matrix()
            }

            /** 生成路径 */
            private fun generatePath(sp: PointF, ep: PointF, stp: PointF?, etp: PointF) {
                // 没有第一控制点，做二阶贝塞尔曲线
                if (stp == null) generateQuadPath(sp, ep, etp)
                else generateCubicPath(sp, ep, stp, etp)
            }

            /**
             * 生成二阶贝塞尔路径
             * @param sp 起始点
             * @param ep 结束点
             * @param etp 结束点的目标点
             */
            private fun generateQuadPath(sp: PointF, ep: PointF, etp: PointF) {
                path.moveTo(sp.x, sp.y)
                // 控制点，即：【结束点的目标点】 基于 【结束点】 的 【镜像的一个模拟点】
                val cp = PointF(ep.x * 2 - etp.x, ep.y * 2 - etp.y)
                path.quadTo(cp.x, cp.y, ep.x, ep.y)
            }

            /**
             * 生成三阶贝塞尔路径
             * @param sp 起始点
             * @param ep 结束点
             * @param stp 起始点的目标点
             * @param etp 结束点的目标点
             */
            private fun generateCubicPath(sp: PointF, ep: PointF, stp: PointF, etp: PointF) {
                path.moveTo(sp.x, sp.y)
                // 第一个控制点，即：【起始点的目标点】
                val cp1 = stp
                // 第二个控制点，即：【结束点的目标点】 基于 【结束点】 的 【镜像的一个模拟点】
                val cp2 = PointF(ep.x * 2 - etp.x, ep.y * 2 - etp.y)
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, ep.x, ep.y)
            }

            override fun getMatrix(progress: Float): Matrix {
                curProgress = progress
                pathMeasure.getMatrix(totalDistance * curProgress, orderMatrix,
                        PathMeasure.POSITION_MATRIX_FLAG or PathMeasure.TANGENT_MATRIX_FLAG)
                return orderMatrix
            }

            private fun getPointOnPath(progress: Float): PointF {
                val pos = floatArrayOf(0f, 0f)
                val tan = floatArrayOf(0f, 0f)
                pathMeasure.getPosTan(totalDistance * progress, pos, tan)
                return PointF(pos[0], pos[1])
            }

            override fun getCurProgress() = curProgress

            override fun getTotalDistance() = totalDistance

            override fun getOrderPath() = path

            override fun getCurPointOnPath() = getPointOnPath(curProgress)

            override fun getTargetPoint() = endPoint
        }
    }
}