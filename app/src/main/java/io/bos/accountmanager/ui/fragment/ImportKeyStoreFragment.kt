package io.bos.accountmanager.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.ImportCallback
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.ui.AbstractBosFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.ec.EosPrivateKey
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import kotlinx.android.synthetic.main.activity_import_key_storefragment.*
import rx.Observable
import rx.Subscription
import java.util.*

/**
 * KeyStore导入
 */

@Route(path = Constants.RoutePath.FRAGMENT.IMPORT_KEYSTORE_FRAGMENT)
class ImportKeyStoreFragment : AbstractBosFragment() {
    override fun fragmentLayout(): Int {
        return R.layout.activity_import_key_storefragment
    }

    var callback: ImportCallback? = null

    private var sub: Subscription? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val keyStoreStr = RxTextView.textChanges(import_edit_key)
        val keyStorePwd = RxTextView.textChanges(import_edit_pwd)
        sub = Observable.combineLatest(keyStorePwd, keyStoreStr) { t1, t2 ->
            !(TextUtils.isEmpty(t1) && TextUtils.isEmpty(t2))
        }.subscribe {
            RxView.enabled(import_store_account).call(it)
        }

        import_store_account.setOnClickListener {
            Run(object : Callback<KeyStoreResult> {
                override fun call(): KeyStoreResult {
                    val pwd = Sha512.from(import_edit_pwd.text.toString().toByteArray())
                    val result = CryptUtil.aesDecrypt(Arrays.copyOf(pwd.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(import_edit_key.text.toString()), PreferencesHelper.getIv(pwd))
                            ?: return KeyStoreResult("", "KeyStore 密码错误", false)
                    return KeyStoreResult(EosPrivateKey(String(result)).toWif(), "", true)
                }
            }).rxJava()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe {
                        import_store_account.isEnabled = false
                    }
                    .doOnComplete {
                        import_store_account.isEnabled = true
                    }
                    .subscribe({
                        if (it.success) {
                            callback?.privateKey(it.privateKey)
                        } else {
                            Toast.makeText(context, resources.getString(R.string.err_txt_key_format), Toast.LENGTH_LONG).show()
                        }
                    }, {
                        it.printStackTrace()
                        Toast.makeText(context, resources.getString(R.string.err_txt_key_format), Toast.LENGTH_LONG).show()
                    })
        }

        import_stort_help.setOnClickListener {

            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.WEB_VIEW_ACTIVITY)
                    .withString("url", "https://www.boscore.io/index.html")
                    .withString("title", resources.getString(R.string.import_txt_help)).navigation()
        }


    }

    override fun onDestroy() {
        sub?.unsubscribe()
        super.onDestroy()
    }

    class KeyStoreResult(val privateKey: String, val error_info: String, val result: Boolean) : Result(result, error_info)
}
