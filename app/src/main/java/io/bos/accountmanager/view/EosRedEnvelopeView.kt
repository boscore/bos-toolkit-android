package io.bos.accountmanager.view

import io.bos.accountmanager.data.local.db.table.AccountTable
import io.bos.accountmanager.net.bean.SecretKeyBean

/**
 *
 * Created by Administrator on 2018/12/26/026.
 */
interface EosRedEnvelopeView:AbstractView {
    /**
     * 获取全部账号信息成功以及失败
     */
    fun getAllAccountSuccess(list:ArrayList<AccountTable>){}
    fun getAllAccountFail(){}
    /**
     * 获取私钥list成功以及失败
     */
    fun getPrivateKey(date: ArrayList<SecretKeyBean>) {}
    fun getPrivateKeyFail(msg:String){}
    /**
     * 钱包创建成功以及失败
     */
    fun createRedEnvelopeSuccess(str:String){}
    fun createRedEnvelopeFail(msg:String){}
    /**
     * 领取红包成功以及失败
     */
    fun getRedEnvelopeSuccess(quantity:String,memo:String,time:String){}
    fun getRedEnvelopeFail(){}


}