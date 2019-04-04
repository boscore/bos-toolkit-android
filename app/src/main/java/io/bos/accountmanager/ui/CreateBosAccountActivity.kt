package io.bos.accountmanager.ui

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.CreateBosAccountPresenter
import io.bos.accountmanager.presenter.EosRedEnvelopePresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.SelectAccountDialog
import io.bos.accountmanager.ui.dialog.TipsSureDialog
import io.bos.accountmanager.view.CreateBosAccountView
import io.bos.accountmanager.view.EosRedEnvelopeView
import io.starteos.jeos.crypto.ec.EosPrivateKey
import kotlinx.android.synthetic.main.activity_create_bos_account.*
import java.math.BigDecimal

/**
 * 创建BOS账号
 * Created by Administrator on 2019/1/3/003.
 */
@Route(path = Constants.RoutePath.ACTIVITY.CREATE_BOS_ACCOUNT_ACTIVITY)
class CreateBosAccountActivity : AbstractActivity<CreateBosAccountView, CreateBosAccountPresenter>(), CreateBosAccountView {

    // 账户名
    @Autowired
    lateinit var accountName: String

    // 私钥
    @Autowired
    lateinit var privateKey: String
    var dialog: LoadingDailog? = null
    var memo: String = ""
    var eosprivateKey: EosPrivateKey? = null  //新账号的私钥
    private var accounts = ArrayList<AccountTable>()//账户信息
    private var accountTableben: AccountTable? = null
    private var keyList: ArrayList<SecretKeyBean>? = ArrayList()  //账号的权限私钥
    private var password: String = ""
    var accountBean: PermissionsBean? = null//选择的账号私钥

    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper

    private var accountTables: ArrayList<Pair<String, AccountTable?>> = ArrayList();
    private var listpublic: ArrayList<String> = ArrayList();


    //选择对话框
    private var selectAccountDialog: SelectAccountDialog? = null

    override fun byId(): Int {
        transparencyBar(this@CreateBosAccountActivity)
        return R.layout.activity_create_bos_account
    }

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): CreateBosAccountView = this


    override fun onAccount(account: ArrayList<String>) {
        if (account.size > 0) {
            //获取账户信息成功
            presenter.getLocalAccount(account)
        } else {
            dialog?.dismiss()
            Toast.makeText(context(), resources.getString(R.string.create_bos_pay_import_fail), Toast.LENGTH_LONG).show()
        }
    }

    override fun importSuccess() {
        dialog?.dismiss()
        Toast.makeText(this@CreateBosAccountActivity, resources.getString(R.string.new_red_envelopes_txt_imotu), Toast.LENGTH_LONG).show()
        ARouter.getInstance().build(Constants.RoutePath.MAIN_ACTIVITY).navigation()
    }

    override fun importError() {
        dialog?.dismiss()
        Toast.makeText(this@CreateBosAccountActivity, resources.getString(R.string.new_red_envelopes_txt_retry), Toast.LENGTH_LONG).show()
    }

    override fun localAccount(accountTables: java.util.ArrayList<Pair<String, AccountTable?>>, date: java.util.ArrayList<String>) {
        dialog?.dismiss()
        this.accountTables.clear()
        this.listpublic.clear()
        this.listpublic.addAll(date)
        this.accountTables.addAll(accountTables)
        presenter.writeDB(accountTables, eosprivateKey!!, password, listpublic)
    }

    override fun localAccountError(message: String) {
        dialog?.dismiss()
        //获取账户信息失败
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun errMessage(message: String) {
        super.errMessage(message)
        dialog?.dismiss()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()


    }

    //创建账户成功
    override fun establishSuccess() {
        super.establishSuccess()

        //查询账号是否存在
        presenter.getAccount(eosprivateKey?.publicKey.toString())

    }

    override fun getCurrentAccountKey(date: ArrayList<SecretKeyBean>, ped: String) {
        super.getCurrentAccountKey(date, ped)
        password = ped
        keyList?.clear()
        keyList?.addAll(date)
        //是否有active权限
        var isexistence: Boolean = false
        var activekey: String = ""
        for (i in 0 until keyList!!.size) {
            if (TextUtils.equals(accountBean?.keys, keyList!![i].publicKey)) {
                activekey = keyList!![i].privateKey
                isexistence = true
                break
            }

        }
        //是否有active权限
        if (isexistence) {

            var loadBuilder = LoadingDailog.Builder(this@CreateBosAccountActivity)
                    .setMessage("")
                    .setCancelable(false)
                    .setShowMessage(false)
                    .setCancelOutside(false);
            dialog = loadBuilder.create();


            dialog?.show()
            var money = BigDecimal(create_bos_money.text.toString().trim()).setScale(4, BigDecimal.ROUND_DOWN)
            presenter.transferTransaction(activekey, memo, accountTableben?.accountName!!, money.toString())
        } else {
            Toast.makeText(context(), resources.getString(R.string.create_bos_account_err_txt_active), Toast.LENGTH_LONG).show()
        }

    }


    //获取账户信息
    override fun onAccountsName(accounts: List<AccountTable>) {

        dialog?.dismiss()
        this.accounts.clear()
        this.accounts.addAll(accounts)

        selectAccountDialog = SelectAccountDialog.newInstance(Gson().toJson(this.accounts), 0)
        selectAccountDialog?.selectAccountCallback = object : SelectAccountDialog.SelectAccountCallback {
            override fun onDetermine(select: AccountTable, positionTag: Int) {
                accountTableben = select
                var list = Gson().fromJson<ArrayList<PermissionsBean>>(accountTableben?.accountPublic, object : com.google.common.reflect.TypeToken<java.util.ArrayList<PermissionsBean>>() {}.type)
                accountBean = PermissionsBean()
                var isexistence: Boolean = false
                for (i in 0 until list!!.size) {
                    if (TextUtils.equals(list[i].perm_name, "active")) {
                        accountBean?.keys = list[i].keys
                        accountBean?.perm_name = list[i].perm_name
                        isexistence = true
                        break
                    }

                }

                if (isexistence) {
                    selectAccountDialog?.dismiss()
                    selectAccountDialog = null

                    pwdView?.decryption(shared.getPwdVerify(), "verify")


                } else {
                    Toast.makeText(context(), resources.getString(R.string.create_bos_account_err_txt_active), Toast.LENGTH_LONG).show()
                }

            }

        }
        selectAccountDialog?.show(supportFragmentManager, "select")

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        eosprivateKey = EosPrivateKey(privateKey)
        create_bos_name.text = Constants.Const.BOS_CONTRACT
        memo = "act^$accountName^${eosprivateKey?.publicKey}^${eosprivateKey?.publicKey}"
        setPricePoint(create_bos_money, 4)
        create_bos_remarks.setText(memo)
        var loadBuilder = LoadingDailog.Builder(this@CreateBosAccountActivity)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();

        shared = BOSApplication.get(this@CreateBosAccountActivity).getAppComponent().preferences()
        pwdView = PwdView(shared, this@CreateBosAccountActivity)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {

                presenter.currentAccountKeys(pwd, accountTableben!!)


            }

        }


    }


    override fun listener() {
        super.listener()

        create_bos_btn.setOnClickListener {


            if (TextUtils.isEmpty(create_bos_money.text.toString().trim())) {
                Toast.makeText(context(), resources.getString(R.string.create_bos_account_money), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var money = BigDecimal(create_bos_money.text.toString().trim()).setScale(4, BigDecimal.ROUND_DOWN)
            if (money.compareTo(BigDecimal(0)) == 1) {
                dialog?.show()
                presenter.accountsName()
            } else {
                Toast.makeText(context(), resources.getString(R.string.create_bos_account_money), Toast.LENGTH_LONG).show()

            }


        }

        back.setOnClickListener {
            finish()
        }


        create_bos_remarks.setOnClickListener {
            var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;
            cm.setText(create_bos_remarks.getText()?.toString() + "");
            Toast.makeText(context(), resources.getString(R.string.new_red_envelopes_cope), Toast.LENGTH_LONG).show()
        }
    }


    /**
     * 让一个输入框只能输入指定位数小数
     *
     * @param editText
     */
    fun setPricePoint(editText: EditText, maxPoint: Int) {
        editText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
                var s = s
                if (s.toString().contains(".")) {
                    if (s.length - 1 - s.toString().indexOf(".") > maxPoint) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + maxPoint + 1)
                        editText.setText(s)
                        editText.setSelection(s.length)
                    }
                }
                if (s.toString().trim { it <= ' ' }.substring(0) == ".") {
                    s = "0$s"
                    editText.setText(s)
                    editText.setSelection(2)
                }

                if (s.toString().startsWith("0") && s.toString().trim { it <= ' ' }.length > 1) {
                    if (s.toString().substring(1, 2) != ".") {
                        editText.setText(s.subSequence(0, 1))
                        editText.setSelection(1)
                        return
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }

        })

    }

}