package io.bos.accountmanager.ui

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.View
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.androidkun.xtablayout.XTabLayout
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.MainPresenter
import io.bos.accountmanager.ui.fragment.RedReceiveRecordFragment
import io.bos.accountmanager.ui.fragment.RedSendOutRecordFragment
import io.bos.accountmanager.view.MainView
import kotlinx.android.synthetic.main.activity_red_envelope_record.*

/**
 * 红包记录
 */
@Route(path = Constants.RoutePath.ACTIVITY.RED_ENVELOPE_RECORD_ACTIVITY)
class RedEnvelopeRecordActivity : AbstractActivity<MainView, MainPresenter>(), MainView {
    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): MainView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_red_envelope_record
    }

    private var currentTabIndex = 0
    private var fragments: MutableList<AbstractBosFragment>? = null
    private lateinit var redReceiveRecordFragment: RedReceiveRecordFragment
    private lateinit var sendOutRedFragment: RedSendOutRecordFragment


    override fun init() {
        super.init()

        fragments = ArrayList()
        redReceiveRecordFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.RED_RECEIVE_RECORD_FRAGMENT).navigation() as RedReceiveRecordFragment
        sendOutRedFragment = ARouter.getInstance().build(Constants.RoutePath.FRAGMENT.RED_SEND_OUT_RECORD_FRAGMENT).navigation() as RedSendOutRecordFragment
        fragments!!.clear()
        (fragments as ArrayList<AbstractBosFragment>).add(redReceiveRecordFragment!!)
        (fragments as ArrayList<AbstractBosFragment>).add(sendOutRedFragment!!)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparencyBar(this@RedEnvelopeRecordActivity)

        changStatusIconCollor(false)
        red_envelope_record_xTablayout?.addTab(red_envelope_record_xTablayout?.newTab()!!.setText(getString(R.string.red_encelpe_record_txt_receive)))
        red_envelope_record_xTablayout?.addTab(red_envelope_record_xTablayout?.newTab()!!.setText(getString(R.string.red_encelpe_record_txt_send)))

        red_envelope_record_xTablayout.setOnTabSelectedListener(object : XTabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: XTabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: XTabLayout.Tab?) {

            }

            override fun onTabSelected(tab: XTabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {

                        switchFragment(0)
                    }

                    1 -> {

                        switchFragment(1)
                    }

                }
            }

        })
        back.setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.red_envelope_record_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitAllowingStateLoss()
        } else {
            currentTabIndex = savedInstanceState.getInt("currentTabIndex")
            val trx = supportFragmentManager.beginTransaction()
            if (!fragments!![currentTabIndex].isAdded) {
                trx.replace(R.id.red_envelope_record_contentPanel, fragments!![currentTabIndex])
            }
            trx.show(fragments!![currentTabIndex]).commitAllowingStateLoss()
        }
    }


    private fun switchFragment(index: Int) {
        if (currentTabIndex != index) {
            val trx = supportFragmentManager.beginTransaction()
            trx.hide(fragments!![currentTabIndex])
            if (!fragments!![index].isAdded) {
                trx.add(R.id.red_envelope_record_contentPanel, fragments!![index])
            }
            trx.show(fragments!![index]).commitAllowingStateLoss()


        }
        currentTabIndex = index


    }


    //设置标题栏颜色
    fun changStatusIconCollor(setDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window?.decorView
            if (decorView != null) {
                var vis = decorView!!.systemUiVisibility
                if (setDark) {
                    vis = vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    vis = vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                decorView!!.systemUiVisibility = vis
            }
        }
    }
}
