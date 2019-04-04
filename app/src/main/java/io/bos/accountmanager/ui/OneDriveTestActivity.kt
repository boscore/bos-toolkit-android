package io.bos.accountmanager.ui

import android.os.Bundle
import android.widget.Toast
import io.bos.accountmanager.R
import io.bos.accountmanager.di.component.ActivityComponent
import io.bos.accountmanager.presenter.OneDrivePresenter
import io.bos.accountmanager.view.OneDriveView
import kotlinx.android.synthetic.main.activity_one_drive_test.*

class OneDriveTestActivity : AbstractActivity<OneDriveView, OneDrivePresenter>(), OneDriveView {
    override fun initInjects(component: ActivityComponent) {
        component.inject(this)
    }

    override fun attachView(): OneDriveView {
        return this
    }

    override fun byId(): Int {
        return R.layout.activity_one_drive_test
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnLogin.setOnClickListener {
            presenter.login()
        }
        btnLogout.setOnClickListener {
            presenter.logout()
        }
        btnGetUserInfo.setOnClickListener {
        }
        btnExist.setOnClickListener {
            presenter.exist("test.txt")
        }
        btnUpload.setOnClickListener {
            presenter.upload("123", "test.txt")
        }
        btnDownload.setOnClickListener {
            presenter.download("test.txt")
        }
    }

    override fun loginError() {
        Toast.makeText(this, "login error", Toast.LENGTH_SHORT).show()
    }

    override fun loginSuccess() {
        Toast.makeText(this, "login success", Toast.LENGTH_SHORT).show()
    }

    override fun logoutError() {
        Toast.makeText(this, "logout error", Toast.LENGTH_SHORT).show()
    }

    override fun logoutSuccess() {
        Toast.makeText(this, "logout success", Toast.LENGTH_SHORT).show()
    }

}
