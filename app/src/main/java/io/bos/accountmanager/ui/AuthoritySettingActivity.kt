package io.bos.accountmanager.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.presenter.WalletManagePresenter
import io.bos.accountmanager.view.WalletManageView
import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_authority_setting.*

/**
 * 更改权限
 */
@Route(path = Constants.RoutePath.ACTIVITY.AUTHORITY_SETTING_ACTIVITY)
class AuthoritySettingActivity : AbstractActivity<WalletManageView, WalletManagePresenter>(), WalletManageView {
    // 账户名
    @Autowired
    lateinit var accountName: String
    var dialog: LoadingDailog? = null
    private var dataSource: ArrayList<PermissionsBean> = ArrayList<PermissionsBean>()
    private var accountTable: AccountTable? = null;

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): WalletManageView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_authority_setting
    }


    override fun getAuthorityList(listBaifen: ArrayList<PermissionsBean>) {
        super.getAuthorityList(listBaifen)
        if (!listBaifen.isEmpty()) {
            dataSource.clear()
            dataSource.addAll(listBaifen)
            adapter.notifyDataSetChanged()
        }


        dialog?.dismiss()
    }

    override fun getErrAuthority(message: String) {
        super.getErrAuthority(message)
        dialog?.dismiss()
        Toast.makeText(context(), message + "", Toast.LENGTH_LONG).show();

    }

    //获取账号详情
    override fun onAccounts(accounts: AccountTable) {
        super.onAccounts(accounts)
        accountTable = accounts
        //获取所有权限
        presenter.getAuthority(accountName)

    }

    //获取失败
    override fun errMessage(message: String) {
        super.errMessage(message)
        Toast.makeText(context(), message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        authority_txt_name.text = accountName
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        dialog?.show()
        authority_recycler.setHasFixedSize(true);
        authority_recycler.setNestedScrollingEnabled(false);
        authority_recycler.layoutManager = LinearLayoutManager(this)
        authority_recycler.adapter = adapter
        adapter.setEnableLoadMore(false)
        presenter.accounts(accountName)
//        //更改
//        authority_btn_owner_update.setOnClickListener{
//            var inte= Intent(this,UpdatePowerActivity::class.java)
//            startActivity(inte)
//        }
    }

    override fun listener() {
        super.listener()

        back.setOnClickListener {
            finish()
        }

    }

    override fun init() {
        super.init()

    }

    override fun data() {
        super.data()

    }


    private val adapter = object : BaseItemDraggableAdapter<PermissionsBean, BaseViewHolder>(R.layout.item_authority_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: PermissionsBean?) {
//            val item_cloud_select = helper!!.getView<ImageView>(R.id.item_cloud_select)
            val item_authority_txt_name = helper!!.getView<TextView>(R.id.item_authority_txt_name)
            val item_authority_contene = helper!!.getView<TextView>(R.id.item_authority_contene)
            val item_authority_btn_update = helper!!.getView<TextView>(R.id.item_authority_btn_update)


            item_authority_txt_name.text = String.format("%s Key", item?.perm_name)
            item_authority_contene.text = item?.keys
            item_authority_btn_update.setOnClickListener {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.UPDATE_POWER_ACTIVITY).withString("accountName", accountName).withString("bean", Gson().toJson(item)).navigation()
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val udateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
//            dd = intent?.getStringExtra("value")!!

        }

    }
}
