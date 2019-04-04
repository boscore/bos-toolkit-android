package io.bos.accountmanager.ui

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.MainActivity
import io.bos.accountmanager.R
import io.bos.accountmanager.data.local.prefs.PreferencesHelper
import io.bos.accountmanager.utils.LocalManageUtil
import kotlinx.android.synthetic.main.activity_home.*

/**
 *
 * 首页创建以及导入钱包
 * Created by Administrator on 2018/12/13/013.
 */
@Route(path = Constants.RoutePath.ACTIVITY.HOME_ACTIVITY)
class HomeActivity : AbstractBosActivity() {
    private lateinit var preferencesHelper: PreferencesHelper
    override fun byId(): Int {
        return R.layout.activity_home
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesHelper = BOSApplication.get(this).getAppComponent().preferences()
        transparencyBar(this)
        language_switch.text = "English"

        if (TextUtils.equals(LocalManageUtil.getSelectLanguage(this), "ENGLISH")) {
            language_switch.text = "中文"
        } else if (TextUtils.equals(LocalManageUtil.getSelectLanguage(this), "中文")) {
            language_switch.text = "English"
        }

        language_switch.setOnClickListener({
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.LANGUAGE_CHANGE_ACTIVITY).withInt("type", 0).navigation()
        })

    }

    override fun listener() {
        super.listener()
        create_layout.setOnClickListener {
            if (preferencesHelper.isSettingPwd()) {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.NEW_READ_ENVELOPERS_ACTIVITY).navigation(this, 204)
            } else {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.FOUND_ACCOUNT_ACTIVITY).navigation(this, 202)
            }
        }
        import_layout.setOnClickListener {
            if (preferencesHelper.isSettingPwd()) {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.IMPORT_ACCOUNT_ACTIVITY).navigation(this, 204)
            } else {
                ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.FOUND_ACCOUNT_ACTIVITY).navigation(this, 203)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 202 && resultCode == Activity.RESULT_OK) {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.NEW_READ_ENVELOPERS_ACTIVITY).navigation(this, 204)
        } else if (requestCode == 203 && resultCode == Activity.RESULT_OK) {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.IMPORT_ACCOUNT_ACTIVITY).navigation(this, 204)
        } else if (requestCode == 204 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            ARouter.getInstance().build(Constants.RoutePath.MAIN_ACTIVITY).navigation()
            finish()
        }
    }

    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */

    @TargetApi(19)
    fun transparencyBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    companion object {
        fun reStart(context: Context, type: Int) {
            var intent = Intent(context, MainActivity::class.java)
            if (type == 0) {
                intent = Intent(context, HomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

        }
    }

}