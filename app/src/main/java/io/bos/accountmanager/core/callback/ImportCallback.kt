package io.bos.accountmanager.core.callback

import io.reactivex.disposables.Disposable

interface ImportCallback {

    fun privateKey(data: String)
    fun oneDrive(data: ArrayList<String>)
    fun addDisposable(disposable: Disposable)

}