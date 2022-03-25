package com.beviswang.customcontrols.graphics.path

import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF

/**
 * View 路径工具
 * @author BevisWong
 * @date 2022/3/18
 */
class ViewPath(var path: Path) {
    private var mPathMeasure: PathMeasure? = null
    private var mDistance: Float = 0f
    private var mCurPoint: PointF = PointF()
    private var mProgress: Float = 0f
    private var mTan: FloatArray = floatArrayOf()
    private var mPos: FloatArray = floatArrayOf()

    init {
        measurePath()
    }

    /**
     * 修改路径后，需要调用该方法重新测量
     */
    fun measurePath(){
        mPathMeasure = PathMeasure(path, false)
        mDistance = mPathMeasure!!.length
    }

    fun getPathPoint(progress: Float): PointF {
        mProgress = progress
        mTan = floatArrayOf(0f, 0f)
        mPos = floatArrayOf(0f, 0f)
        mPathMeasure?.getPosTan(mDistance * mProgress, mPos, mTan)
        mCurPoint.set(mPos[0], mPos[1])
        return PointF(mPos[0], mPos[1])
    }
}