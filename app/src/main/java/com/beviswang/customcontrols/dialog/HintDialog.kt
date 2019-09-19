package com.beviswang.customcontrols.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import com.beviswang.customcontrols.R
import kotlinx.android.synthetic.main.dialog_hint.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * 提示窗口
 * @author BevisWang
 * @date 2019/7/29 16:47
 */
open class HintDialog(context: Context, @StyleRes themeResId: Int = R.style.HintDialog) : Dialog(context, themeResId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (window == null) return
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val lp = window!!.attributes
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        window!!.attributes = lp
    }

    class Builder(context: Context) {
        private var mLayout: View
        private var mDialog: HintDialog = HintDialog(context, R.style.HintDialog)
        private var mIsCanceledOnTouchOutside: Boolean = true
        private var mIsCancelable: Boolean = true
        private var mRightButtonListener: (HintDialog) -> Unit = {}
        private var mLeftButtonListener: (HintDialog) -> Unit = {}

        init {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mLayout = inflater.inflate(R.layout.dialog_hint, null, false)
            mDialog.addContentView(mLayout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT))
            mLayout.tv_dialog_hint_setting.onClick {
                mRightButtonListener(mDialog)
                mDialog.dismiss()
            }
            mLayout.tv_dialog_hint_cancel.onClick {
                mLeftButtonListener(mDialog)
                mDialog.dismiss()
            }
        }

        /** 设置标题 */
        fun addTitle(title: String): Builder {
            mLayout.tv_dialog_hint_title.text = title
            return this
        }

        /** 设置内容 */
        fun addContent(content: String): Builder {
            mLayout.tv_dialog_hint_content.text = content
            return this
        }

        /** 设置按钮文字及事件 */
        fun addRightButton(btnTitle: String, listener: (HintDialog) -> Unit={}): Builder {
            mLayout.tv_dialog_hint_setting.text = btnTitle
            mRightButtonListener = listener
            return this
        }

        /** 设置取消按钮的显示隐藏，默认隐藏 */
        fun addLeftVisibility(visibility: Int): Builder  {
            mLayout.tv_dialog_hint_cancel.visibility = visibility
            return this
        }

        /** 设置取消按钮 */
        fun addLeftButton(btnTitle: String, listener: (HintDialog) -> Unit={}): Builder {
            mLayout.tv_dialog_hint_cancel.visibility = View.VISIBLE
            mLayout.tv_dialog_hint_cancel.text = btnTitle
            mLeftButtonListener = listener
            return this
        }

        /** 点击屏幕外侧是否关闭 */
        fun addCanceledOnTouchOutside(boolean: Boolean): Builder {
            mIsCanceledOnTouchOutside = boolean
            return this
        }

        /** 是否可关闭 */
        fun addCancelable(boolean: Boolean): Builder {
            mIsCancelable = boolean
            return this
        }

        /** 创建弹窗 */
        fun create(): HintDialog {
            mDialog.setContentView(mLayout)
            mDialog.setCancelable(mIsCancelable)
            mDialog.setCanceledOnTouchOutside(mIsCanceledOnTouchOutside)
            return mDialog
        }

        /** 直接显示 */
        fun show() = create().show()
    }
}