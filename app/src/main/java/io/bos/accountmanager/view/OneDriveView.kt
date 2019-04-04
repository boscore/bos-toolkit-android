package io.bos.accountmanager.view

interface OneDriveView : AbstractView {

    fun loginSuccess() {}

    fun loginError() {}

    fun logoutSuccess() {}

    fun logoutError() {}

}