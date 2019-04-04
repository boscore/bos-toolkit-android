package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import io.bos.accountmanager.R
import kotlinx.android.synthetic.main.dialog_input_password_frame.*

class PwdDialogCloud : DialogFragment() {

    interface PwdCallback {
        fun onPwd(pwd: String)
        fun onDismiss() {}
    }

    var pwdCallback: PwdCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_input_password_frame_cloud, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable=false
        input_pwd_btn.setOnClickListener {
            pwdCallback?.onPwd(pwd.text.toString())
            dismiss()
        }
        close_btn.setOnClickListener {
            pwd.setText("")
            dismiss()
            pwdCallback?.onDismiss()
        }
    }

    override fun onPause() {
        pwd.setText("")
        super.onPause()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }
}