package io.bos.accountmanager.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.tu.loadingdialog.LoadingDailog
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.AccountListPresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.TipsSureDialog
import io.bos.accountmanager.view.AccountListView
import io.starteos.jeos.crypto.ec.EosPrivateKey
import kotlinx.android.synthetic.main.activity_account_list.*
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * 创建的本地账户列表
 */
@Route(path = Constants.RoutePath.ACTIVITY.ACCOUNT_LIST_ACTIVITY)
class AccountListActivity : AbstractActivity<AccountListView, AccountListPresenter>(), AccountListView {
    private var dataSource: ArrayList<EstablishAccountTable> = ArrayList<EstablishAccountTable>()

    var dialog: LoadingDailog? = null
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    private var accountTables: ArrayList<Pair<String, AccountTable?>> = ArrayList();
    private var listpublic: ArrayList<String> = ArrayList();
    private var newPriavate: EosPrivateKey? = null
    private var password: String = "";
    private var tipsSureDialog: TipsSureDialog? = null


    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): AccountListView {
        return this
    }

    override fun byId(): Int {
        transparencyBar(this@AccountListActivity)
        return R.layout.activity_account_list
    }

    override fun errAccount(message: String) {
        super.errAccount(message)
        dialog?.dismiss()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun deleteSuccess(message: String) {
        super.deleteSuccess(message)
        dialog?.dismiss()
        swipe.isRefreshing = true
        presenter.getEstablishAccount()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    /**
     * 通过公钥获取账号名,并且获取所有的公钥已经权限
     */
    override fun onAccount(account: ArrayList<String>) {

        if (account.size > 0) {
            //获取账户信息成功
            presenter.getLocalAccount(account)
        } else {
            dialog?.dismiss()
            Toast.makeText(this@AccountListActivity, resources.getString(R.string.new_red_envelopes_txt_retry), Toast.LENGTH_LONG).show()
        }
    }

    override fun Accountumber() {
        super.Accountumber()
        dialog?.dismiss()
        pwdView?.decryption(shared.getPwdVerify(), "verify")
    }

    override fun importSuccess() {
        Toast.makeText(this@AccountListActivity, resources.getString(R.string.new_red_envelopes_txt_imotu), Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun importError() {
        dialog?.dismiss()
        Toast.makeText(this@AccountListActivity, resources.getString(R.string.new_red_envelopes_txt_retry), Toast.LENGTH_LONG).show()
    }

    override fun localAccount(accountTables: java.util.ArrayList<Pair<String, AccountTable?>>, date: java.util.ArrayList<String>) {
        dialog?.dismiss()
        this.accountTables.clear()
        this.listpublic.clear()
        this.listpublic.addAll(date)
        this.accountTables.addAll(accountTables)
        presenter.writeDB(accountTables, newPriavate!!, password, listpublic)
    }

    override fun localAccountError(message: String) {
        dialog?.dismiss()
        //获取账户信息失败
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    override fun init() {
        super.init()
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();


        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                dialog?.show()
                password = pwd
                presenter.getAccount(newPriavate?.publicKey.toString())

            }
        }
    }


    override fun getHistoryList(history: List<EstablishAccountTable>) {
        super.getHistoryList(history)
        swipe.isRefreshing = false
        dataSource.clear()
        dataSource.addAll(history)
        adapter.notifyDataSetChanged()
    }


    override fun errLose(message: String) {
        super.errLose(message)
        Toast.makeText(context(), message, Toast.LENGTH_LONG).show()
        swipe.isRefreshing = false


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(this@AccountListActivity)
        recycler.adapter = adapter
        adapter.setEnableLoadMore(false)
        swipe.isRefreshing = true
        presenter.getEstablishAccount()
        swipe.setOnRefreshListener {
            presenter.getEstablishAccount()
        }
    }


    override fun listener() {
        super.listener()
        back.setOnClickListener {
            finish()
        }
    }


    private val adapter = object : BaseQuickAdapter<EstablishAccountTable, BaseViewHolder>(R.layout.item_account_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: EstablishAccountTable?) {
            val item_account_list_name = helper!!.getView<AppCompatTextView>(R.id.item_account_list_name)
            val item_account_list_btn_import = helper!!.getView<AppCompatTextView>(R.id.item_account_list_btn_import)
            val item_account_list_btn_delete = helper!!.getView<AppCompatTextView>(R.id.item_account_list_btn_delete)
            val item_account_list_time = helper!!.getView<AppCompatTextView>(R.id.item_account_list_time)
            item_account_list_name.text = item?.accountName
            //监测导入私钥
            item_account_list_btn_import.setOnClickListener {
                if (NoDoubleClickUtils.isDoubleClick()) {
                    newPriavate = EosPrivateKey(item?.privateKey)


                    dialog?.show()
                    presenter.getIsAccount(item?.accountName!!);
                }

            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val l = java.lang.Long.valueOf(item!!.time)
            var timeString = sdf.format(Date(l))//单位秒
            item_account_list_time.text = timeString
            item_account_list_btn_delete.setOnClickListener {
                if (NoDoubleClickUtils.isDoubleClick()) {
                    tipsSureDialog = TipsSureDialog.newInstance("", "", "")
                    tipsSureDialog?.tipsSureCallback = object : TipsSureDialog.TipsSureCallback {
                        override fun onCancelLeftClick() {
                            tipsSureDialog?.dismiss()
                        }

                        override fun onSureRightClick() {
                            tipsSureDialog?.dismiss()
                            dialog?.show()
                            presenter.deleteAccountName(item?.id!!)
                        }

                    }
                    tipsSureDialog?.show(supportFragmentManager, "AccountList")

                }


            }

        }
    }


    object NoDoubleClickUtils {
        private val SPACE_TIME = 500//2次点击的间隔时间，单位ms
        private var lastClickTime: Long = 0
        fun isDoubleClick(): Boolean {
            val currentTime = System.currentTimeMillis()
            val isClick: Boolean
            if (currentTime - lastClickTime > SPACE_TIME) {
                isClick = true
            } else {
                isClick = false
            }
            lastClickTime = currentTime
            return isClick
        }
    }

}
