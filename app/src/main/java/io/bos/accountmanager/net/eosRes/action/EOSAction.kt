package com.hconline.base.eos.eosRes.action

import com.google.gson.annotations.Expose

data class EOSAction(
        @Expose
        val global_action_seq: Int,
        @Expose
        val account_action_seq: Int,
        @Expose
        val block_num: Int,
        @Expose
        val block_time: String,
        @Expose
        val action_trace: ActionTrace
)