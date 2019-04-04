package io.bos.accountmanager

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.di.component.AppComponent
import io.bos.accountmanager.di.component.DaggerAppComponent
import io.bos.accountmanager.di.module.AppModule
import io.bos.accountmanager.utils.LocalManageUtil

class BOSApplication : Application() {
    private var mAppComponent: AppComponent? = null

    companion object {
        fun get(context: Context): BOSApplication {
            return context.applicationContext as BOSApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        mAppComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()

        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
        LocalManageUtil.setApplicationLanguage(this)

    }

    fun getAppComponent(): AppComponent {
        return mAppComponent!!
    }


    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            LocalManageUtil.saveSystemCurrentLanguage(base)
        }
        super.attachBaseContext(LocalManageUtil.setLocal(base!!))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //保存系统选择语言
        LocalManageUtil.onConfigurationChanged(applicationContext)
    }


}