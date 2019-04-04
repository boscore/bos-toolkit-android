package io.bos.accountmanager.ui.fragment

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.ui.AbstractBosFragment
import kotlinx.android.synthetic.main.activity_red_send_out_record_fragment.*

/**
 * 红包发送记录
 */
@Route(path = Constants.RoutePath.FRAGMENT.RED_SEND_OUT_RECORD_FRAGMENT)
class RedSendOutRecordFragment : AbstractBosFragment() {
    private var dataSource: ArrayList<String> = ArrayList<String>()


    override fun fragmentLayout(): Int {
        return R.layout.activity_red_send_out_record_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter.setEnableLoadMore(false)


    }
//

    private val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_red_record_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: String?) {
            val item_red_record_type = helper!!.getView<AppCompatTextView>(R.id.item_red_record_type)
            item_red_record_type.setTextColor(resources.getColor(R.color.colorPrimaryDark))
            item_red_record_type.text = "-"
        }
    }


}
