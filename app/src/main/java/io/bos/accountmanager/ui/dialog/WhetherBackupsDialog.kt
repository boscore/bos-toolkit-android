package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable

import kotlinx.android.synthetic.main.dialog_confirm_backup.*

/**
 * 提示是否备份
 */
class WhetherBackupsDialog : DialogFragment() {
    private var dateList = ArrayList<AccountTable>()

    var whetherBackupsCallback: WhetherBackupsCallback? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_confirm_backup, container, false)
    }

    interface WhetherBackupsCallback {
        //立即备份
        fun onImmediately()

        //已经备份
        fun onAlreadyBackups()

    }

    companion object {
        fun newInstance(date: String): WhetherBackupsDialog {
            val fragment = WhetherBackupsDialog()
            val bundle = Bundle()
            bundle.putString("date", date)
            fragment.arguments = bundle
            return fragment
        }


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = arguments?.getString("date")
//
        dateList.clear()
        dateList.addAll(Gson().fromJson<List<AccountTable>>(data, object : TypeToken<List<AccountTable>>() {}.type))
        var cont: StringBuffer = StringBuffer()
        cont.append(resources.getString(R.string.dialog_whether_txt_content))
        for (i in 0 until dateList.size) {
            if (i < dateList.size - 1) {
                cont.append(dateList[i].accountName + "、")
            } else {
                cont.append(dateList[i].accountName)
            }

        }
        confirm_content.text = cont.toString()

        confirm_btn_close.setOnClickListener {
            dismiss()
        }
        //已经备份
        confirm_btn_sure.setOnClickListener {
            whetherBackupsCallback?.onAlreadyBackups()
            dismiss()
        }
        //立即备份
        confirm_btn_immediately.setOnClickListener {
            whetherBackupsCallback?.onImmediately()
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