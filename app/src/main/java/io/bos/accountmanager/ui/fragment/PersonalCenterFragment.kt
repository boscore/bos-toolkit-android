package io.bos.accountmanager.ui.fragment

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.storage.StorageFactory
import io.bos.accountmanager.ui.AbstractBosFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_center.*

/**
 * 个人中心
 */
@Route(path = Constants.RoutePath.FRAGMENT.PERSONAL_CENTER_FRAGMENT)
class PersonalCenterFragment : AbstractBosFragment() {
    var dialog: LoadingDailog? = null
    override fun fragmentLayout(): Int {
        return R.layout.activity_personal_center
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loadBuilder = LoadingDailog.Builder(context)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        transparencyBar(activity!!)

        personal_click_language.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.LANGUAGE_CHANGE_ACTIVITY).withInt("type", 1).navigation()
        }
        //使用帮助
        personal_click_help.setOnClickListener { a ->
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.WEB_VIEW_ACTIVITY).withString("url", "https://www.boscore.io/index.html").withString("title", resources.getString(R.string.personal_txt_help)).navigation()
        }
        personal_click_cloud.setOnClickListener {
            dialog?.show()
            StorageFactory.createOneDrive(activity!!).login(activity!!).rxJava().observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        dialog?.dismiss()
                        if (it.isLogin) {
                            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.CLOUD_MANAGEMENT_ACTIVITY).navigation()
                        } else {
                            Toast.makeText(this@PersonalCenterFragment.context, "登录失败", Toast.LENGTH_SHORT).show()
                        }
                    }, {
                        dialog?.dismiss()
                        Toast.makeText(this@PersonalCenterFragment.context, "登录失败", Toast.LENGTH_SHORT).show()
                    })
        }
        //打开红包
        personal_click_red.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RECEIVE_RED_ENVELOPE_ACTIVITY).navigation()
        }

        personal_click_pwd.setOnClickListener {
            //0是修改密码   1是找回密码
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.PWD_UPDATE_ACTIVITY)
                    .withInt("type", 0)
                    .navigation()
        }
        personal_click_about.setOnClickListener {

            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.WEB_VIEW_ACTIVITY)
                    .withString("url", "https://www.boscore.io/index.html")
                    .withString("title", resources.getString(R.string.personal_txt_about)).navigation()
        }

        personal_click_forget.setOnClickListener {
            //0是修改密码   1是找回密码
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.PWD_UPDATE_ACTIVITY)
                    .withInt("type", 1)
                    .navigation()
        }
    }

    //去掉标题栏
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

    fun setWindowStatusBarColor(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var window = activity.window;
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.statusBarColor = activity.resources.getColor(R.color.colorPrimaryDark);
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

    }


}
