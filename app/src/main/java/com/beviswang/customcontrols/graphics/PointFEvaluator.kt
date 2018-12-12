package com.beviswang.customcontrols.graphics

import android.animation.TypeEvaluator
import android.graphics.PointF

/**
 * This evaluator can be used to perform type interpolation between `PointF` values.
 */
class PointFEvaluator : TypeEvaluator<PointF> {

    /**
     * When null, a new PointF is returned on every evaluate call. When non-null,
     * mPoint will be modified and returned on every evaluate.
     */
    private var mPoint: PointF? = null

    /**
     * Construct a PointFEvaluator that returns a new PointF on every evaluate call.
     * To avoid creating an object for each evaluate call,
     * [PointFEvaluator] should be used
     * whenever possible.
     */
    constructor() {}

    /**
     * Constructs a PointFEvaluator that modifies and returns `reuse`
     * in [.evaluate] calls.
     * The value returned from
     * [.evaluate] should
     * not be cached because it will change over time as the object is reused on each
     * call.
     *
     * @param reuse A PointF to be modified and returned by evaluate.
     */
    constructor(reuse: PointF) {
        mPoint = reuse
    }

    /**
     * This function returns the result of linearly interpolating the start and
     * end PointF values, with `fraction` representing the proportion
     * between the start and end values. The calculation is a simple parametric
     * calculation on each of the separate components in the PointF objects
     * (x, y).
     *
     *
     * If [.PointFEvaluator] was used to construct
     * this PointFEvaluator, the object returned will be the `reuse`
     * passed into the constructor.
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue The start PointF
     * @param endValue   The end PointF
     * @return A linear interpolation between the start and end values, given the
     * `fraction` parameter.
     */
    override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF? {
        val x = startValue.x + fraction * (endValue.x - startValue.x)
        val y = startValue.y + fraction * (endValue.y - startValue.y)

        return if (mPoint != null) {
            mPoint?.set(x, y)
            mPoint
        } else {
            PointF(x, y)
        }
    }
}