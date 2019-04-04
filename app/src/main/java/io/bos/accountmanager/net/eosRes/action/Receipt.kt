package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class Receipt(
        @Expose
        val receiver: String,
        @Expose
        val act_digest: String,
        @Expose
        val global_sequence: Int,
        @Expose
        val recv_sequence: Int,
        @Expose
        val auth_sequence: List<List<String>>,
        @Expose
        val code_sequence: Int,
        @Expose
        val abi_sequence: Int
)