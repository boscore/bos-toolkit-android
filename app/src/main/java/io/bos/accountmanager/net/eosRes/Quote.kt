package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose


data class Quote(
        @Expose
    val balance: String,
        @Expose
    val weight: String
)