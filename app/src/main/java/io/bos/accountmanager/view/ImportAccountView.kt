package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable

interface ImportAccountView : AbstractView {

    /**
     * 获取账号名错误
     */
    fun onAccountError()

    /**
     * 通过公钥获取账号名,并且回去所有的公钥已经权限
     */
    fun onAccount(account: ArrayList<String>)

    /**
     * 导入成功
     */
    fun importSuccess()

    /**
     * 导入失败
     */
    fun importError()

    /**
     * 加载账号成功
     */
    fun localAccount(accountTables: ArrayList<Pair<String, AccountTable?>>,date:ArrayList<String>)

    /**
     * 加载账号失败
     */
    fun localAccountError(message: String)

}