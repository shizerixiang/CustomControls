package com.beviswang.customcontrols.graphics.evaluator

import android.animation.TypeEvaluator

class ColorEvaluator: TypeEvaluator<Int> {
    companion object{
        private val sInstance = ColorEvaluator()

        fun getInstance(): ColorEvaluator {
            return sInstance
        }
    }

    override fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
        val startInt = startValue
        val startA = (startInt shr 24 and 255).toFloat() / 255.0f
        var startR = (startInt shr 16 and 255).toFloat() / 255.0f
        var startG = (startInt shr 8 and 255).toFloat() / 255.0f
        var startB = (startInt and 255).toFloat() / 255.0f
        val endInt = endValue
        val endA = (endInt shr 24 and 255).toFloat() / 255.0f
        var endR = (endInt shr 16 and 255).toFloat() / 255.0f
        var endG = (endInt shr 8 and 255).toFloat() / 255.0f
        var endB = (endInt and 255).toFloat() / 255.0f
        startR = Math.pow(startR.toDouble(), 2.2).toFloat()
        startG = Math.pow(startG.toDouble(), 2.2).toFloat()
        startB = Math.pow(startB.toDouble(), 2.2).toFloat()
        endR = Math.pow(endR.toDouble(), 2.2).toFloat()
        endG = Math.pow(endG.toDouble(), 2.2).toFloat()
        endB = Math.pow(endB.toDouble(), 2.2).toFloat()
        var a = startA + fraction * (endA - startA)
        var r = startR + fraction * (endR - startR)
        var g = startG + fraction * (endG - startG)
        var b = startB + fraction * (endB - startB)
        a *= 255.0f
        r = Math.pow(r.toDouble(), 0.45454545454545453).toFloat() * 255.0f
        g = Math.pow(g.toDouble(), 0.45454545454545453).toFloat() * 255.0f
        b = Math.pow(b.toDouble(), 0.45454545454545453).toFloat() * 255.0f
        return Math.round(a) shl 24 or (Math.round(r) shl 16) or (Math.round(g) shl 8) or Math.round(
            b
        )
    }
}