package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import java.util.ArrayList

interface AccountListView : AbstractView {

    fun deleteSuccess(message:String){}

    fun  getHistoryList(history:List<EstablishAccountTable>){}

    fun errLose(message:String){}

    fun errAccount(message:String){}

    fun Accountumber(){}

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
    fun localAccount(accountTables: ArrayList<Pair<String, AccountTable?>>, date: ArrayList<String>)

    /**
     * 加载账号失败
     */
    fun localAccountError(message: String)
}