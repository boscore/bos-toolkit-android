package io.bos.accountmanager.ui.dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import kotlinx.android.synthetic.main.dialog_select_export_key.*

/**
 *红包创建选择账户弹出框
 * Created by Administrator on 2019/1/4/004.
 */
class SelectAccountDialog : DialogFragment() {
    private var dateList = ArrayList<AccountTable>()
    var selectAccountCallback: SelectAccountCallback? = null
    private var positionTag = 0

    interface SelectAccountCallback {
        fun onDetermine(select: AccountTable, positionTag: Int)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_account_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        select_export_recycler.layoutManager = LinearLayoutManager(context)
        select_export_recycler.adapter = adapter
        title.text = getString(R.string.choose_account)
        select_export_delete.setOnClickListener {
            dismiss()
        }
        select_export_btn.setBackgroundResource(R.drawable.red_envelope_btn_drawable)
        val data = arguments?.getString("data")
        dateList.clear()
        dateList.addAll(Gson().fromJson<List<AccountTable>>(data, object : TypeToken<List<AccountTable>>() {}.type))
        adapter.notifyDataSetChanged()
        select_export_btn.setOnClickListener {
            selectAccountCallback?.onDetermine(dateList[positionTag], positionTag)
            dismiss()

        }
        positionTag = arguments?.getInt("positionTag")!!

    }

    companion object {
        fun newInstance(data: String, positionTag: Int): SelectAccountDialog {
            val fragment = SelectAccountDialog()
            val bundle = Bundle()
            bundle.putString("data", data)
            bundle.putInt("positionTag", positionTag)
            fragment.arguments = bundle
            return fragment
        }


    }


    private val adapter = object : BaseQuickAdapter<AccountTable, BaseViewHolder>(R.layout.item_dialog_select_key_list, dateList) {
        override fun convert(helper: BaseViewHolder?, item: AccountTable?) {
            val itemSelectAccount = helper?.getView<TextView>(R.id.item_select_key)
            val itemSelectClick = helper?.getView<RelativeLayout>(R.id.item_select_click)
            val itemSelectImage = helper?.getView<ImageView>(R.id.item_select_image)
            val itemSelectType = helper?.getView<AppCompatTextView>(R.id.item_select_type)

            itemSelectAccount?.text = item?.accountName
            if (helper?.adapterPosition == positionTag) {
                itemSelectImage?.visibility = View.VISIBLE
            } else {
                itemSelectImage?.visibility = View.GONE
            }
            itemSelectType?.visibility = View.GONE
            itemSelectClick?.setOnClickListener {
                positionTag = helper?.adapterPosition
                notifyDataSetChanged()
            }

        }


    }

}