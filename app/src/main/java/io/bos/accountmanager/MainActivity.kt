package io.bos.accountmanager

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.google.gson.Gson
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.MainPresenter
import io.bos.accountmanager.ui.AbstractActivity
import io.bos.accountmanager.ui.AbstractBosFragment
import io.bos.accountmanager.ui.dialog.RedPackageDialog
import io.bos.accountmanager.ui.dialog.WhetherBackupsDialog
import io.bos.accountmanager.ui.fragment.HomeWalletFragment
import io.bos.accountmanager.ui.fragment.PersonalCenterFragment
import io.bos.accountmanager.view.MainView
import io.starteos.jeos.crypto.util.Base58
import kotlinx.android.synthetic.main.activity_main.*

@Route(path = Constants.RoutePath.MAIN_ACTIVITY)
class MainActivity : AbstractActivity<MainView, MainPresenter>(), MainView {
    override fun onBalance() {
        homeFragment.onBalance()
    }

    private var isDisplay: Boolean = true
    private val isAccounts = ArrayList<AccountTable>()
    private var checkId: Int = R.id.main_select
    private var fragments: MutableList<AbstractBosFragment>? = null
    private var currentTabIndex = 0
    private lateinit var homeFragment: HomeWalletFragment
    private lateinit var persona_fragment: PersonalCenterFragment
    private var redPackageDialog: RedPackageDialog? = null
    private val accounts = ArrayList<AccountTable>()
    private var whetherBackupsDialog: WhetherBackupsDialog? = null//提示备份
    var dialog: LoadingDailog? = null

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): MainView {
        return this
    }

    override fun byId(): Int {
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setStatusBar()
        return R.layout.activity_main
    }

    fun getBalance() {
        presenter.balance()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt("currentTabIndex", currentTabIndex)
    }

    override fun getAccountIs(accounts: List<AccountTable>) {
        super.getAccountIs(accounts)
        isDisplay = true
        isAccounts.clear()
        isAccounts.addAll(accounts)
        if (isAccounts.size > 0) {
            whetherBackupsDialog = WhetherBackupsDialog.newInstance(Gson().toJson(isAccounts))

            whetherBackupsDialog?.whetherBackupsCallback = object : WhetherBackupsDialog.WhetherBackupsCallback {
                override fun onImmediately() {
                    //立即备份
                    if (NoDoubleClickUtils.isDoubleClick()) {

                        presenter.updateState()
                    }
                }
                override fun onAlreadyBackups() {
                    //已经备份
                    if (NoDoubleClickUtils.isDoubleClick()) {

                        presenter.updateState()
                    }
                }
            }
            whetherBackupsDialog?.show(supportFragmentManager, "updatePowerActivity")
        }


    }

    override fun errAccount() {
        super.errAccount()
        isDisplay = true
    }

    override fun init() {
        super.init()
        fragments = ArrayList()
        homeFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.HOME_WALLET_FRAGMENT).navigation() as HomeWalletFragment
        persona_fragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.PERSONAL_CENTER_FRAGMENT).navigation() as PersonalCenterFragment
        fragments!!.clear()
        (fragments as ArrayList<AbstractBosFragment>).add(homeFragment!!)
        (fragments as ArrayList<AbstractBosFragment>).add(persona_fragment!!)
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        light(false)
        if (savedInstanceState == null) {
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.main_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitAllowingStateLoss()
        } else {
            currentTabIndex = savedInstanceState.getInt("currentTabIndex")
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.main_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitAllowingStateLoss()
        }
        group.setOnCheckedChangeListener { _, i ->
            checkId = i
            when (i) {
                R.id.main_select -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setWindowStatusBarColor(this@MainActivity)
                    }
                    switchFragment(0)
                }
                R.id.personal_select -> {
                    transparencyBar(this@MainActivity)
                    switchFragment(1)
                }
            }
        }

        group.check(R.id.main_select)

        var getSting = getClipData()
        if (!TextUtils.isEmpty(getSting)&&!getSting.equals("")) {
             var desc=try {
                 String(Base58.decode(getSting))
             }catch (e:Exception){
                 ""
             }
            var redTextList =desc .split("^")
            if (redTextList.size == 3) {
                redPackageDialog = RedPackageDialog.newInstance(getClipData())
                redPackageDialog?.getRedEnvelopeCallback = object : RedPackageDialog.GetRedEnvelopeCallback {
                    override fun get(redText: String) {
                        ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RECEIVE_RED_ENVELOPE_ACTIVITY).withString("redText", redText).navigation()
                    }
                }
                redPackageDialog?.show(supportFragmentManager, "")
                clearClipData()

            }
        }


    }

    /**
     * 获取剪切板内容是否是红包串 弹出领取框
     */
    fun getClipData(): String {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val data = cm.primaryClip
        if (data == null) {
            return ""
        } else {
            val item = data.getItemAt(0)
            return item.text.toString()
        }
    }

    /**
     * 清空剪切板内容
     */
    fun clearClipData() {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, "")
        clipboard.primaryClip = clipData
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt("currentTabIndex")
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.main_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitNowAllowingStateLoss()
        }
    }

    override fun onAccounts(accounts: List<AccountTable>) {
        super.onAccounts(accounts)
        this.accounts.clear()
        this.accounts.addAll(accounts)
        homeFragment.onAccounts(this.accounts)

        if (accounts.isEmpty()) {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.HOME_ACTIVITY).navigation(this, 205)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 205 && resultCode != Activity.RESULT_OK) || accounts.isEmpty()) {
            finish()
        }
    }

    override fun listener() {
        super.listener()
    }

    override fun data() {
        super.data()
    }

    init {
        fragments = ArrayList()
    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    private fun switchFragment(index: Int) {
        if (currentTabIndex != index) {
            val trx = supportFragmentManager.beginTransaction()
            trx.hide(fragments!![currentTabIndex])
            if (!fragments!![index].isAdded) {
                trx.add(R.id.main_contentPanel, fragments!![index])
            }
            trx.show(fragments!![index]).commitAllowingStateLoss()
        }
        currentTabIndex = index
        if (fragments!![index] is PersonalCenterFragment) {
            light(false)
        } else {
            light(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setWindowStatusBarColor(activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = activity.resources.getColor(R.color.colorWhite)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            //底部导航栏
            //window.setNavigationBarColor(activity.getResources().getColor(colorResId))
        }

    }


    override fun onResume() {
        super.onResume()
        if (isDisplay) {
            isDisplay = false
            presenter.getIsAccount()
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
