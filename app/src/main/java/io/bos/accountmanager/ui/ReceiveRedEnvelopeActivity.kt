package io.bos.accountmanager.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.EosRedEnvelopePresenter
import io.bos.accountmanager.ui.dialog.SelectAccountDialog
import io.bos.accountmanager.view.EosRedEnvelopeView
import io.starteos.jeos.utils.Base58
import kotlinx.android.synthetic.main.activity_receive_red_envelope.*

/**
 * 领取紅包
 * Created by Administrator on 2019/1/3/003.
 */
@Route(path = Constants.RoutePath.ACTIVITY.RECEIVE_RED_ENVELOPE_ACTIVITY)
class ReceiveRedEnvelopeActivity : AbstractActivity<EosRedEnvelopeView, EosRedEnvelopePresenter>(), EosRedEnvelopeView {
    private var dialogSelect: SelectAccountDialog? = null
    private var positionTag = 0
    private var accountData = ArrayList<AccountTable>()
    private var toast: LoadingDailog? = null

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): EosRedEnvelopeView = this

    override fun byId(): Int {
        return R.layout.activity_receive_red_envelope
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        transparencyBar(this)
        val redText = intent.getStringExtra("redText")
        if (!TextUtils.isEmpty(redText)) {
            red_packet.setText(redText)
        }
        toast = toast(getString(R.string.get_red)).create()
        presenter.getAllAccount()
        send_red_envelope.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.BOS_RED_ENVELOPE_ACTIVITY).navigation()
        }
        create_account.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.NEW_READ_ENVELOPERS_ACTIVITY).navigation()
        }
        go.setOnClickListener {
            val redText = String(Base58.decode(red_packet.text.toString()))

            if (TextUtils.isEmpty(redText)) {
                Toast.makeText(context(), getString(R.string.input_red_envelope_text), Toast.LENGTH_LONG).show()
            } else if (redText.split("^").size != 3) {
                Toast.makeText(context(), getString(R.string.right_format), Toast.LENGTH_LONG).show()
            } else {
                val redTextList = redText.split("^")
                if (accountData.size > 0) {
                    var json = Gson().toJson(accountData)
                    dialogSelect = SelectAccountDialog.newInstance(json, positionTag)
                    dialogSelect?.selectAccountCallback = object : SelectAccountDialog.SelectAccountCallback {
                        override fun onDetermine(select: AccountTable, positionTag: Int) {
                            this@ReceiveRedEnvelopeActivity.positionTag = positionTag
                            if (redTextList[0].toInt() == 3) {
                                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.NEW_READ_ENVELOPERS_ACTIVITY).withString("redText", red_packet.text.toString()).navigation()
                            } else {
                                toast?.show()
                                presenter.get(select.accountName, redTextList[2], redTextList[1].toString().toLong())
                            }
                        }
                    }
                    dialogSelect?.show(supportFragmentManager, "select")
                } else {
                    Toast.makeText(context(), getString(R.string.get_account_error), Toast.LENGTH_LONG).show()
                }
            }
        }
        back.setOnClickListener { finish() }

    }

    /**
     * 获取账户信息成功
     */
    override fun getAllAccountSuccess(list: ArrayList<AccountTable>) {
        super.getAllAccountSuccess(list)
        accountData.clear()
        accountData.addAll(list)
    }

    /**
     * 获取账户信息失败
     */
    override fun getAllAccountFail() {
        super.getAllAccountFail()
        Toast.makeText(context(), getString(R.string.get_account_error), Toast.LENGTH_LONG).show()
    }

    override fun getRedEnvelopeSuccess(quantity: String, memo: String, time: String) {
        super.getRedEnvelopeSuccess(quantity, memo, time)
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        clearClipData()
        Toast.makeText(context(), getString(R.string.get_red_success), Toast.LENGTH_LONG).show()
        val from = splitData(memo, "m", ":")
        val last = memo.substring(memo.length - 1, memo.length)
        val congratulations = splitData(memo, ":", last) + last

        ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RED_RECEIVE_SUCCESS_ACTIVITY).withString("quantity", quantity)
                .withString("time", time)
                .withString("from", from)
                .withString("con", congratulations).navigation()
    }

    /**
     * 领取红包失败
     */
    override fun getRedEnvelopeFail() {
        super.getRedEnvelopeFail()
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        Toast.makeText(context(), getString(R.string.get_red_fail), Toast.LENGTH_LONG).show()

    }

    private fun splitData(str: String, strStart: String, strEnd: String): String {
        var tempStr = ""
        tempStr = str.substring(str.indexOf(strStart) + 1, str.lastIndexOf(strEnd))
        return tempStr
    }

    /**
     * 清空剪切板内容
     */
    private fun clearClipData() {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, "")
        clipboard.primaryClip = clipData
    }

}