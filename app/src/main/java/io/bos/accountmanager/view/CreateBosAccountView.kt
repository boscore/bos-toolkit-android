package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.net.bean.SecretKeyBean
import java.util.ArrayList

interface CreateBosAccountView : AbstractView {
    //获取所有的账户信息
    fun onAccountsName(accounts: List<AccountTable>)

    fun errMessage(message:String){}
    //获取当前账号的所有公私钥
    fun getCurrentAccountKey(date: ArrayList<SecretKeyBean>, ped:String){}

    //创建账号成功
    fun establishSuccess(){}


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