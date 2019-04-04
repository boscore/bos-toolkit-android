package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class Data(
        @Expose
        val from: String?,

        @Expose
        val to: String?,

        @Expose
        val quantity: String?,

        @Expose
        val memo: String?,
        @Expose
        val receiver:String?,
        @Expose
        val stake_net_quantity:String?,
        @Expose
        val stake_cpu_quantity:String?,
        @Expose
        val transfer:Int = 0,
        @Expose
        val voter:String?,
        @Expose
        val proxy:String?,
        @Expose
        val producers:List<String>?
)