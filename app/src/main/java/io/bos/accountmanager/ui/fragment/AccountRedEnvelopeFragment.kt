package io.bos.accountmanager.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.RedEnvelopeCallback
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.ui.AbstractBosFragment
import io.bos.accountmanager.ui.dialog.SelectAccountDialog
import kotlinx.android.synthetic.main.fragment_red_envelope_account.*

/**
 * 账号红包fragment
 * Created by Administrator on 2019/1/2/002.
 */
@Route(path = Constants.RoutePath.FRAGMENT.RED_ENVELOPE_ACCOUNT_FRAGMENT)
class AccountRedEnvelopeFragment : AbstractBosFragment() {

    private var positionTag = 0

    public var accountData: ArrayList<AccountTable> = ArrayList()
    private var dialogSelect: SelectAccountDialog? = null
    public var callBack: RedEnvelopeCallback? = null
    private var accountTable: AccountTable? = null

    override fun fragmentLayout(): Int {
        return R.layout.fragment_red_envelope_account
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var json = Gson().toJson(accountData)

        select_account.setOnClickListener {
            dialogSelect = SelectAccountDialog.newInstance(json, positionTag)
            dialogSelect?.selectAccountCallback = object : SelectAccountDialog.SelectAccountCallback {
                override fun onDetermine(select: AccountTable, positionTag: Int) {
                    this@AccountRedEnvelopeFragment.positionTag = positionTag
                    select_account_text.text = select.accountName
                    accountTable = select
                }
            }
            dialogSelect?.show(fragmentManager, "select")
        }

        send.setOnClickListener {
            val amount = amount.text.toString()
            val account = select_account_text.text.toString()
            val count = count.text.toString()
            when {
                TextUtils.equals(getString(R.string.choose_pay_account), account) ->
                    Toast.makeText(context, getString(R.string.account_not_empty), Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(amount) ->
                    Toast.makeText(context, getString(R.string.amount_not_empty), Toast.LENGTH_SHORT).show()
                TextUtils.isEmpty(count) ->
                    Toast.makeText(context, getString(R.string.count_not_empty), Toast.LENGTH_SHORT).show()
                amount.toDouble() < 0.1 ->
                    Toast.makeText(context, getString(R.string.red_amount_limit), Toast.LENGTH_SHORT).show()
                count.toInt() > 100 ->
                    Toast.makeText(context, getString(R.string.red_count_limit), Toast.LENGTH_SHORT).show()
                else ->
                    callBack?.accountRedEnvelope(accountTable!!, amount, 3, count.toInt(), congratulations.text.toString())

            }
        }


    }
}