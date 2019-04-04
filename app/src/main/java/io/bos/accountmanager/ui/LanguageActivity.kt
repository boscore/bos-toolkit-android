package io.bos.accountmanager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.utils.LocalManageUtil
import kotlinx.android.synthetic.main.activity_language.*

/**
 * 语言设置
 */
@Route(path = Constants.RoutePath.ACTIVITY.LANGUAGE_CHANGE_ACTIVITY)
class LanguageActivity : AbstractBosActivity() {
    override fun byId(): Int {
        return R.layout.activity_language
    }

    private var preferences: SharedPreferences? = null

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        preferences = getSharedPreferences(Constants.Const.LANGUAGE, Context.MODE_PRIVATE)

        if (TextUtils.equals(LocalManageUtil.getSelectLanguage(this), "ENGLISH")) {
            language_image_en.visibility = View.VISIBLE
            language_image_zh.visibility = View.GONE
        } else if (TextUtils.equals(LocalManageUtil.getSelectLanguage(this), "中文")) {
            language_image_en.visibility = View.GONE
            language_image_zh.visibility = View.VISIBLE
        }
        zh_layout.setOnClickListener({
            selectLanguage(1)
        })
        en_layout.setOnClickListener({
            selectLanguage(3)

        })

        back.setOnClickListener({
            finish()
        })
    }

    private fun selectLanguage(select: Int) {
        LocalManageUtil.saveSelectLanguage(this, select)
        if (intent?.getIntExtra("type", 1) == 0) {
            HomeActivity.reStart(this, 0)
        } else {
            HomeActivity.reStart(this, 1)

        }
    }


}
