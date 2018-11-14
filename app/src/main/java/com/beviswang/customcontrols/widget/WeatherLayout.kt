package com.beviswang.customcontrols.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class WeatherLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, def: Int = 0)
    : RelativeLayout(context, attrs, def) {
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }
}