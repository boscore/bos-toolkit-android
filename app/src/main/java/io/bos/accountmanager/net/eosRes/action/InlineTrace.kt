package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class InlineTrace(
        @Expose
        val receipt: Receipt,
        @Expose
        val act: Act,
        @Expose
        val elapsed: Int,
        @Expose
        val cpu_usage: Int,
        @Expose
        val console: String,
        @Expose
        val total_cpu_usage: Int,
        @Expose
        val trx_id: String,
        @Expose
        val inline_traces: List<InlineTrace>
)