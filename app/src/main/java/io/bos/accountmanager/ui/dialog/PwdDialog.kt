package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import android.widget.Toast
import io.bos.accountmanager.R
import kotlinx.android.synthetic.main.dialog_input_password_frame.*

class PwdDialog : DialogFragment() {

    interface PwdCallback {
        fun onPwd(pwd: String)
        fun onTip(): String
    }

    var pwdCallback: PwdCallback? = null
    var hintDialogListener: PwdHintDialogListener? = null


    fun pwdError(text: String) {
        Toast.makeText(context, resources.getString(R.string.dialog_pwd_err_txt), Toast.LENGTH_LONG).show()
    }

    fun pwdSuccess() {

    }

    fun pwdStart() {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_input_password_frame, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        input_pwd_btn.setOnClickListener {
            pwdCallback?.onPwd(pwd.text.toString())
            dismiss()
        }
        input_pwd_tips.setOnClickListener {
            Toast.makeText(context, if (pwdCallback != null) pwdCallback!!.onTip() else "", Toast.LENGTH_LONG).show()
        }
        close_btn.setOnClickListener {
            pwd.setText("")
            dismiss()
            hintDialogListener?.hint()
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

    interface PwdHintDialogListener {
        fun hint()
    }
}