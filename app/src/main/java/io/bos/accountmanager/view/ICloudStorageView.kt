package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.presenter.CloudStoragePresenter
import io.bos.accountmanager.ui.dialog.PwdDialogCloud

interface ICloudStorageView : AbstractView {

    fun onLocalAccounts(accounts: List<AccountTable>) {}

    fun onExportPrivateKey(keys: List<CloudStoragePresenter.EnDataResult>) {}

    fun onCloudAccounts(keys: List<CloudStoragePresenter.EnDataResult>) {}

    fun synchronizationSuccess() {}

    fun synchronizationFailed() {}

    /**
     * 导入成功
     */
    fun importSuccess() {}

    /**
     * 导入失败
     */
    fun importError() {}

    fun showPwd(listener: PwdDialogCloud.PwdCallback) {}

}