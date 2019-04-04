package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.net.bean.PermissionsBean
import io.bos.accountmanager.net.bean.SecretKeyBean
import io.bos.accountmanager.presenter.CloudStoragePresenter
import io.bos.accountmanager.presenter.WalletManagePresenter
import io.bos.accountmanager.ui.dialog.PwdDialogCloud
import java.util.ArrayList

interface WalletManageView : AbstractView {
    fun onAccounts(accounts: AccountTable) {}

    fun errMessage(message: String) {}
    fun getDecrypt(date: ArrayList<SecretKeyBean>) {}


    fun getAuthorityList(listBaifen: ArrayList<PermissionsBean>) {}
    fun getErrAuthority(message: String) {}
    /**
     * 删除钱包成功
     */
    fun onDeleteWalletSuccess() {}

    /**
     * 删除钱包失败
     */
    fun onDeleteWalletFail() {}


    fun getPrivateKey(date: ArrayList<SecretKeyBean>) {}

    //获取当前账号的所有公私钥
    fun getCurrentAccountKey(date: ArrayList<SecretKeyBean>,ped:String){}
    //错误提示
    fun errAccount(message:String){}
    //修改权限成功
    fun updateSuccess(){}

    //获取到所有列表
   fun onCloudAccounts( cloudAccounts : ArrayList<WalletManagePresenter.EnDataResults>){}
    //
    fun showPwd(listener: PwdDialogCloud.PwdCallback) {}

    fun cloceDialog(){}
}