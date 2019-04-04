package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class Act(
        @Expose
        val account: String,
        @Expose
        val name: String,
        @Expose
        val authorization: List<Authorization>,
        @Expose
        val data: Data,
        @Expose
        val hex_data: String
)