package io.bos.accountmanager.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.Constants.*
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.RedEnvelopeCallback
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.PermissionBean
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.EosRedEnvelopePresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.SelectAccountDialog
import io.bos.accountmanager.ui.fragment.AccountRedEnvelopeFragment
import io.bos.accountmanager.ui.fragment.OrdinaryRedEnvelopeFragment
import io.bos.accountmanager.ui.fragment.RandomRedEnvelopeFramgent
import io.bos.accountmanager.view.EosRedEnvelopeView
import io.starteos.jeos.utils.Base58
import kotlinx.android.synthetic.main.activity_eos_red_envelope.*
import org.json.JSONArray
import java.math.BigDecimal
import kotlin.collections.ArrayList

/**
 * EOS红包
 * Created by Administrator on 2018/12/26/026.
 */
@Route(path = RoutePath.ACTIVITY.BOS_RED_ENVELOPE_ACTIVITY)
class BosRedEnvelopeActivity : AbstractActivity<EosRedEnvelopeView, EosRedEnvelopePresenter>(), EosRedEnvelopeView, RedEnvelopeCallback {

    private lateinit var accountRedEnvelopeFragment: AccountRedEnvelopeFragment
    private lateinit var ordinaryRedEnvelopeFragment: OrdinaryRedEnvelopeFragment
    private lateinit var randomRedEnvelopeFragment: RandomRedEnvelopeFramgent
    private var dialogSelect: SelectAccountDialog? = null
    private var accountData = ArrayList<AccountTable>()

    private var fragments = ArrayList<AbstractBosFragment>()
    private var title: Array<String>? = null
    private var pagerAdapter: Adapter? = null
    private var pwdView: PwdView? = null
    private var toast: LoadingDailog? = null
    private var positionTag = 0
    /**
     * 记录active权限公钥
     */
    private var publickeyActive = ""

    /**
     * 账号权限集合
     */
    private var authList = ArrayList<PermissionsBean>()
    private lateinit var shared: PreferencesHelper
    /**
     * 账户信息对象
     */
    private var accountTable: AccountTable? = null

    /**
     * amount : 红包金额
     */
    private var amount: String? = null
    /**
     * redEnvelopeType : 红包类型
     * 1.普通红包
     * 2.随机红包
     * 3.仅供创建账号的红包
     */
    private var redEnvelopeType = 3
    /**
     * count : 红包数量
     */
    private var count = 0
    /**
     * congratulations ：红包祝贺语
     */
    private var congratulations = ""


    override fun attachView(): EosRedEnvelopeView = this

    override fun byId(): Int {
        return R.layout.activity_eos_red_envelope
    }

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparencyBar(this)
        toast = toast(getString(R.string.creating_red_envelope)).create()
        title = arrayOf(getString(R.string.account_envelope), resources.getString(R.string.red_ordinary_red), getString(R.string.random_envelope))
        accountRedEnvelopeFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.RED_ENVELOPE_ACCOUNT_FRAGMENT).navigation() as AccountRedEnvelopeFragment
        ordinaryRedEnvelopeFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.RED_ENVELOPE_ORDINARY_FRAGMENT).navigation() as OrdinaryRedEnvelopeFragment
        randomRedEnvelopeFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.RED_ENVELOPE_RANDOM_FRAGMENT).navigation() as RandomRedEnvelopeFramgent
        presenter.getAllAccount()
        fragments.add(accountRedEnvelopeFragment)
        fragments.add(ordinaryRedEnvelopeFragment)
        fragments.add(randomRedEnvelopeFragment)
        pagerAdapter = Adapter(supportFragmentManager)
        pager.adapter = pagerAdapter
        tab.setupWithViewPager(pager)
        back.setOnClickListener {
            finish()
        }


        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                presenter.exportPrivateKey(pwd, accountTable!!)
            }
        }
        pwdView?.hintDialogListener = object : PwdView.HintDialogListener {
            override fun hint() {
                if (toast?.isShowing!!) {
                    toast?.hide()
                }
            }

        }
        accountRedEnvelopeFragment.callBack = this
        ordinaryRedEnvelopeFragment.callBack = this
        randomRedEnvelopeFragment.callBack = this
        red_envelope_record.setOnClickListener {
            var json = Gson().toJson(accountData)
            dialogSelect = SelectAccountDialog.newInstance(json, positionTag)
            dialogSelect?.selectAccountCallback = object : SelectAccountDialog.SelectAccountCallback {
                override fun onDetermine(select: AccountTable, positionTag: Int) {
                    this@BosRedEnvelopeActivity.positionTag = positionTag
                    ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RED_RECORD_ACTIVITY).withString("accountName", select.accountName).navigation()
                }
            }
            dialogSelect?.show(supportFragmentManager, "select")
        }

    }

    override fun data() {
        super.data()

    }

    private inner class Adapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title!![position]
        }

    }

    /**
     * 获取账户私钥列表
     */
    override fun getPrivateKey(date: ArrayList<SecretKeyBean>) {
        super.getPrivateKey(date)
        var privateKeyActive = ""
        var isActive = false
        for (i in 0 until date.size) {
            if (TextUtils.equals(date[i].publicKey, publickeyActive)) {
                isActive = true
                privateKeyActive = date[i].privateKey
            }
        }
        if (!isActive) {
            if (toast?.isShowing!!) {
                toast?.hide()
            }
            Toast.makeText(context(), getString(R.string.input_active_permission), Toast.LENGTH_LONG).show()
            return
        }

        presenter.createRedEnvelope(accountTable?.accountName!!, privateKeyActive, amount!!, redEnvelopeType, count, congratulations)
    }

    override fun getPrivateKeyFail(msg: String) {
        super.getPrivateKeyFail(msg)
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        Toast.makeText(context(), getString(R.string.get_account_error), Toast.LENGTH_LONG).show()


    }

    /**
     * 获取所有账户列表
     */
    override fun getAllAccountSuccess(list: ArrayList<AccountTable>) {
        super.getAllAccountSuccess(list)
        accountRedEnvelopeFragment.accountData = list
        ordinaryRedEnvelopeFragment.accountData = list
        randomRedEnvelopeFragment.accountData = list
        accountData.clear()
        accountData.addAll(list)
    }

    /**
     * 获取所有账户列表失败
     */
    override fun getAllAccountFail() {
        super.getAllAccountFail()
        Toast.makeText(context(), getString(R.string.get_account_error), Toast.LENGTH_LONG).show()

    }

    override fun createRedEnvelopeSuccess(str: String) {
        super.createRedEnvelopeSuccess(str)
        if (toast?.isShowing!!) {
            toast?.hide()
        }

        val b = Base58.encode(str.toByteArray())
        Toast.makeText(context(), getString(R.string.create_success), Toast.LENGTH_SHORT).show()
        ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RED_ADD_SUCCESS_ACTIVITY).withString("redEnvelopeText", b).withString("accountName", accountTable?.accountName).navigation()


    }

    override fun createRedEnvelopeFail(msg: String) {
        super.createRedEnvelopeFail(msg)
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        Toast.makeText(context(), getString(R.string.create_red_envelope_fail), Toast.LENGTH_LONG).show()

    }

    override fun accountRedEnvelope(accountTable: AccountTable, amount: String, redEnvelopeType: Int, count: Int, congratulations: String) {
        toast?.show()
        authList.clear()
        var lsit = Gson().fromJson<java.util.ArrayList<PermissionsBean>>(accountTable.accountPublic, object : com.google.common.reflect.TypeToken<java.util.ArrayList<PermissionsBean>>() {}.type)
        authList.addAll(lsit)
        (0 until authList.size)
                .filter { TextUtils.equals(authList[it].perm_name, "active") }
                .forEach { publickeyActive = authList[it].keys }
        pwdView?.decryption(shared.getPwdVerify(), "verify")
        this.amount = BigDecimal(amount).setScale(4, BigDecimal.ROUND_HALF_UP).toString() + " BOS"
        this.redEnvelopeType = redEnvelopeType
        this.count = count
        if (TextUtils.isEmpty(congratulations)) {
            this.congratulations = getString(R.string.congratulations)
        } else {
            this.congratulations = congratulations
        }
        this.accountTable = accountTable

    }

}