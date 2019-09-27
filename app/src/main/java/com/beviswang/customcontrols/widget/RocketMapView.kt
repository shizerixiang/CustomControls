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
import com.beviswang.customcontrols.graphics.PointHelper
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
    private var mRocketSize: Int = 0
    private var mRocketRes: Int = 0

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RocketMapView)
        mLineColor = typedArray.getColor(R.styleable.RocketMapView_colorLine, mLineColor)
        mRocketSize = typedArray.getDimensionPixelSize(R.styleable.RocketMapView_flyImageSize, ViewHelper.dip2px(context, 42f).toInt())
        mRocketRes = typedArray.getResourceId(R.styleable.RocketMapView_flyImage, R.mipmap.ic_rocket)
        typedArray.recycle()

        mRocketList = ArrayList()
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
    }

    fun setSpeed(speed: Float) {
        mRocketList.forEach { it.setSpeed(speed) }
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
        mRocketList.forEach { it.draw(canvas) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
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
        mRocketList.forEach { it.destroy() }
    }

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

        private var isInsertOrder: Boolean = false

        private var pathPaint: Paint

        private var mFlySpeed: Float = 2f // 火箭飞行速度 px/ms

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

        /** 设置飞行速度 */
        fun setSpeed(speed: Float) {
            mFlySpeed = speed
            flyAuto()
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
            normalOrderAnimator?.duration = (normalOrder.getTotalDistance() / mFlySpeed).toLong()
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
                    // S 式路径
//                    etp.x = target.x + (stp.x - sp.x)
//                    etp.y = target.y + (stp.y - sp.y)
                    urgentOrder = UrgentOrder(sp, target, etp, stp)
                }
                ROCKET_STATE_URGENT_ORDER -> {
                    if (state == ROCKET_STATE_URGENT_ORDER || state == ROCKET_STATE_NORMAL_ORDER_RETURN)
                        isInsertOrder = true
                    val sp = urgentOrder!!.getCurPointOnPath()
                    val stp = urgentOrder!!.getTargetPoint()
                    val etp = normalOrder.getCurPointOnPath()
                    urgentOrder = UrgentOrder(sp, target, etp, stp)
                }
                ROCKET_STATE_NORMAL_ORDER_RETURN -> {
                    val sp = urgentOrder!!.getCurPointOnPath()
                    val etp = normalOrder.getTargetPoint()
                    // 中途有未完成指令
                    if (isInsertOrder) {
                        urgentOrder = UrgentOrder(sp, target, etp)
                        isInsertOrder = false
                        return
                    }
                    val stp = PointF()
                    // C 式
                    stp.x = sp.x - (etp.x - target.x)
                    stp.y = sp.y - (etp.y - target.y)
                    // S 式路径
//                    stp.x = sp.x + (etp.x - target.x)
//                    stp.y = sp.y + (etp.y - target.y)
                    urgentOrder = UrgentOrder(sp, target, etp, stp)
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
            urgentOrderAnimator?.duration = (urgentOrder!!.getTotalDistance() / mFlySpeed).toLong()
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

            private val bufferFactor: Float = 200f                          // 缓冲因子，即：在只确定方向的情况下，转向该方向所损耗的路程（根据固定的减速度，从当前速度到0时所消耗的路程长度）

            var mTanPath: Path = Path()

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
                val tp = PointHelper.getVectorPointInCircle(o, p, bufferFactor, true)
                mTanPath = Path()
                mTanPath.moveTo(0f, 0f)
                mTanPath.lineTo(p.x, p.y)
                mTanPath.lineTo(tp.x, tp.y)
                return tp
            }

            private fun getPointOnPath(progress: Float): PointF {
                val pos = floatArrayOf(0f, 0f)
                val tan = floatArrayOf(0f, 0f)
                pathMeasure.getPosTan(totalDistance * progress, pos, tan)
                val degrees: Float = (Math.atan2(tan[1].toDouble(), tan[0].toDouble()) * 180f / Math.PI).toFloat()
                Log.e("点的角度", "degrees=$degrees")
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