package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose


data class Error(
        @Expose
        val code: Int,
        @Expose
        val name: String,
        @Expose
        val what: String,
        @Expose
        val details: List<ErrorDetail>
)


data class ErrorDetail(
        @Expose
        val message: String,
        @Expose
        val file: String,
        @Expose
        val line_number: Int,
        @Expose
        val method: String
)