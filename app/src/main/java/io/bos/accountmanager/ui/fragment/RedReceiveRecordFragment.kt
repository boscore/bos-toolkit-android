package io.bos.accountmanager.ui.fragment

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.ui.AbstractBosFragment
import kotlinx.android.synthetic.main.activity_red_receive_record_fragment.*

/**
 * 领取记录
 */
@Route(path = Constants.RoutePath.FRAGMENT.RED_RECEIVE_RECORD_FRAGMENT)
class RedReceiveRecordFragment : AbstractBosFragment() {
    private var dataSource: ArrayList<String> = ArrayList<String>()


    override fun fragmentLayout(): Int {
        return R.layout.activity_red_receive_record_fragment
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        adapter.setEnableLoadMore(false)


    }


    private val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_red_record_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: String?) {
//            val item_wallet_click = helper!!.getView<RelativeLayout>(R.id.item_wallet_click)

        }
    }


}
