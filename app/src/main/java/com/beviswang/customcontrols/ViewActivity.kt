package com.beviswang.customcontrols

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        getPackageViewClass("com.beviswang.customcontrols.widget")
    }

    /**
     * 获取某个包下的自定义 View 控件
     * @param packageName 自定义控件包的包名（）
     */
    private fun getPackageViewClass(packageName: String): ArrayList<Class<*>> {
        val clazzArray = ArrayList<Class<*>>()
        try {
            val enumerations = classLoader.getResources(packageName)
            Log.e("1","URL：$packageName")
            enumerations.toList().forEach{
                Log.e("1","URL：$it")
            }
//            clazzArray.addAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return clazzArray
    }
}
