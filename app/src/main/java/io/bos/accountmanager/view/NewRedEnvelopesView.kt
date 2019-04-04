package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.data.local.db.table.EstablishAccountTable
import io.bos.accountmanager.net.bean.PermissionsBean
import io.starteos.jeos.crypto.ec.EosPrivateKey
import java.util.ArrayList

interface NewRedEnvelopesView : AbstractView {

    fun  getlocalSuccess(establishAccountTable: EstablishAccountTable){}

    fun errAccount(message:String){}
    //创建账号成功
    fun  CreateSuccess(  newPriavateKey: EosPrivateKey, id:Long, accountName: String){}

    fun AddTransferAccounts(isAccount:Boolean){}

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