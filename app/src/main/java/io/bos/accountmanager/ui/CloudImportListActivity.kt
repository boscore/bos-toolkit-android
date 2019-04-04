package io.bos.accountmanager.ui

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.CloudStoragePresenter
import io.bos.accountmanager.ui.dialog.PwdDialogCloud
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.view.ICloudStorageView
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_cloud_import_list.*
import org.json.JSONArray
import java.util.*

/**
 * 云端导入列表
 */
@Route(path = Constants.RoutePath.ACTIVITY.CLOUD_IMPORT_LIST_ACTIVITY)
class CloudImportListActivity : AbstractActivity<ICloudStorageView, CloudStoragePresenter>(), ICloudStorageView {
    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): ICloudStorageView = this

    override fun byId(): Int = R.layout.activity_cloud_import_list

    var pwdView: PwdView? = null

    var sharedHelper: PreferencesHelper? = null

    var pwd: String? = null

    private var dataSource: MutableList<AccountTable> = ArrayList<AccountTable>()
    private val keysLocal: MutableList<CloudStoragePresenter.EnDataResult> = ArrayList<CloudStoragePresenter.EnDataResult>()
    private val keysCloud: MutableList<CloudStoragePresenter.EnDataResult> = ArrayList<CloudStoragePresenter.EnDataResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.setEnableLoadMore(false)
        sharedHelper = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(sharedHelper!!, this)
        pwdView?.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                //确认导出
                this@CloudImportListActivity.pwd = pwd
                presenter.exportPrivateKey(pwd, dataSource)
            }
        }
        cloud_btn_import.setOnClickListener {
            pwdView?.decryption(sharedHelper!!.getPwdVerify(), "backup")
        }
        swipe.isRefreshing = true
        presenter.getLocalAccounts()
    }

    override fun showPwd(listener: PwdDialogCloud.PwdCallback) {
        val pwdView = PwdDialogCloud()
        pwdView.pwdCallback = listener
        pwdView.show(supportFragmentManager, "showCloudPwd")
    }

    override fun onLocalAccounts(accounts: List<AccountTable>) {
        dataSource.clear()
        dataSource.addAll(accounts)
        presenter.getCloudAccounts()
    }

    override fun onCloudAccounts(keys: List<CloudStoragePresenter.EnDataResult>) {
        swipe.isRefreshing = false
        keysCloud.clear()
        keysCloud.addAll(keys)
        for (i in 0 until dataSource.size) {
            keysCloud.forEach {
                if (it.accountName.equals(dataSource[i].accountName)) {
                    //账号名相同，判断公钥是否完全一致
                    val jsonArray = JSONArray(dataSource[i].publicKey)
                    var equalsCount = 0
                    it.enData.forEach { keysBean ->
                        for2@ for (j in 0 until jsonArray.length()) {
                            val key = jsonArray.optString(j)
                            if (keysBean.publicKey.equals(key)) {
                                equalsCount++
                                break@for2
                            }
                        }
                    }
                    it.isLocal = equalsCount == it.enData.size
                    return@forEach
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onExportPrivateKey(keys: List<CloudStoragePresenter.EnDataResult>) {
        keysLocal.clear()
        keysLocal.addAll(keys)
        presenter.importFromCloud(keysLocal, keysCloud, pwd!!, Consumer {

        }, Consumer {

        })
    }

    private val adapter = object : BaseItemDraggableAdapter<CloudStoragePresenter.EnDataResult, BaseViewHolder>(R.layout.item_cloud_import_list, keysCloud) {
        override fun convert(helper: BaseViewHolder?, item: CloudStoragePresenter.EnDataResult?) {
            val item_cloud_txt_name = helper!!.getView<TextView>(R.id.item_cloud_txt_name)
            val item_cloud_txt_isguide = helper!!.getView<TextView>(R.id.item_cloud_txt_isguide)
            val item_cloud_select = helper!!.getView<ImageView>(R.id.item_cloud_select)
            item_cloud_txt_name.text = item?.accountName
            if (item?.isLocal!!) {
                item_cloud_txt_isguide.visibility = View.VISIBLE
            } else {
                item_cloud_txt_isguide.visibility = View.GONE
            }
        }
    }

}
