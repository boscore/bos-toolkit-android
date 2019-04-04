package io.bos.accountmanager.view

interface PwdUpdateView :AbstractView{
    fun startUpdate()
    fun updateError(error: String)
    fun updateSuccess()

    fun seetPwd(date:String){}
    fun errSet(err:String){}

}