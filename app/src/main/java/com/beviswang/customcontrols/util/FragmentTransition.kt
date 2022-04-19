package com.beviswang.customcontrols.util

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.beviswang.customcontrols.R

/**
// 这里指定了共享的视图元素
ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), shareView, "image");
ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
 */
fun <T : Fragment> AppCompatActivity.displayFragment(
    @IdRes targetId: Int,
    fragmentName: Class<T>,
    view: View
) {
    var fragment = supportFragmentManager.findFragmentByTag(fragmentName.name)
    if (fragment == null) fragment = fragmentName.newInstance()
    supportFragmentManager.beginTransaction()
//        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .setCustomAnimations(R.animator.animator_fade_in, R.animator.animator_fade_out)
        .addSharedElement(view, ViewCompat.getTransitionName(view) ?: "")
        .addToBackStack(this::class.java.name)
        .replace(targetId, fragment!!)
        .commit()
}

/**
// 这里指定了共享的视图元素
ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), shareView, "image");
ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
 */
fun <T : Fragment> Fragment.displayFragment(
    @IdRes targetId: Int,
    fragmentName: Class<T>,
    view: View
) {
    val fm = fragmentManager ?: return
    var fragment = fm.findFragmentByTag(fragmentName.name)
    if (fragment == null) fragment = fragmentName.newInstance()
    fm.beginTransaction()
//        .setCustomAnimations(R.animator.animator_fade_in, R.animator.animator_fade_out)
        .addSharedElement(view, ViewCompat.getTransitionName(view) ?: "")
        .addToBackStack(this::class.java.name)
        .replace(targetId, fragment!!)
        .commit()
}