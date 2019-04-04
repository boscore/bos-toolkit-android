package io.bos.accountmanager.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxCompoundButton
import com.jakewharton.rxbinding.widget.RxTextView
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import kotlinx.android.synthetic.main.activity_found_account.*
import rx.Observable
import rx.Subscription

/**
 * 创建密码
 */

@Route(path = Constants.RoutePath.ACTIVITY.FOUND_ACCOUNT_ACTIVITY)
class FoundAccountActivity : AbstractBosActivity() {
    override fun byId(): Int {
        return R.layout.activity_found_account
    }

    private var password: String = ""
    private var tips: String = ""

    private var from: Subscription? = null
    private var focus: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pwdObserver = RxTextView.textChanges(found_edit_pwd)
        val pwdConfirmObserver = RxTextView.textChanges(found_edit_confirm)
        val protocolObserver = RxCompoundButton.checkedChanges(found_check_service)
        val tipObserver = RxTextView.textChanges(found_edit_optional)
        focus = RxView.focusChanges(found_edit_pwd)
                .subscribe {
                    rank.visibility = if (it) View.VISIBLE else View.GONE
                }
        from = Observable.combineLatest(pwdConfirmObserver, pwdObserver, protocolObserver, tipObserver) { t1, t2, t3, t4 ->
            if (found_edit_pwd.isFocused) {
                var rankNumber = 0
                val upperCase = t2.toString().matches(Regex(".*[A-Z]+.*"))
                if (upperCase) {
                    rankNumber++
                }
                val lowerCase = t2.toString().matches(Regex(".*[a-z]+.*"))
                if (lowerCase) {
                    rankNumber++
                }
                val o = t2.toString().matches(Regex(".*[`~!@#$%^&*()_\\-+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，？]+.*"))
                val number = t2.toString().matches(Regex(".*[0-9]+.*"))
                if (number || o) {
                    rankNumber++
                }

                val rankStr = when (rankNumber) {
                    1 -> {
                        Pair(getString(R.string.weak_str), Color.BLUE)
                    }
                    2 -> {
                        Pair(getString(R.string.medium_str), Color.GREEN)
                    }
                    3 -> {
                        Pair(getString(R.string.strong_str), Color.RED)
                    }
                    else -> {
                        Pair("", Color.TRANSPARENT)
                    }
                }
                rank.text = rankStr.first
                rank.setTextColor(rankStr.second)


            }
            val text = TextUtils.equals(t1, t2)
            val length = t1.length in 8..16
            val eq = !TextUtils.equals(t1, t4)
            if (text == false) {
                found_login.text = resources.getString(R.string.pwd_update_err_identical)
            } else if (length == false) {
                found_login.text = resources.getString(R.string.pwd_txt_length_err)
            } else if (eq == false) {
                found_login.text = resources.getString(R.string.pwd_txt_tips_identical)
            } else {
                found_login.text = resources.getString(R.string.found_btn_next)
            }

            if (text && eq && length) {
                password = t1.toString()
            }

            text && t3!! && eq && length
        }.subscribe {
            RxView.enabled(found_login).call(it)
        }

        found_login.setOnClickListener {
            if (!TextUtils.equals(found_edit_pwd.text?.toString(), found_edit_confirm.text?.toString())) {
            }
            tips = found_edit_optional.text.toString()
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.SECRET_SECURITY_ACTIVITY)
                    .withString("password", password)
                    .withString("tips", tips)
                    .navigation(this, 202)
        }

        back.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        from?.unsubscribe()
        focus?.unsubscribe()
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 202 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
