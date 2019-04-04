package io.bos.accountmanager.ui


import android.os.Bundle
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
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.WalletManagePresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.SelectPrivateKeyDialog
import io.bos.accountmanager.view.WalletManageView
import kotlinx.android.synthetic.main.activity_wallet_manage.*

@Route(path = Constants.RoutePath.ACTIVITY.WALLET_MANAGE_ACTIVITY)
class WalletManageActivity : AbstractActivity<WalletManageView, WalletManagePresenter>(), WalletManageView {

    var dialog: LoadingDailog? = null
    // 账户名
    @Autowired
    lateinit var accountName: String

    private var accountTable: AccountTable? = null;
    //对话框
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    private var decryptDate: ArrayList<SecretKeyBean> = ArrayList<SecretKeyBean>()

    private var dialogSelect: SelectPrivateKeyDialog? = null

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): WalletManageView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_wallet_manage
    }

    override fun onAccounts(accounts: AccountTable) {
        super.onAccounts(accounts)
        transparencyBar(this@WalletManageActivity)
        accountTable = accounts
        wallet_manage_balance.text = accountTable?.balance

    }

    override fun getDecrypt(date: ArrayList<SecretKeyBean>) {
        super.getDecrypt(date)
        dialog?.dismiss()
        decryptDate.clear()

        if (!date.isEmpty()) {
            decryptDate.addAll(date)
        }

        var json = Gson().toJson(decryptDate)
        dialogSelect = SelectPrivateKeyDialog.newInstance(json)
        dialogSelect?.selectPrivateKeyCallback = object : SelectPrivateKeyDialog.SelectPrivateKeyCallback {
            override fun onDetermine(select: Int) {
//                //确认导出
                dialogSelect?.dismiss()

                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.EXPORT_KEYT_TIPS_ACTIVITY)
                        .withString("key", decryptDate[select].privateKey)
                        .withInt("type", 1)
                        .navigation()

            }

        }

        dialogSelect?.show(supportFragmentManager, "select")
    }

    override fun errMessage(message: String) {
        super.errMessage(message)
        dialog?.dismiss()
        Toast.makeText(context(), message, Toast.LENGTH_LONG).show()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();


        wallet_manage_name.text = accountName
        //限额配置
        wallet_manage_click_norm.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.QUOTA_DEPLOY_ACTIVITY).navigation()
        }
        //高级设置
        wallet_manage_install.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.SENIOR_SETUP_ACTIVITY).withString("accountName", accountName).navigation()
        }
        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                dialog?.show()
                //确认导出
                presenter.exportKeyStoreDate(pwd, accountTable!!)
            }
        }
        presenter.accounts(accountName)
    }

    override fun listener() {
        super.listener()
        wallet_manage_click_keyStore.setOnClickListener {

            if (NoDoubleClickUtils.isDoubleClick()) {
                //输入密码
                pwdView?.decryption(shared.getPwdVerify(), "verify")

            }


        }

        wallet_manage_back.setOnClickListener {
            finish()
        }
    }

    override fun init() {
        super.init()
    }

    override fun data() {
        super.data()
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


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_wallet_manage)
//
//        //限额配置
//        wallet_manage_click_norm.setOnClickListener{
//            var inte=Intent(this,QuotaDeployActivity::class.java)
//            startActivity(inte)
//        }
//        //高级设置
//        wallet_manage_install.setOnClickListener{
//            var inte=Intent(this,SeniorSetUpActivity::class.java)
//            startActivity(inte)
//        }
//
//
//    }


//    //去掉标题栏
//    fun transparencyBar(activity: Activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            val window = activity.window
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//            )
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            window.statusBarColor = Color.TRANSPARENT
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            val window = activity.window
//            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        }
//    }
}
