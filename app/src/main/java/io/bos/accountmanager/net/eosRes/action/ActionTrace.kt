package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class ActionTrace(
        @Expose
        val receipt: Receipt,
        @Expose
        val act: Act,
        @Expose
        val elapsed: Long,
        @Expose
        val cpu_usage: Long,
        @Expose
        val console: String,
        @Expose
        val total_cpu_usage: Long,
        @Expose
        val trx_id: String,
        @Expose
        val inline_traces: List<InlineTrace>
)