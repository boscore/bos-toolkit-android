package io.bos.accountmanager.presenter

import android.app.Activity
import android.util.Log
import io.bos.accountmanager.core.storage.OneDrive
import io.bos.accountmanager.core.storage.core.response.LoginResponse
import io.bos.accountmanager.view.OneDriveView
import io.reactivex.Flowable
import javax.inject.Inject

class OneDrivePresenter @Inject constructor() : AbstractPresenter<OneDriveView>() {

    fun login() {
        addDisposable(OneDrive.get(getView()?.context()!! as Activity).isLogin().rxJava()
                .flatMap {
                    if (it.result) {
                        return@flatMap Flowable.just(LoginResponse(true, ""))
                    } else {
                        return@flatMap OneDrive.get(getView()?.context()!! as Activity).login(getView()?.context()!! as Activity).rxJava()
                    }
                }
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({
                    if (it.isLogin) {
                        getView()?.loginSuccess()
                    } else {
                        getView()?.loginError()
                    }
                }, {
                    it.printStackTrace()
                    getView()?.loginError()
                }))
    }

    fun logout() {
        addDisposable(OneDrive.get(getView()?.context()!! as Activity).logout().rxJava()
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({
                    if (it.isLogout) {
                        getView()?.logoutSuccess()
                    } else {
                        getView()?.logoutError()
                    }
                }, {
                    getView()?.logoutError()
                }))
    }

    fun exist(folder: String) {
        addDisposable(OneDrive.get(getView() as Activity).exist(folder).rxJava()
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({
                    Log.e("exist", "true")
                }, {
                    Log.e("exist", "false")
                }))
    }

    fun upload(data: String, fileName: String) {
        addDisposable(OneDrive.get(getView() as Activity).upload(data, fileName).rxJava()
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({
                    Log.e("upload", "true")
                }, {
                    Log.e("upload", "false")
                }))
    }

    fun download(fileName: String) {
        addDisposable(OneDrive.get(getView() as Activity).download(fileName).rxJava()
                .subscribeOn(ioSchedulers)
                .observeOn(androidSchedulers)
                .subscribe({
                    Log.e("download", "true")
                }, {
                    Log.e("download", "false")
                }))
    }

}