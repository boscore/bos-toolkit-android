package io.bos.accountmanager.ui

import android.content.ClipboardManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.ExportKeytTipsPresenter
import io.bos.accountmanager.presenter.NewRedEnvelopesPresenter
import io.bos.accountmanager.view.ExportKeytTipsView
import io.bos.accountmanager.view.NewRedEnvelopesView
import kotlinx.android.synthetic.main.activity_export_private_key.*

/**
 * 导出私钥复制(在管理中心选择导出私钥后打开的界面)
 */

@Route(path = Constants.RoutePath.ACTIVITY.EXPORT_PRIVATE_KEY_ACTIVITY)
class ExportPrivateKeyActivity : AbstractActivity<ExportKeytTipsView, ExportKeytTipsPresenter>(), ExportKeytTipsView {
    // 要导出的秘钥
    @Autowired
    @JvmField
    var key: String = ""

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
        return R.layout.activity_export_private_key
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        back.setOnClickListener {
            finish()
        }
        if (type == 0) {
            tool_title.text = resources.getString(R.string.export_key_tite)
        } else {
            tool_title.text = resources.getString(R.string.manage_txt_store)
        }
        export_key_text.text = key

        export_copy.setOnClickListener {
            var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setText(export_key_text.text.toString().trim());
            Toast.makeText(this@ExportPrivateKeyActivity, resources.getString(R.string.new_red_envelopes_cope), Toast.LENGTH_LONG).show()
        }


    }
}
