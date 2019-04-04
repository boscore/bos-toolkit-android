package io.bos.accountmanager.ui

import android.app.Activity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.tu.loadingdialog.LoadingDailog
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
import kotlinx.android.synthetic.main.activity_cloud_management.*
import org.json.JSONArray

/**
 * 云端管理
 */
@Route(path = Constants.RoutePath.ACTIVITY.CLOUD_MANAGEMENT_ACTIVITY)
class CloudManagementActivity : AbstractActivity<ICloudStorageView, CloudStoragePresenter>(), ICloudStorageView {

    private val dataSource: ArrayList<AccountTable> = ArrayList<AccountTable>()
    private val keysLocal: MutableList<CloudStoragePresenter.EnDataResult> = ArrayList<CloudStoragePresenter.EnDataResult>()
    private val keysCloud: MutableList<CloudStoragePresenter.EnDataResult> = ArrayList<CloudStoragePresenter.EnDataResult>()
    private val showList: MutableList<Map<String, String>> = ArrayList()
    var dialog: LoadingDailog? = null

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): ICloudStorageView {
        return this
    }

    override fun byId(): Int = R.layout.activity_cloud_management
    var pwd: String? = null
    var pwdView: PwdView? = null
    var sharedHelper: PreferencesHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        back.setOnClickListener { finish() }
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        sharedHelper = BOSApplication.get(this).getAppComponent().preferences()
        pwdView = PwdView(sharedHelper!!, this)
        pwdView?.callback = object : PwdView.PwdViewCallback {
            override fun success(data: String, tag: Any, pwd: String) {
                //确认导出
                this@CloudManagementActivity.pwd = pwd
                presenter.exportPrivateKey(pwd, dataSource)
            }
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.setEnableLoadMore(false)
        swipe.setOnRefreshListener {
            presenter.getLocalAccounts()
        }
        cloud_btn_backup.setOnClickListener {
            pwdView?.decryption(sharedHelper!!.getPwdVerify(), "backup")
        }
        cloud_management_cancel.setOnClickListener {
            dialog?.show()
            presenter.logout(Consumer { logout ->
                dialog?.dismiss()
                if (logout.isLogout) {
                    finish()
                } else {

                }
            }, Consumer {
                dialog?.dismiss()
            })
        }
        swipe.isRefreshing = true
        presenter.getLocalAccounts()
    }

    override fun onLocalAccounts(accounts: List<AccountTable>) {
        dataSource.clear()
        dataSource.addAll(accounts)
        presenter.getCloudAccounts()
    }

    override fun onCloudAccounts(keys: List<CloudStoragePresenter.EnDataResult>) {
        swipe.isRefreshing = false
        showList.clear()
        keysCloud.clear()
        keysCloud.addAll(keys)
        for (i in 0 until dataSource.size) {
            dataSource[i].backup = false
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
                    dataSource[i].backup = equalsCount == it.enData.size
                    return@forEach
                }
            }
            //添加本地数据
            val map = HashMap<String, String>()
            map.put("account", dataSource[i].accountName)
            map.put("backup", dataSource[i].backup.toString())
            map.put("import", true.toString())
            showList.add(map)
        }
        for (i in 0 until keysCloud.size) {
            if (dataSource.find {
                        TextUtils.equals(it.accountName, keysCloud[i].accountName)
                    } == null) {
                val map = HashMap<String, String>()
                map.put("account", keysCloud[i].accountName)
                map.put("backup", true.toString())
                map.put("import", false.toString())
                showList.add(map)
            }
        }
        if (showList.isEmpty()) {
            Toast.makeText(context(), getString(R.string.no_account), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        presenter.uploadBackupStatus(dataSource)
        adapter.notifyDataSetChanged()
    }

    fun showProgress(message: String) {
        dialog?.show()
    }

    fun hideProgress() {
        dialog?.dismiss()
    }

    override fun showPwd(listener: PwdDialogCloud.PwdCallback) {
        swipe.isRefreshing = false
        val pwdView = PwdDialogCloud()
        pwdView.pwdCallback = object : PwdDialogCloud.PwdCallback {
            override fun onPwd(pwd: String) {
                listener.onPwd(pwd)
            }

            override fun onDismiss() {
                finish()
            }

        }
        pwdView.show(supportFragmentManager, "showCloudPwd")
    }

    override fun onDestroy() {
        super.onDestroy()


    }

    override fun onExportPrivateKey(keys: List<CloudStoragePresenter.EnDataResult>) {
        keysLocal.clear()
        keysLocal.addAll(keys)
        showProgress("")
        presenter.synchronization(keysLocal, keysCloud, pwd!!, Consumer {
            //备份到云端成功
            if (it.result) {
                //导入所有数据到本地
                presenter.importFromCloud(keysLocal, keysCloud, pwd!!, Consumer {
                    //导入成功
                    hideProgress()
                    Toast.makeText(context(), getString(R.string.synchronization_success), Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()

                }, Consumer {
                    //失败
                    hideProgress()
                    Toast.makeText(context(), getString(R.string.synchronization_error), Toast.LENGTH_SHORT).show()
                })
            } else {
                //失败
                hideProgress()
                Toast.makeText(context(), getString(R.string.synchronization_error), Toast.LENGTH_SHORT).show()
            }
        }, Consumer {
            //失败
            hideProgress()
            Toast.makeText(context(), getString(R.string.synchronization_error), Toast.LENGTH_SHORT).show()
        })
    }


    private val adapter = object : BaseItemDraggableAdapter<Map<String, String>, BaseViewHolder>(R.layout.item_cloud_management_list, showList) {
        override fun convert(helper: BaseViewHolder?, item: Map<String, String>?) {
            val item_cloud_manage_select = helper!!.getView<ImageView>(R.id.item_cloud_manage_select)
            val item_cloud_manage_relative = helper.getView<ConstraintLayout>(R.id.item_cloud_manage_relative)
            val item_cloud_manage_txt_name = helper.getView<TextView>(R.id.item_cloud_manage_txt_name)
            val item_cloud_manage_txt_isguide = helper.getView<TextView>(R.id.item_cloud_manage_txt_isguide)
            val item_cloud_manage_txt_isimport = helper.getView<TextView>(R.id.item_cloud_manage_txt_isimport)
            item_cloud_manage_txt_name.text = item!!.get("account")
            if (item.get("backup")!!.toBoolean()) {
                item_cloud_manage_txt_isguide.visibility = View.VISIBLE
            } else {
                item_cloud_manage_txt_isguide.visibility = View.GONE
            }

            if (item.get("import")!!.toBoolean()) {
                item_cloud_manage_txt_isimport.visibility = View.VISIBLE
            } else {
                item_cloud_manage_txt_isimport.visibility = View.GONE
            }

        }
    }

}
