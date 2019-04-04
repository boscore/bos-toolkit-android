package io.bos.accountmanager.ui

import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.RedEnvelopeRecordBean
import io.bos.accountmanager.net.eosReq.BosTableRedEnvelopeRowReq
import io.bos.accountmanager.presenter.RedRecordPresenter
import io.bos.accountmanager.utils.DateUtil
import io.bos.accountmanager.view.RedRecordView
import kotlinx.android.synthetic.main.activity_red_record.*
import java.util.*

/**
 * 红包记录
 */
@Route(path = Constants.RoutePath.ACTIVITY.RED_RECORD_ACTIVITY)
class RedRecordActivity : AbstractActivity<RedRecordView, RedRecordPresenter>(), RedRecordView {
    private var dataSource: ArrayList<RedEnvelopeRecordBean> = ArrayList()
    private var toast: LoadingDailog? = null

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): RedRecordView {
        return this
    }

    override fun byId(): Int {
        transparencyBar(this@RedRecordActivity)
        return R.layout.activity_red_record

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        toast = toast(getString(R.string.loading)).create()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        try {
            adapter.setEmptyView(R.layout.activity_null_data)
        } catch (e: Exception) {
            adapter.bindToRecyclerView(recycler)
            adapter.setEmptyView(R.layout.activity_null_data)
        }
        val accountName = intent?.getStringExtra("accountName")
        toast?.show()
        presenter.getRedEnvelopeRecord(BosTableRedEnvelopeRowReq(Constants.Const.BOS_CONTRACT, Constants.Const.BOS_CONTRACT, "redpacket", true, accountName!!, "i64", "2"))


        swipe.setOnRefreshListener {
            presenter.getRedEnvelopeRecord(BosTableRedEnvelopeRowReq(Constants.Const.BOS_CONTRACT, Constants.Const.BOS_CONTRACT, "redpacket", true, accountName!!, "i64", "2"))
        }
        back.setOnClickListener {
            finish()
        }

    }

    private val adapter = object : BaseQuickAdapter<RedEnvelopeRecordBean, BaseViewHolder>(R.layout.item_rad_record_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: RedEnvelopeRecordBean?) {
            val redEnvelopeCount = helper?.getView<AppCompatTextView>(R.id.item_red_record_num)
            val date = helper?.getView<AppCompatTextView>(R.id.item_red_record_time)
            helper?.setText(R.id.item_red_record_money, item?.amount)
            val d = DateUtil.dd((item?.expire!! - 86400).toString())
            date?.text = String.format(Locale.CHINESE, "%s", d)
            redEnvelopeCount?.text = String.format(Locale.CHINESE, getString(R.string.already_get), item.claims?.size, item.count)


        }
    }

    override fun getRedEnvelopeListSuccess(list: ArrayList<RedEnvelopeRecordBean>) {
        super.getRedEnvelopeListSuccess(list)
        if (swipe.isRefreshing) {
            swipe.isRefreshing = false
        }
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        dataSource.clear()
        dataSource.addAll(list)
        adapter.notifyDataSetChanged()
    }

    override fun getRedEnvelopeListFail() {
        super.getRedEnvelopeListFail()
        if (swipe.isRefreshing) {
            swipe.isRefreshing = false
        }
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        Toast.makeText(context(), getString(R.string.get_red_record_fail), Toast.LENGTH_LONG).show()
    }


}
