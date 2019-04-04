package io.bos.accountmanager.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.android.tu.loadingdialog.LoadingDailog
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.core.callback.ImportCallback
import io.bos.accountmanager.core.storage.StorageFactory
import io.bos.accountmanager.ui.AbstractBosFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_import_cloudfragment.*

/***
 * 云导入
 */
@Route(path = Constants.RoutePath.FRAGMENT.IMPORT_CLOUD_FRAGMENT)
class ImportCloudfragment : AbstractBosFragment() {

    override fun fragmentLayout(): Int = R.layout.activity_import_cloudfragment

    var dialog: LoadingDailog? = null
    var callback: ImportCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var loadBuilder = LoadingDailog.Builder(context)
                .setMessage("")
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        import_clound_account.setOnClickListener {
            dialog?.show()
            callback?.addDisposable(StorageFactory.createOneDrive(activity!!).login(activity!!).rxJava()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        dialog?.dismiss()
                        //登录成功，进入下一页
                        if (it.isLogin) {
                            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.CLOUD_MANAGEMENT_ACTIVITY).navigation(activity, 200)
                        } else {
                            Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show()
                        }
                    }, {
                        dialog?.dismiss()
                        Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show()
                    }))
        }



        import_clound_help.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.WEB_VIEW_ACTIVITY)
                    .withString("url", "https://www.boscore.io/index.html")
                    .withString("title", resources.getString(R.string.import_txt_help)).navigation()
        }


    }


}
