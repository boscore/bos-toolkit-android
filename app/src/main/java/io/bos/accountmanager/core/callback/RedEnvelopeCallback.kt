package io.bos.accountmanager.core.callback

import io.bos.accountmanager.data.local.db.table.AccountTable

/**
 *
 * 红包创建对应回调
 * Created by Administrator on 2019/1/4/004.
 */
interface RedEnvelopeCallback {
    fun accountRedEnvelope(accountTable: AccountTable, amount:String,redEnvelopeType:Int,count:Int,congratulations:String)
}