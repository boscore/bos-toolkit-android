package io.bos.accountmanager.ui

import android.content.pm.ActivityInfo
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.di.component.DaggerActivityComponent
import io.bos.accountmanager.utils.LocalManageUtil

abstract class AbstractBosActivity : AppCompatActivity() {
    protected var mActivityComponent: ActivityComponent? = null

    /**
     * 布局ID
     */
    @LayoutRes
    abstract fun byId(): Int

    /**
     * 绑定事件
     */
    open fun listener() {

    }

    /**
     * 处理数据
     */
    open fun data() {

    }

    /**
     * 初始化数据绑定
     */
    open fun init() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(byId())
        init()
        listener()
        data()
    }

    fun getActivityComponent(): ActivityComponent {
        if (null == mActivityComponent) {
            mActivityComponent = DaggerActivityComponent.builder()
                    .appComponent(BOSApplication.get(this).getAppComponent())
                    .build()
        }
        return mActivityComponent!!
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase!!))
    }
}