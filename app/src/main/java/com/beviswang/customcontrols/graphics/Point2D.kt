package com.beviswang.customcontrols.graphics

import android.graphics.PointF

class Point2D constructor() : PointF() {
    var positionArray: FloatArray = floatArrayOf(0f, 0f)
    var x: Float = positionArray[0]
    var y: Float = positionArray[1]

    constructor(x: Float, y: Float) : this() {
        positionArray[0] = x
        positionArray[1] = y
    }

    constructor(src: PointF) : this() {
        positionArray[0] = src.x
        positionArray[1] = src.y
    }
}