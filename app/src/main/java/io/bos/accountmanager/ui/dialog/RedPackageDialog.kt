package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import io.bos.accountmanager.R
import kotlinx.android.synthetic.main.dialog_receive_red.*

/**
 * 领取红包弹出框
 */
class RedPackageDialog : DialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_receive_red, container, false)
    }

    var getRedEnvelopeCallback: GetRedEnvelopeCallback? = null

    interface GetRedEnvelopeCallback {
        fun get(redText: String)
    }

    companion object {
        fun newInstance(date: String): RedPackageDialog {
            val fragment = RedPackageDialog()
            val bundle = Bundle()
            bundle.putString("data", date)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = arguments?.getString("data")

        dialog_receive_content.text = data

        dialog_receive_delete.setOnClickListener {
            dismiss()
        }
        dialog_receive_btn_sure.setOnClickListener {
            getRedEnvelopeCallback?.get(data!!)
            dismiss()
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

}
