package io.bos.accountmanager.ui

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.ImportCallback
import io.bos.accountmanager.core.storage.StorageFactory
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.CloudStoragePresenter
import io.bos.accountmanager.presenter.WalletManagePresenter
import io.bos.accountmanager.ui.dialog.PwdDialogCloud
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.SelectPrivateKeyDialog
import io.bos.accountmanager.ui.dialog.TipsSureDialog
import io.bos.accountmanager.view.WalletManageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_senior_set_up.*

/**
 * 高级设置
 */

@Route(path = Constants.RoutePath.ACTIVITY.SENIOR_SETUP_ACTIVITY)
class SeniorSetUpActivity : AbstractActivity<WalletManageView, WalletManagePresenter>(), WalletManageView {
    private var toast: LoadingDailog? = null

    // 账户名
    @Autowired
    lateinit var accountName: String
    private var pwdSting: String = ""
    private var isDelete: Boolean = false
    private var cloud = ArrayList<WalletManagePresenter.EnDataResults>()
    private var accountTable: AccountTable? = null

    //对话框
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    private var decryptDate: ArrayList<SecretKeyBean> = ArrayList<SecretKeyBean>()

    private var dialogSelect: SelectPrivateKeyDialog? = null
    private var tipsSureDialog: TipsSureDialog? = null


    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): WalletManageView {
        return this
    }


    override fun getErrAuthority(message: String) {
        super.getErrAuthority(message)
        Toast.makeText(context(), message, Toast.LENGTH_SHORT).show()
        presenter.deleteWallet(accountName)

    }

    override fun byId(): Int {
        return R.layout.activity_senior_set_up
    }

    override fun onAccounts(accounts: AccountTable) {
        super.onAccounts(accounts)
        accountTable = accounts


    }

    override fun getPrivateKey(date: ArrayList<SecretKeyBean>) {
        super.getPrivateKey(date)
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        decryptDate.clear()
        if (!date.isEmpty()) {
            decryptDate.addAll(date)
        }

        if (decryptDate.size > 0) {
            var json = Gson().toJson(decryptDate)

            dialogSelect = SelectPrivateKeyDialog.newInstance(json)
            dialogSelect?.selectPrivateKeyCallback = object : SelectPrivateKeyDialog.SelectPrivateKeyCallback {
                override fun onDetermine(select: Int) {
//                //确认导出
                    dialogSelect?.dismiss()
                    ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.EXPORT_KEYT_TIPS_ACTIVITY)
                            .withString("key", decryptDate[select].privateKey)
                            .withInt("type", 0)
                            .navigation()
                }
            }
            dialogSelect?.show(supportFragmentManager, "select")
        }


    }

    override fun errMessage(message: String) {
        super.errMessage(message)
        Toast.makeText(context(), message, Toast.LENGTH_LONG).show()
    }

    override fun onCloudAccounts(cloudAccounts: ArrayList<WalletManagePresenter.EnDataResults>) {
        super.onCloudAccounts(cloudAccounts)
        toast?.show()
        if (accountTable != null) {
            var publickey = Gson().fromJson<java.util.ArrayList<String>>(accountTable?.publicKey, object : com.google.common.reflect.TypeToken<java.util.ArrayList<String>>() {}.type)
            for (i in 0 until cloudAccounts.size) {
                if (TextUtils.equals(accountName, cloudAccounts[i].accountName)) {
                    for (j in 0 until publickey.size) {
                        for (k in 0 until cloudAccounts[i].enData.size) {
                            var data = ArrayList<SecretKeyBean>()
                            if (!TextUtils.equals(cloudAccounts[i].enData[k].publicKey, publickey[j])) {
                                data.add(cloudAccounts[i].enData[k])
                            }
                            if (k == cloudAccounts[i].enData.size - 1) {
                                cloudAccounts[i].enData = data
                            }

                        }
                    }
                }

            }

            for (i in 0 until cloudAccounts.size) {
                if(cloudAccounts[i].enData!=null){
                    if(TextUtils.equals(cloudAccounts[i].accountName,accountName)){
                        if(cloudAccounts[i].enData.size<=0){
                            cloudAccounts.remove(cloudAccounts[i])
                        }
                        break
                    }

                }else{
                    cloudAccounts.remove(cloudAccounts[i])
                    break
                }

            }


            cloud.clear()
            cloud.addAll(cloudAccounts)
            pwdView?.decryption(shared.getPwdVerify(), "cloud")

        } else {
            Toast.makeText(context(), resources.getString(R.string.delete_fail), Toast.LENGTH_SHORT).show()
        }


    }

    override fun showPwd(listener: PwdDialogCloud.PwdCallback) {
        super.showPwd(listener)
        val pwdView = PwdDialogCloud()
        pwdView.pwdCallback = listener
        pwdView.show(supportFragmentManager, "showCloudPwd")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)

        toast = toast("").create()
        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                if (TextUtils.equals(tag.toString(), "delete")) {
                    toast?.show()
                    pwdSting = pwd
                    presenter.deleteWallet(accountName)
                } else if (TextUtils.equals(tag.toString(), "cloud")) {
                    presenter.synchronization(ArrayList<WalletManagePresenter.EnDataResults>(), cloud, pwd, Consumer {
                        if (toast?.isShowing!!) {
                            toast?.hide()
                        }
                        presenter.deleteWalletCloud(accountName)


                    }, Consumer {
                        if (toast?.isShowing!!) {
                            toast?.hide()
                        }
                        Toast.makeText(context(), resources.getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
                    })
                } else {
                    toast?.show()
                    //确认导出
                    presenter.exportPrivateKey(pwd, accountTable!!)
                }
            }
        }
        pwdView?.hintDialogListener = object : PwdView.HintDialogListener {
            override fun hint() {
                if (toast?.isShowing!!) {
                    toast?.hide()
                }
            }
        }
        //获取本地钱包信息
        presenter.accounts(accountName)
    }

    override fun cloceDialog() {
        super.cloceDialog()
        toast?.dismiss()
    }

    override fun listener() {
        super.listener()
        //点击权限管理
        senior_click_authority.setOnClickListener {

            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.AUTHORITY_SETTING_ACTIVITY).withString("accountName", accountName).navigation()
        }
        //导出私钥
        senior_click_key.setOnClickListener {
            //输入密码
            if (NoDoubleClickUtils.isDoubleClick()) {
                //输入密码
                pwdView?.decryption(shared.getPwdVerify(), "verify")
            }
        }
        back.setOnClickListener {
            finish()
        }
        wallet_manage_install.setOnClickListener {
            //            pwdView?.decryption(shared.getPwdVerify(), "delete")
            tipsSureDialog = TipsSureDialog.newInstance(resources.getString(R.string.cloud_dialog_is_content), resources.getString(R.string.cloud_dialog_no), resources.getString(R.string.cloud_dialog_yes))
            tipsSureDialog?.tipsSureCallback = object : TipsSureDialog.TipsSureCallback {
                override fun onCancelLeftClick() {
                    tipsSureDialog?.dismiss()
                    pwdView?.decryption(shared.getPwdVerify(), "delete")
                }

                override fun onSureRightClick() {
                    tipsSureDialog?.dismiss()
                    toast?.show()
                    presenter.loginOneDrive(Consumer {

                        //登录成功，进入下一页
                        if (it.isLogin) {
                            presenter.getCloudAccounts()
                        } else {
                            toast?.dismiss()
                            Toast.makeText(context(), resources.getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
                        }
                    }, Consumer {
                        toast?.dismiss()
                        Toast.makeText(context(), resources.getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
                    })
                }
            }
            tipsSureDialog?.show(supportFragmentManager, "AccountList")


        }
    }

    object NoDoubleClickUtils {
        private val SPACE_TIME = 500//2次点击的间隔时间，单位ms
        private var lastClickTime: Long = 0
        fun isDoubleClick(): Boolean {
            val currentTime = System.currentTimeMillis()
            val isClick: Boolean
            isClick = currentTime - lastClickTime > SPACE_TIME
            lastClickTime = currentTime
            return isClick
        }
    }


    override fun onDeleteWalletSuccess() {
        super.onDeleteWalletSuccess()
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        Toast.makeText(context(), getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
        ARouter.getInstance().build(Constants.RoutePath.MAIN_ACTIVITY).navigation()
        finish()
    }

    override fun onDeleteWalletFail() {
        super.onDeleteWalletFail()
        if (toast?.isShowing!!) {
            toast?.hide()
        }
        Toast.makeText(context(), getString(R.string.delete_fail), Toast.LENGTH_SHORT).show()

    }
}
