package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class Authorization(
        @Expose
        val actor: String,

        @Expose
        val permission: String
)