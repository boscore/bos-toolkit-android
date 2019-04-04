package io.bos.accountmanager.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.bos.accountmanager.Constants
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.EosRedEnvelopePresenter
import io.bos.accountmanager.view.EosRedEnvelopeView
import kotlinx.android.synthetic.main.activity_red_add_success.*

/**
 * 创建红包成功，复制
 */

@Route(path = Constants.RoutePath.ACTIVITY.RED_ADD_SUCCESS_ACTIVITY)
class RedAddSuccessActivity : AbstractActivity<EosRedEnvelopeView, EosRedEnvelopePresenter>(), EosRedEnvelopeView {
    override fun byId(): Int {
        return R.layout.activity_red_add_success
    }

    override fun initInjects(component: ActivityComponent) {
        component?.inject(this)
    }

    override fun attachView(): EosRedEnvelopeView = this


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparencyBar(this)
        ARouter.getInstance().inject(this)
        red_add_success_display.text = intent.getStringExtra("redEnvelopeText")
        red_add_success_txt_copy.setOnClickListener {
            copy(red_add_success_display.text.toString())
            Toast.makeText(context(), getString(R.string.copy_success), Toast.LENGTH_SHORT).show()

        }
        my_red_envelope.setOnClickListener {
            ARouter.getInstance().build(Constants.RoutePath.ACTIVITY.RED_RECORD_ACTIVITY).withString("accountName", intent.getStringExtra("accountName")).navigation()
        }
        back.setOnClickListener { finish() }
    }


    private fun copy(str: String) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, str)
        clipboard.primaryClip = clipData
    }


}
