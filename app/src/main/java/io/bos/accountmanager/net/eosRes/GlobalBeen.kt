package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose


data class GlobalBeen(
        @Expose
    val max_block_net_usage: Int,
        @Expose
    val target_block_net_usage_pct: Int,
        @Expose
    val max_transaction_net_usage: Int,
        @Expose
    val base_per_transaction_net_usage: Int,
        @Expose
    val net_usage_leeway: Int,
        @Expose
    val context_free_discount_net_usage_num: Int,
        @Expose
    val context_free_discount_net_usage_den: Int,
        @Expose
    val max_block_cpu_usage: Int,
        @Expose
    val target_block_cpu_usage_pct: Int,
        @Expose
    val max_transaction_cpu_usage: Int,
        @Expose
    val min_transaction_cpu_usage: Int,
        @Expose
    val max_transaction_lifetime: Int,
        @Expose
    val deferred_trx_expiration_window: Int,
        @Expose
    val max_transaction_delay: Int,
        @Expose
    val max_inline_action_size: Int,
        @Expose
    val max_inline_action_depth: Int,
        @Expose
    val max_authority_depth: Int,
        @Expose
    val max_ram_size: String,
        @Expose
    val total_ram_bytes_reserved: String,
        @Expose
    val total_ram_stake: String,
        @Expose
    val last_producer_schedule_update: String,
        @Expose
    val last_pervote_bucket_fill: String,
        @Expose
    val pervote_bucket: Int,
        @Expose
    val perblock_bucket: Int,
        @Expose
    val total_unpaid_blocks: Int,
        @Expose
    val total_activated_stake: String,
        @Expose
    val thresh_activated_stake_time: String,
        @Expose
    val last_producer_schedule_size: Int,
        @Expose
    val total_producer_vote_weight: String,
        @Expose
    val last_name_close: String
)