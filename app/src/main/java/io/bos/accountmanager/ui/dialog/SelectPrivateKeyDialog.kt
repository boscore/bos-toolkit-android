package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.bos.accountmanager.R
import io.bos.accountmanager.net.bean.SecretKeyBean
import kotlinx.android.synthetic.main.dialog_select_export_key.*

/**
 * 选择导出私钥的弹出框
 */
class SelectPrivateKeyDialog : DialogFragment() {
    private var selectNum: Int? = 0
    private var dateList: ArrayList<SecretKeyBean> = ArrayList<SecretKeyBean>()

    interface SelectPrivateKeyCallback {
        fun onDetermine(select: Int)

    }

    var selectPrivateKeyCallback: SelectPrivateKeyCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_select_export_key, container, false)
    }


    companion object {
        fun newInstance(date: String): SelectPrivateKeyDialog {
            val fragment = SelectPrivateKeyDialog()
            val bundle = Bundle()
            bundle.putString("date", date)
            fragment.arguments = bundle
            return fragment
        }


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var date = arguments?.getString("date")
        dateList.clear()
        var list = Gson().fromJson<List<SecretKeyBean>>(date, object : TypeToken<List<SecretKeyBean>>() {}.type)
        dateList.addAll(list)
        selectNum = 0
        select_export_recycler.layoutManager = LinearLayoutManager(context)
        select_export_recycler.adapter = adapter
        adapter.notifyDataSetChanged()
        select_export_delete.setOnClickListener {
            dismiss()

        }
        select_export_btn.setOnClickListener {

            selectPrivateKeyCallback?.onDetermine(selectNum!!)
        }


    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    private val adapter = object : BaseQuickAdapter<SecretKeyBean, BaseViewHolder>(R.layout.item_dialog_select_key_list, dateList) {
        override fun convert(helper: BaseViewHolder?, item: SecretKeyBean?) {
            val item_select_key = helper?.getView<TextView>(R.id.item_select_key)
            val item_select_click = helper?.getView<RelativeLayout>(R.id.item_select_click)
            val item_select_image = helper?.getView<ImageView>(R.id.item_select_image)
            val item_select_type = helper?.getView<TextView>(R.id.item_select_type)

            item_select_key?.text = getStarString(item?.publicKey.toString(), 4, 8)
            if (!TextUtils.isEmpty(item?.accountName)) {
                item_select_type?.visibility = View.VISIBLE

                item_select_type?.text = item?.accountName
            } else {
                item_select_type?.visibility = View.GONE
            }

            if (selectNum == helper?.adapterPosition) {
                item_select_image?.visibility = View.VISIBLE
            } else {
                item_select_image?.visibility = View.GONE
            }
            item_select_click?.setOnClickListener {
                selectNum = helper.adapterPosition
                refresh()


            }

        }


    }

    fun refresh() {
        adapter.notifyDataSetChanged()
    }

    private fun getStarString(content: String, begin: Int, end: Int): String {

        if (begin >= content.length || begin < 0) {
            return content
        }
        if (end >= content.length || end < 0) {
            return content
        }
        if (begin >= end) {
            return content
        }
        var starStr = ""
        for (i in begin until end) {
            starStr = "$starStr*"
        }
        return content.substring(0, begin) + starStr + content.substring(end, content.length)

    }
}
