package com.beviswang.customcontrols.util

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.widget.ImageView

/**
 * 过渡动画辅助工具
 * @author BevisWang
 * @date 2019/9/4 15:27
 */
object TransitionHelper {
    /** 修改颜色动画 */
    fun changeBgColorAnimator(fromColor: Int, toColor: Int, view: View) {
//        val animator = ValueAnimator.ofInt(fromColor, toColor)
        val animator = ValueAnimator.ofArgb(fromColor, toColor)
        animator.duration = 240
        animator.setEvaluator(ArgbEvaluator())
        animator.addUpdateListener { animation -> view.setBackgroundColor(animation.animatedValue as Int) }
        animator.start()
    }

    /** 修改颜色动画 */
    fun changeImageTintColorAnimator(fromColor: Int, toColor: Int, view: ImageView) {
//        val animator = ValueAnimator.ofInt(fromColor, toColor)
        val animator = ValueAnimator.ofArgb(fromColor, toColor)
        animator.duration = 240
        animator.setEvaluator(ArgbEvaluator())
        animator.addUpdateListener { animation -> view.setColorFilter(animation.animatedValue as Int) }
        animator.start()
    }
}