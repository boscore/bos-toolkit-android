package io.bos.accountmanager.net.eosReq

import com.google.gson.annotations.Expose

/**
 *
 *  by Administrator on 2019/1/7/007.
 */
class BosTableRedEnvelopeRowReq (
    @Expose
    val scope: String,
    @Expose
    val code: String,
    @Expose
    val table: String,
    @Expose
    val json: Boolean,
//    @Expose
//    val limit:Int,
    @Expose
    val lower_bound:String,
    @Expose
    val key_type:String,
    @Expose
    val index_position:String

    )