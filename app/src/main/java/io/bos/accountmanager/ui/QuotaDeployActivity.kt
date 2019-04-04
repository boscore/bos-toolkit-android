package io.bos.accountmanager.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.MainPresenter
import io.bos.accountmanager.view.MainView

/**
 * 限额配置
 */
@Route(path = Constants.RoutePath.ACTIVITY.QUOTA_DEPLOY_ACTIVITY)
class QuotaDeployActivity : AbstractActivity<MainView, MainPresenter>(), MainView {
    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): MainView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_quota_deploy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_quota_deploy)
//    }
}
