package io.bos.accountmanager.ui.fragment

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.jakewharton.rxbinding.widget.RxTextView
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.ImportCallback
import io.bos.accountmanager.ui.AbstractBosFragment
import io.starteos.jeos.crypto.ec.EosPrivateKey
import kotlinx.android.synthetic.main.activity_import_private_keyfragment.*

/**
 * 导入私钥
 */
@Route(path = Constants.RoutePath.FRAGMENT.IMPORT_PRIVATE_KEY_FRAGMENT)
class ImportPrivateKeyfragment : AbstractBosFragment() {
    override fun fragmentLayout(): Int {
        return R.layout.activity_import_private_keyfragment
    }

    var callback: ImportCallback? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RxTextView.textChanges(import_key_edit)
                .subscribe {
                    try {
                        EosPrivateKey(it.toString().trim())
                        import_key_btn.isEnabled = true
                    } catch (e: Exception) {
                        import_key_btn.isEnabled = false
                    }
                }
        import_key_btn.setOnClickListener {
            callback?.privateKey(import_key_edit.text.toString().trim())
        }


        import_key_help.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.WEB_VIEW_ACTIVITY)
                    .withString("url", "https://www.boscore.io/index.html")
                    .withString("title", resources.getString(R.string.import_txt_help)).navigation()
        }


    }
}
