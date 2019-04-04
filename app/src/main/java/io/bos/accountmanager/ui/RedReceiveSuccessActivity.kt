package io.bos.accountmanager.ui

import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.EosRedEnvelopePresenter
import io.bos.accountmanager.ui.dialog.SelectAccountDialog
import io.bos.accountmanager.utils.DateConversion
import io.bos.accountmanager.view.EosRedEnvelopeView
import kotlinx.android.synthetic.main.activity_red_receive_success.*
import java.util.*


/**
 * 红包领取成功界面
 */
@Route(path = Constants.RoutePath.ACTIVITY.RED_RECEIVE_SUCCESS_ACTIVITY)
class RedReceiveSuccessActivity : AbstractActivity<EosRedEnvelopeView, EosRedEnvelopePresenter>(), EosRedEnvelopeView {
    override fun byId(): Int {
        return R.layout.activity_red_receive_success
    }

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): EosRedEnvelopeView = this

    private var dialogSelect: SelectAccountDialog? = null
    private var accountData = ArrayList<AccountTable>()
    private var positionTag = 0
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparencyBar(this@RedReceiveSuccessActivity)
        ARouter.getInstance().inject(this)
        val quantity = intent.getStringExtra("quantity")
        red_success_money.text = quantity.substring(0, quantity.length - 3)
        quantity_text.text = quantity.substring(0, quantity.length - 3)
        back.setOnClickListener {
            finish()
        }
        presenter.getAllAccount()
        red_success_txt_launch.text = "From" + intent.getStringExtra("from")

        red_success_txt_time.text = DateConversion.converTime(intent.getStringExtra("time"), TimeZone.getTimeZone("GMT+8"))
        red_success_txt_auspicious.text = intent.getStringExtra("con")

        red_success_my.setOnClickListener {
            var json = Gson().toJson(accountData)
            dialogSelect = SelectAccountDialog.newInstance(json, positionTag)
            dialogSelect?.selectAccountCallback = object : SelectAccountDialog.SelectAccountCallback {
                override fun onDetermine(select: AccountTable, positionTag: Int) {
                    this@RedReceiveSuccessActivity.positionTag = positionTag
                    ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RED_RECORD_ACTIVITY).withString("accountName", select.accountName).navigation()
                }
            }
            dialogSelect?.show(supportFragmentManager, "select")
        }


    }

    /**
     * 获取所有账户列表
     */
    override fun getAllAccountSuccess(list: ArrayList<AccountTable>) {
        super.getAllAccountSuccess(list)

        accountData.clear()
        accountData.addAll(list)
    }


}
