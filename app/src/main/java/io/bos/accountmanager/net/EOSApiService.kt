package io.bos.accountmanager.net

import com.google.gson.JsonObject
import com.hconline.base.eos.eosRes.EOSAccountRes
import io.bos.accountmanager.net.eosReq.BosTableRedEnvelopeRowReq
import io.bos.accountmanager.net.eosReq.EOSGetAccountReq
import io.reactivex.Flowable
import retrofit2.http.Body
import retrofit2.http.POST

interface EOSApiService {

    @POST("v1/chain/get_account")
    fun getAccountJson(@Body content: EOSGetAccountReq): Flowable<EOSAccountRes>
    @POST("v1/chain/get_table_rows")
    fun getRedEnvelopeRecoed(@Body content: BosTableRedEnvelopeRowReq): Flowable<JsonObject>

}