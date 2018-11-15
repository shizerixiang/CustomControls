package com.beviswang.customcontrols

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main2.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mWeatherLayout.setOnClickListener { it ->
            doAsync {
                (0..100).forEach { i ->
                    uiThread {
                        mWeatherLayout.setScale(i / 100f)
                    }
                    Thread.sleep(4)
                }
                (0..100).forEach { i ->
                    uiThread {
                        mWeatherLayout.setScale(1 - (i / 100f))
                    }
                    Thread.sleep(4)
                }
            }
        }
    }
}