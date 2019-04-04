package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose


data class Base(
        @Expose
    val balance: String,
        @Expose
    val weight: String
)