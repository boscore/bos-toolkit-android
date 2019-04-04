package io.bos.accountmanager.ui

import android.app.Activity
import android.content.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.presenter.NewRedEnvelopesPresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.RedPackageDialog
import io.bos.accountmanager.ui.dialog.TipsSureDialog
import io.bos.accountmanager.view.NewRedEnvelopesView
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import kotlinx.android.synthetic.main.activity_new_red_envelopes.*
import java.lang.Exception
import java.util.regex.Pattern

/**
 *创建红包界面
 */
@Route(path = Constants.RoutePath.ACTIVITY.NEW_READ_ENVELOPERS_ACTIVITY)
class NewRedEnvelopesActivity : AbstractActivity<NewRedEnvelopesView, NewRedEnvelopesPresenter>(), NewRedEnvelopesView {

    var list: ArrayList<EstablishAccountTable>? = ArrayList()
    private var accountTables: ArrayList<Pair<String, AccountTable?>> = ArrayList();
    private var listpublic: ArrayList<String> = ArrayList();
    var dialog: LoadingDailog? = null
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    private var newPriavate: EosPrivateKey? = null
    private var tipsSureDialog: TipsSureDialog? = null
    private var redPackageDialog: RedPackageDialog? = null

    @Autowired
    @JvmField
    var redText: String = ""

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): NewRedEnvelopesView {
        return this
    }


    override fun byId(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setWindowStatusBarColor(this@NewRedEnvelopesActivity)
            changStatusIconCollor(false)
        }
        return R.layout.activity_new_red_envelopes
    }


    override fun errAccount(message: String) {
        super.errAccount(message)
        dialog?.dismiss()
        Toast.makeText(this@NewRedEnvelopesActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun CreateSuccess(newPriavateKey: EosPrivateKey, id: Long, accountName: String) {
        super.CreateSuccess(newPriavateKey, id, accountName)
        dialog?.dismiss()
        newPriavate = newPriavateKey
        //创建成功后清空粘贴板
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, "")
        clipboard.primaryClip = clipData
        Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_envelopes_txt_success), Toast.LENGTH_LONG).show()
        dialog?.show()
        presenter.getAccount(newPriavate?.publicKey!!.toString("BOS"))

    }


    override fun onAccountError() {
        dialog?.dismiss()
        //获取账户信息失败
        Toast.makeText(this, getString(R.string.get_account_error), Toast.LENGTH_LONG).show()
    }

    /**
     * 通过公钥获取账号名,并且获取所有的公钥已经权限
     */
    override fun onAccount(account: ArrayList<String>) {
        //获取账户信息成功
        presenter.getLocalAccount(account)
    }

    override fun importSuccess() {
        Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_envelopes_txt_imotu), Toast.LENGTH_LONG).show()
        ARouter.getInstance().build(Constants.RoutePath.MAIN_ACTIVITY).navigation()

    }

    override fun importError() {
        Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_envelopes_txt_manual), Toast.LENGTH_LONG).show()
    }

    //得到账号，用户输入密码进行导入
    override fun localAccount(accountTables: java.util.ArrayList<Pair<String, AccountTable?>>, date: java.util.ArrayList<String>) {
        dialog?.dismiss()
        this.accountTables.clear()
        this.listpublic.clear()
        this.listpublic.addAll(date)
        this.accountTables.addAll(accountTables)
        pwdView?.decryption(shared.getPwdVerify(), "verify")
    }

    override fun localAccountError(message: String) {
        dialog?.dismiss()
        //获取账户信息失败
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * 验证账号没有被注册,保存到本地数据库
     */
    override fun AddTransferAccounts(isAccount: Boolean) {
        super.AddTransferAccounts(isAccount)

        if (isAccount) {
            tipsSureDialog = TipsSureDialog.newInstance(resources.getString(R.string.new_red_diog_content_is), resources.getString(R.string.new_red_diog_import), resources.getString(R.string.red_record_add))

            tipsSureDialog?.tipsSureCallback = object : TipsSureDialog.TipsSureCallback {
                override fun onCancelLeftClick() {
                    tipsSureDialog?.dismiss()
                    dialog?.dismiss()
                    ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.ACCOUNT_LIST_ACTIVITY)
                            .navigation(context())
                }

                override fun onSureRightClick() {
                    tipsSureDialog?.dismiss()
                    var addPrivate = EosPrivateKey()
                    var establishAccountTable = EstablishAccountTable()
                    establishAccountTable.publicKey = addPrivate.publicKey.toString(Constants.Const.WALLETTYPE)
                    establishAccountTable.privateKey = addPrivate.toString()
                    establishAccountTable.accountName = new_red_edit_account.text.toString().trim()
                    establishAccountTable.time = System.currentTimeMillis().toString()
                    presenter.AddAccount(establishAccountTable)
                }

            }
            tipsSureDialog?.show(supportFragmentManager, "AccountList")
        } else {
            var addPrivate = EosPrivateKey()
            var establishAccountTable = EstablishAccountTable()
            establishAccountTable.publicKey = addPrivate.publicKey.toString(Constants.Const.WALLETTYPE)
            establishAccountTable.privateKey = addPrivate.toString()
            establishAccountTable.accountName = new_red_edit_account.text.toString().trim()
            establishAccountTable.time = System.currentTimeMillis().toString()
            presenter.AddAccount(establishAccountTable)
        }

    }

    override fun getlocalSuccess(establishAccountTable: EstablishAccountTable) {
        super.getlocalSuccess(establishAccountTable)
        dialog?.dismiss()
        ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.CREATE_BOS_ACCOUNT_ACTIVITY)
                .withString("accountName", new_red_edit_account.text.toString().trim())
                .withString("privateKey", establishAccountTable.privateKey)
                .navigation()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ARouter.getInstance().inject(this)
        if (!TextUtils.isEmpty(redText)) {
            update_power_txt_strand.setText(redText + "")
        }

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
                presenter.writeDB(accountTables, newPriavate!!, pwd, listpublic)
            }
        }
    }

    override fun listener() {
        super.listener()

        back.setOnClickListener {
            finish()
        }

        update_power_txt_key.setOnClickListener {
            if (!TextUtils.isEmpty(update_power_txt_key.text.toString().trim())) {
                var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setText(update_power_txt_key.text.toString().trim());
                Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_envelopes_cope), Toast.LENGTH_LONG).show()
            }
        }
        new_red_copy.setOnClickListener {
            if (TextUtils.isEmpty(new_red_edit_account.text.toString().trim()) || new_red_edit_account.text.toString().trim().length != 12) {
                Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_envelopes_err_acctont_leng), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;
            cm.setText(new_red_edit_account.getText()?.toString() + "");
            Toast.makeText(context(), resources.getString(R.string.new_red_envelopes_cope), Toast.LENGTH_LONG).show()

        }

        //点击导入账号
        new_red_have.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.ACCOUNT_LIST_ACTIVITY)
                    .navigation(this@NewRedEnvelopesActivity, 200)
        }




        new_red_btn.setOnClickListener { it ->

            if (TextUtils.isEmpty(new_red_edit_account.text.toString().trim()) || new_red_edit_account.text.toString().trim().length != 12) {
                Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_envelopes_err_acctont_leng), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(update_power_txt_strand.text.toString().trim())) {
                dialog?.show()
                presenter.getIsAccount(new_red_edit_account.text.toString().trim())

            } else {
                if(isContainChinese(update_power_txt_strand.text.toString().trim())){
                    Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_enve_err_txt_retry), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }


                var receive =try {
                    String(Base58.decode(update_power_txt_strand.text.toString().trim()))
                }catch (e:Exception){
                    ""
                }


                //使用红包串创建
                var strandList =
                        try {
                            receive.split("^")

                        } catch (e: Exception) {
                            ArrayList<String>()
                        }
                if (strandList.size == 3) {
                    var type: Int = try {
                        strandList[0].toInt()
                    } catch (e: Exception) {
                        0
                    }
                    //创建账号红包
                    if (type == 3) {
                        dialog?.show()
                        presenter.redEstablishAccount(strandList[2], strandList[1], new_red_edit_account.text.toString().trim())
                    } else if (type == 1 || type == 2) {//等于随机红包和普通红包

                        redPackageDialog = RedPackageDialog.newInstance(update_power_txt_strand.text.toString().trim())
                        redPackageDialog?.getRedEnvelopeCallback = object : RedPackageDialog.GetRedEnvelopeCallback {
                            override fun get(redText: String) {
                                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RECEIVE_RED_ENVELOPE_ACTIVITY).withString("redText", redText).navigation()
                            }
                        }
                        redPackageDialog?.show(supportFragmentManager, "redPackage")

                    } else {
                        Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_enve_err_txt_retry), Toast.LENGTH_LONG).show()

                    }

                } else {
                    Toast.makeText(this@NewRedEnvelopesActivity, resources.getString(R.string.new_red_enve_err_txt_retry), Toast.LENGTH_LONG).show()
                }

            }


        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun setWindowStatusBarColor(activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = activity.resources.getColor(R.color.new_red_title)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            //底部导航栏
            //window.setNavigationBarColor(activity.getResources().getColor(colorResId))


        }

    }

    //设置标题栏颜色  true黑色  flase是白色
    fun changStatusIconCollor(setDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = getWindow()?.getDecorView()
            if (decorView != null) {
                var vis = decorView!!.getSystemUiVisibility()
                if (setDark) {
                    vis = vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    vis = vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                decorView.systemUiVisibility = vis
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 200) {
            setResult(Activity.RESULT_OK)
            finish()
        }

    }




    fun isContainChinese( str:String):Boolean {

        var p = Pattern.compile("[\\u4e00-\\u9fa5]");
        var m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }



}
