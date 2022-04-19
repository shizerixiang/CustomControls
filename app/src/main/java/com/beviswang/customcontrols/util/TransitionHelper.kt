package com.beviswang.customcontrols.util

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.AnimRes
import com.beviswang.customcontrols.R

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

    /**
     * 播放布局动画
     * @param animId 动画 XML 文件
     */
    fun ViewGroup.startLayoutAnimator(
        @AnimRes animId: Int,
        start: () -> Unit = {},
        end: () -> Unit = {}
    ) {
        layoutAnimation = AnimationUtils.loadLayoutAnimation(context, animId)
        layoutAnimationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = start()

            override fun onAnimationEnd(animation: Animation?) = end()

            override fun onAnimationRepeat(animation: Animation?) {
            }
        }
        startLayoutAnimation()
    }
}