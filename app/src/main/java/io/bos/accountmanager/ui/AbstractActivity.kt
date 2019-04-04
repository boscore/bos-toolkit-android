package io.bos.accountmanager.ui

import android.annotation.TargetApi
import android.app.Activity
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.android.tu.loadingdialog.LoadingDailog
import io.bos.accountmanager.BOSApplication
import io.bos.accountmanager.Constants
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.di.component.DaggerActivityComponent
import io.bos.accountmanager.presenter.AbstractPresenter
import io.bos.accountmanager.view.AbstractView
import java.util.*
import javax.inject.Inject

abstract class AbstractActivity<V : AbstractView, P : AbstractPresenter<V>> : AbstractBosActivity(), AbstractView {
    @Inject
    lateinit var presenter: P
    override fun owner(): LifecycleOwner {
        return this
    }

    override fun hideKeyboard() {
        hideKeyboard(null)
    }

    override fun hideKeyboard(windowToken: IBinder?) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken ?: window.decorView.windowToken, 0)
    }

    abstract fun initInjects(component: ActivityComponent)
    abstract fun attachView(): V
    override fun context(): Context? {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initInjects(getActivityComponent())
        presenter.attachView(attachView())
        initDialog()

    }
   fun initDialog(){



   }
    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    fun toast(msg:String):LoadingDailog.Builder{
        var loadBuilder = LoadingDailog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setShowMessage(false)
                .setCancelOutside(false)
        return loadBuilder
    }




    fun light(boolean: Boolean) {
        if (boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }

    protected fun setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
//            window.navigationBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = this.window
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
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


}