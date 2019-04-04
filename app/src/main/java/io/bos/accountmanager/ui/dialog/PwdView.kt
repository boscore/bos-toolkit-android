package io.bos.accountmanager.ui.dialog

import android.text.TextUtils
import io.bos.accountmanager.core.run.Callback
import io.bos.accountmanager.core.run.Result
import io.bos.accountmanager.core.run.Run
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.ui.AbstractBosActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.starteos.jeos.crypto.digest.Sha512
import io.starteos.jeos.crypto.util.Base58
import io.starteos.jeos.crypto.util.CryptUtil
import java.util.*

open class PwdView constructor(val shared: PreferencesHelper, val activity: AbstractBosActivity) {


    var callback: PwdViewCallback? = null
    var hintDialogListener: HintDialogListener? = null

    private val pwdDialog: PwdDialog = PwdDialog()

    private var parameter: Pair<String, Any>? = null

    init {
        pwdDialog.pwdCallback = object : PwdDialog.PwdCallback {
            override fun onPwd(pwd: String) {
                val r = Run(object : Callback<PwdResult> {
                    override fun call(): PwdResult {
                        val pwd_tmp = Sha512.from(pwd.toByteArray())
                        val deData = CryptUtil.aesDecrypt(Arrays.copyOf(pwd_tmp.bytes, PreferencesHelper.ENCRYPT_KEY_LEN), Base58.decode(parameter!!.first), PreferencesHelper.getIv(pwd_tmp))
                                ?: return PwdResult("", parameter!!.second, true, "Password Error")
                        return PwdResult(String(deData), parameter!!.second, false, pwd = pwd)
                    }
                }).rxJava()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.newThread())
                        .doOnComplete {
                            pwdDialog.pwdStart()
                        }
                        .subscribe({
                            if (it.success) {
                                pwdDialog.pwdSuccess()
                                callback?.success(it.data, it.tag, it.pwd)
                                pwdDialog.dismiss()
                            } else {
                                hintDialogListener?.hint()

                                pwdDialog.pwdError(it.error)
                            }
                        }, {
                            hintDialogListener?.hint()

                            pwdDialog.pwdError(if (TextUtils.isEmpty(it.message)) "" else it.message!!)
                        })
            }

            override fun onTip(): String {
                return shared.getPwdTip()
            }
        }
        pwdDialog.hintDialogListener = object : PwdDialog.PwdHintDialogListener {
            override fun hint() {
                hintDialogListener?.hint()
            }
        }

    }


    fun decryption(cipher: String, tag: Any) {
        parameter = Pair(cipher, tag)
        pwdDialog.show(activity.supportFragmentManager, tag.toString())

    }

    interface PwdViewCallback {
        fun success(data: String, tag: Any, pwd: String)
    }

    interface HintDialogListener {
        fun hint()
    }

    class PwdResult(val data: String, val tag: Any, val reset: Boolean, val pwd: String, error: String = "") : Result(!TextUtils.isEmpty(data), error)
}