package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose


open class EOSAbstractRes {
    @Expose
    val code: Int = 0
    @Expose
    val message: String = ""
    @Expose
    val error: Error? = null

    fun hasError(): Boolean {
        return error != null
    }
}