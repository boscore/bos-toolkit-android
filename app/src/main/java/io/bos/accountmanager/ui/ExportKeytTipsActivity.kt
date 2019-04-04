package io.bos.accountmanager.ui

import android.os.Bundle
import android.util.Log
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.ExportKeytTipsPresenter
import io.bos.accountmanager.view.ExportKeytTipsView
import kotlinx.android.synthetic.main.activity_export_keyt_tips.*

/**
 * 导出私钥时候提示
 */

@Route(path = Constants.RoutePath.ACTIVITY.EXPORT_KEYT_TIPS_ACTIVITY)
class ExportKeytTipsActivity : AbstractActivity<ExportKeytTipsView, ExportKeytTipsPresenter>(), ExportKeytTipsView {


    // 要导出的秘钥
    @Autowired
    lateinit var key: String
    // title
    @Autowired
    @JvmField
    var type: Int = 0   //0是导出私钥  1是导出keyStore


    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): ExportKeytTipsView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_export_keyt_tips
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        Log.e("TAG", "key====" + key)
        if (type == 0) {
            tool_title.text = resources.getString(R.string.export_key_tite)
        } else {
            tool_title.text = resources.getString(R.string.manage_txt_store)
        }
        back.setOnClickListener {
            finish()
        }

        export_key_click.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.EXPORT_PRIVATE_KEY_ACTIVITY)
                    .withString("key", key)
                    .withInt("type", type)
                    .navigation()

        }
    }
}
