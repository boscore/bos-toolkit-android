package io.bos.accountmanager.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.MainActivity
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.WalletManagePresenter
import io.bos.accountmanager.ui.dialog.PwdView

import io.bos.accountmanager.view.WalletManageView
import io.starteos.jeos.crypto.ec.EosPrivateKey
import kotlinx.android.synthetic.main.activity_update_power.*
import java.util.ArrayList

/**
 * 更改权限
 */
@Route(path = Constants.RoutePath.ACTIVITY.UPDATE_POWER_ACTIVITY)
class UpdatePowerActivity : AbstractActivity<WalletManageView, WalletManagePresenter>(), WalletManageView {

    // 账户名
    @Autowired
    lateinit var accountName: String
    // 要修改的公钥和名称
    @Autowired
    lateinit var bean: String
    private var permissionsBean: PermissionsBean = PermissionsBean() //要修改的公钥

    private var privateKey: String = ""
    private var publicKey: String = ""
    private var accountTable: AccountTable? = null;
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper

    private var isFininsh: Boolean = false

    var dialog: LoadingDailog? = null
    var currentIs: Boolean = false//当前是不允许修改的
    private var keyList: ArrayList<SecretKeyBean>? = ArrayList()  //账号的权限私钥
    private var dataSource: ArrayList<PermissionsBean> = ArrayList<PermissionsBean>()// 当前钱包导入时的私钥权限
    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): WalletManageView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_update_power
    }

    override fun errAccount(message: String) {
        super.errAccount(message)
        dialog?.dismiss()
        Toast.makeText(this@UpdatePowerActivity, message, Toast.LENGTH_LONG).show()


    }

    override fun updateSuccess() {
        super.updateSuccess()
        dialog?.dismiss()
        Toast.makeText(this@UpdatePowerActivity, resources.getString(R.string.update_poer_txt_success), Toast.LENGTH_LONG).show()
        isFininsh = true
        finish()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFininsh) {
            ARouter.getInstance().build(Constants.RoutePath.MAIN_ACTIVITY).navigation()
        }


    }

    //获取账号详情
    override fun onAccounts(accounts: AccountTable) {
        super.onAccounts(accounts)
        accountTable = accounts
        dataSource?.clear()
        var list = Gson().fromJson<ArrayList<PermissionsBean>>(accountTable?.accountPublic, object : com.google.common.reflect.TypeToken<ArrayList<PermissionsBean>>() {}.type)
        if (list != null && !list.isEmpty()) {
            dataSource.addAll(list)
        } else {
            Toast.makeText(context(), resources.getString(R.string.update_pwer_err_txt_owner), Toast.LENGTH_LONG).show()
        }


    }

    //获取当前账号的所有公私钥
    override fun getCurrentAccountKey(date: ArrayList<SecretKeyBean>, pwd: String) {
        super.getCurrentAccountKey(date, pwd)
        keyList?.clear()
        keyList?.addAll(date)
        var owner: PermissionsBean = PermissionsBean()
        for (i in 0 until dataSource!!.size) {
            if (TextUtils.equals(dataSource[i].perm_name, "owner")) {
                owner.keys = dataSource[i].keys
                owner.perm_name = dataSource[i].perm_name
            }

        }
        if (TextUtils.equals(owner.keys, "") || owner.keys.isEmpty()) {
            Toast.makeText(context(), resources.getString(R.string.update_pwer_err_txt_owner), Toast.LENGTH_LONG).show()
        } else {
            //获取当前账户的owner私钥
            var ownerPrivate = ""
            //判断本地是否有owner
            for (i in 0 until keyList!!.size) {
                if (TextUtils.equals(keyList!![i].publicKey, owner.keys)) {
                    currentIs = true

                    ownerPrivate = keyList!![i].privateKey
                    break
                }
            }

            if (currentIs) {
                var parentLevel = "active"
                if (TextUtils.equals(permissionsBean?.perm_name, "owner")) {
                    parentLevel = ""
                } else {
                    parentLevel = "owner"
                }
                dialog?.show()
                presenter.updateAuthority(accountName, permissionsBean?.perm_name, parentLevel, update_power_txt_public.text.toString().trim(), update_power_txt_private.text.toString(), ownerPrivate, accountTable!!, pwd, permissionsBean?.keys, permissionsBean.perm_name)

            } else {
                Toast.makeText(context(), resources.getString(R.string.update_pwer_err_txt_owner), Toast.LENGTH_LONG).show()
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        permissionsBean = Gson().fromJson<PermissionsBean>(bean, object : TypeToken<PermissionsBean>() {}.type)
        update_power_txt_current.text = String.format("%s Key(%s)", permissionsBean?.perm_name, resources.getString(R.string.update_power_txt_ago))
        update_power_txt_key.text = permissionsBean?.keys
        rebuildKey()
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();

        presenter.accounts(accountName)
        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                //确认导出
                presenter.currentAccountKeys(pwd, accountTable!!)
            }

        }

    }

    override fun listener() {
        super.listener()
        update_power_linear_click_renovate.setOnClickListener {
            rebuildKey()

        }
        update_power_determine.setOnClickListener {
            currentIs = false
            //输入密码
            pwdView?.decryption(shared.getPwdVerify(), "verify")

        }

        back.setOnClickListener {
            finish()
        }

        update_power_txt_public.setOnClickListener {
            var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setText(update_power_txt_public.text.toString().trim());
            Toast.makeText(this@UpdatePowerActivity, resources.getString(R.string.new_red_envelopes_cope), Toast.LENGTH_LONG).show()
        }

        update_power_txt_private.setOnClickListener {
            var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setText(update_power_txt_private.text.toString().trim());
            Toast.makeText(this@UpdatePowerActivity, resources.getString(R.string.new_red_envelopes_cope), Toast.LENGTH_LONG).show()
        }


    }

    fun rebuildKey() {
        val key = EosPrivateKey()
        privateKey = key.toString()
        publicKey = key.publicKey.toString(Constants.Const.WALLETTYPE)
        update_power_txt_private.text = privateKey
        update_power_txt_public.text = publicKey
    }
}
