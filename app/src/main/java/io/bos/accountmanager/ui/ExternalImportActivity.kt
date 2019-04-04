package io.bos.accountmanager.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.tu.loadingdialog.LoadingDailog
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.net.bean.CloudBean
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.ExternalImportPresenter
import io.bos.accountmanager.ui.dialog.PwdView
import io.bos.accountmanager.ui.dialog.SelectPrivateKeyDialog
import io.bos.accountmanager.view.ExternalImportView
import io.starteos.jeos.crypto.util.Base58
import kotlinx.android.synthetic.main.activity_external_import.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

/**
 * 第三方拉起app列表
 */
@Route(path = Constants.RoutePath.ACTIVITY.EXTERNAL_IMPORT_ACTIVITY)
class ExternalImportActivity : AbstractActivity<ExternalImportView, ExternalImportPresenter>(), ExternalImportView {
    private var dataSource: ArrayList<CloudBean> = ArrayList<CloudBean>()
    private var importList: ArrayList<CloudBean> = ArrayList<CloudBean>()//选择的账户名
    private var pwdView: PwdView? = null
    private lateinit var shared: PreferencesHelper
    var dialog: LoadingDailog? = null
    private var getName: String = ""//调用的方法名称
    private var dialogSelect: SelectPrivateKeyDialog? = null  //选择私钥弹出框

    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): ExternalImportView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_external_import
    }

    override fun getAccontList(cloudBean: ArrayList<CloudBean>) {
        super.getAccontList(cloudBean)
        dataSource.clear()
        dataSource.addAll(cloudBean)
        if (dataSource.size > 0) {
            extemal_image.visibility = View.GONE
        } else {
            extemal_image.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()

    }


    override fun exportPriavteList(date: ArrayList<SecretKeyBean>) {
        super.exportPriavteList(date)
        dialog?.dismiss()
        var jsondate = Gson().toJson(date)

        dialogSelect = SelectPrivateKeyDialog.newInstance(jsondate)
        dialogSelect?.selectPrivateKeyCallback = object : SelectPrivateKeyDialog.SelectPrivateKeyCallback {
            override fun onDetermine(select: Int) {
                //确认导出
                dialogSelect?.dismiss()
                var dataObjs = JSONObject()
                dataObjs.put("action", getName)
//                dataObjs.put("type", "BOS")
                var dataArray = JSONArray()
                var dataObjet = JSONObject()
                dataObjet.put("account_name", importList[0].accountName)
                var keysObjst = JSONArray()
                keysObjst.put(date[select].privateKey)
                dataObjet.put("keys", keysObjst)
                dataObjet.put("ype", "BOS")
                dataArray.put(dataObjet)
                dataObjs.put("data", dataArray)
                var intent = Intent();
                intent.putExtra("data", Base58.encode(dataObjs.toString().toByteArray()));
                setResult(Activity.RESULT_OK, intent); //设置返回数据
                finish();
            }
        }
        dialogSelect?.show(supportFragmentManager, "select")


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_external_import)
        setWindowStatusBarColor(this@ExternalImportActivity)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        adapter.setEnableLoadMore(false)
        presenter.getAccpountlist()
        extemal_image_close.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, intent); //设置返回数据
            finish()
        }
        extemal_btn_import.setOnClickListener {
            importList.clear()
            for (i in 0 until dataSource.size) {
                if (dataSource[i].isSelect) {
                    importList.add(dataSource[i])
                }
            }
            if (importList.size > 0) {
                pwdView?.decryption(shared.getPwdVerify(), "verify")
            } else {
                Toast.makeText(context(), resources.getString(R.string.external_txt_select_account), Toast.LENGTH_LONG).show()
            }


        }
        getAppData()

    }

    override fun listener() {
        super.listener()
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
                dialog?.show()
                presenter.exportPrivateKey(importList[0], pwd)

            }
        }
    }

    fun getAppData() {
        var intent = getIntent();

        if (intent != null) {
            val uri = intent.data
            if (uri != null) {
                val scheme = uri.scheme//csd
                val host = uri.host//pull.csd.demo
                val path = uri.path///cyn
                if (TextUtils.isEmpty(uri.getQueryParameter("action"))) {
                    finish()
                } else {
                    getName = uri.getQueryParameter("action")
                }


            }

        }
    }


    private val adapter = object : BaseItemDraggableAdapter<CloudBean, BaseViewHolder>(R.layout.item_extemal_list, dataSource) {
        override fun convert(helper: BaseViewHolder?, item: CloudBean?) {
            val item_extemal_name = helper!!.getView<AppCompatTextView>(R.id.item_extemal_name)
            val item_extemal_balance = helper!!.getView<AppCompatTextView>(R.id.item_extemal_balance)
            val item_extemal_selct = helper!!.getView<AppCompatImageView>(R.id.item_extemal_selct)
            item_extemal_name.text = item?.accountName
            item_extemal_balance.text = item?.money

            if (item?.isSelect!!) {
                item_extemal_selct.setImageResource(R.drawable.account_icon_pitchon_default)
            } else {
                item_extemal_selct.setImageResource(R.drawable.account_icon_select_default)
            }

            item_extemal_selct.setOnClickListener {

                var keyName = Gson().fromJson<ArrayList<PermissionsBean>>(item?.publicName, object : com.google.common.reflect.TypeToken<ArrayList<PermissionsBean>>() {}.type)
                var lsitKey = Gson().fromJson<ArrayList<String>>(item?.publicKey, object : com.google.common.reflect.TypeToken<ArrayList<String>>() {}.type)
                var isflage = false
                for (i in 0 until keyName.size) {
                    for (j in 0 until lsitKey.size) {
                        if (TextUtils.equals(keyName[i].keys, lsitKey[j])) {
                            if (TextUtils.equals(keyName[i].perm_name, "active")) {
                                isflage = true
                                break
                            }
                        }
                    }

                }

                if (isflage) {
                    for (j in 0 until dataSource.size) {
                        dataSource[j].isSelect = false
                    }
                    dataSource[helper.adapterPosition].isSelect = true

                    notifyDataSetChanged()

                } else {
                    Toast.makeText(context(), resources.getString(R.string.external_txt_import_key), Toast.LENGTH_LONG).show()
                }


            }

        }


    }


    fun setWindowStatusBarColor(activity: Activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(R.color.colorWhite));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //底部导航栏
            //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
        }

    }

}
