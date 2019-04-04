package io.bos.accountmanager.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.androidkun.xtablayout.XTabLayout
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.ImportCallback
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.ImportAccountPresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.fragment.*
import io.bos.accountmanager.view.ImportAccountView
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_import_account.*

/**
 * 导入账号
 */
@Route(path = Constants.RoutePath.ACTIVITY.IMPORT_ACCOUNT_ACTIVITY)
class ImportAccountActivity : AbstractActivity<ImportAccountView, ImportAccountPresenter>(), ImportCallback, ImportAccountView {


    private var fragments: MutableList<AbstractBosFragment>? = null
    private var currentTabIndex = 0
    var dialog: LoadingDailog? = null
    private var accountTables: ArrayList<Pair<String, AccountTable?>> = ArrayList();
    private var listpublic: ArrayList<String> = ArrayList()
    private lateinit var importCloudFragment: ImportCloudfragment //云导入
    private lateinit var importPrivateKeyFragment: ImportPrivateKeyfragment //私钥导入
    private lateinit var importStoreFragment: ImportKeyStoreFragment //store导入
    private var privateKey: EosPrivateKey? = null
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    override fun importError() = Toast.makeText(this, getString(R.string.import_error), Toast.LENGTH_LONG).show()
    override fun onAccountError() {
        dialog?.dismiss()
        Toast.makeText(this, getString(R.string.get_account_error), Toast.LENGTH_LONG).show()

    }

    override fun localAccount(accountTables: ArrayList<Pair<String, AccountTable?>>, date: ArrayList<String>) {
        dialog?.dismiss()
        this.accountTables.clear()
        this.listpublic.clear()
        this.listpublic.addAll(date)
        this.accountTables.addAll(accountTables)
        pwdView?.decryption(shared.getPwdVerify(), "verify")
    }

    override fun localAccountError(message: String) {
        dialog?.dismiss()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onAccount(account: ArrayList<String>) {
        presenter.getLocalAccount(account)
    }

    override fun importSuccess() {
        setResult(Activity.RESULT_OK)
        finish()

    }


    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): ImportAccountView {
        return this
    }

    override fun privateKey(data: String) {
        privateKey = try {
            EosPrivateKey(data)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.invalid_private), Toast.LENGTH_LONG).show()
            return
        }

        dialog?.show()
        presenter.getAccount(privateKey!!.publicKey.toString(Constants.Const.WALLETTYPE))
//        通过私钥获取账户
//        解密本地测试数据，获取密码，然后加密私钥并存入数据库
    }

    override fun oneDrive(data: ArrayList<String>) {
    }

    override fun addDisposable(disposable: Disposable) {
        presenter.addDisposable2(disposable)
    }

    override fun byId(): Int {
        return R.layout.activity_import_account
    }

    override fun init() {
        super.init()
        shared = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(shared, this)
        pwdView!!.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                presenter.writeDB(accountTables, privateKey!!, pwd, listpublic)
            }
        }
        fragments = ArrayList()
        importStoreFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.IMPORT_KEYSTORE_FRAGMENT).navigation() as ImportKeyStoreFragment
        importPrivateKeyFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.IMPORT_PRIVATE_KEY_FRAGMENT).navigation() as ImportPrivateKeyfragment
        importCloudFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.IMPORT_CLOUD_FRAGMENT).navigation() as ImportCloudfragment
        fragments!!.clear()
        (fragments as ArrayList<AbstractBosFragment>).add(importStoreFragment!!)
        (fragments as ArrayList<AbstractBosFragment>).add(importPrivateKeyFragment!!)
        (fragments as ArrayList<AbstractBosFragment>).add(importCloudFragment!!)
    }

    override fun data() {
        super.data()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt("currentTabIndex", currentTabIndex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        if (savedInstanceState == null) {
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.import_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitAllowingStateLoss()
        } else {
            currentTabIndex = savedInstanceState.getInt("currentTabIndex")
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.import_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitAllowingStateLoss()
        }
        importCloudFragment.callback = this
        importStoreFragment.callback = this
        importPrivateKeyFragment.callback = this
        import_xTablayout?.addTab(import_xTablayout?.newTab()!!.setText(getString(R.string.import_tab_key)))
        import_xTablayout?.addTab(import_xTablayout?.newTab()!!.setText(getString(R.string.import_tab_private)))
        import_xTablayout?.addTab(import_xTablayout?.newTab()!!.setText(getString(R.string.import_tab_cloud)))
        import_xTablayout.setOnTabSelectedListener(object : XTabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: XTabLayout.Tab?) {}
            override fun onTabUnselected(tab: XTabLayout.Tab?) {}
            override fun onTabSelected(tab: XTabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {

                        switchFragment(0)

                    }
                    1 -> {

                        switchFragment(1)


                    }
                    2 -> {

                        switchFragment(2)
                    }

                }

            }

        })
        back.setOnClickListener {
            finish()
        }
    }

    companion object Factory {
        private const val IMPORT_CLOUD_FRAGMENT = "Import_Cloud_fragment"//云导入
        private const val IMPORT_KEY_STORE_FRAGMENT = "Import_Key_Store_fragment"//KeyStore导入
        private const val IMPORT_PRIVATE_KEY_FRAGMENT = "Import_Private_Key_fragment"//私钥导入
    }

    private fun switchFragment(index: Int) {
        if (currentTabIndex != index) {
            val trx = supportFragmentManager.beginTransaction()
            trx.hide(fragments!![currentTabIndex])
            if (!fragments!![index].isAdded) {
                trx.add(R.id.import_contentPanel, fragments!![index])
            }
            trx.show(fragments!![index]).commitAllowingStateLoss()
        }
        currentTabIndex = index
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

}
