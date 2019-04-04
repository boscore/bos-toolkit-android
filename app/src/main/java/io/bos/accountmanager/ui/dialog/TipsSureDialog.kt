package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.*
import io.bos.accountmanager.R
import kotlinx.android.synthetic.main.dialog_tips_sure.*


class TipsSureDialog : DialogFragment() {
    var tipsSureCallback: TipsSureCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_tips_sure, container, false)
    }


    interface TipsSureCallback {
        //取消
        fun onCancelLeftClick()

        //确定
        fun onSureRightClick()

    }

    companion object {
        fun newInstance(content: String, cancel: String, sure: String): TipsSureDialog {
            val fragment = TipsSureDialog()
            val bundle = Bundle()
            bundle.putString("content", content)
            bundle.putString("cancel", cancel)
            bundle.putString("sure", sure)
            fragment.arguments = bundle
            return fragment
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var content = arguments?.getString("content")
        var cancel = arguments?.getString("cancel")
        //确定
        var sure = arguments?.getString("sure")
        if (!TextUtils.isEmpty(content)) {
            dialog_tips_content.text = content
        }

        if (!TextUtils.isEmpty(cancel)) {
            dialog_tips_cancel.text = cancel
        }

        if (!TextUtils.isEmpty(sure)) {
            dialog_tips_sure.text = sure
        }
        //qu
        dialog_tips_cancel.setOnClickListener {
            tipsSureCallback?.onCancelLeftClick()
        }

        dialog_tips_sure.setOnClickListener {
            tipsSureCallback?.onSureRightClick()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }


}