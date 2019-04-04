package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose

/**
 * Created by haichecker on 18-6-19.
 */

data class EOSGetBlockRes(
        @Expose
        val timestamp: String,
        @Expose
        val producer: String,
        @Expose
        val confirmed: Int,
        @Expose
        val previous: String,
        @Expose
        val transaction_mroot: String,
        @Expose
        val action_mroot: String,
        @Expose
        val schedule_version: Int,
        @Expose
        val new_producers: Any,
        @Expose
        val header_extensions: List<Any>,
        @Expose
        val producer_signature: String,
        @Expose
        val transactions: List<TransactionRoot>,
        @Expose
        val block_extensions: List<Any>,
        @Expose
        val id: String,
        @Expose
        val block_num: Int,
        @Expose
        val ref_block_prefix: Long
) : EOSAbstractRes()

data class TransactionRoot(
        @Expose
        val status: String,
        @Expose
        val cpu_usage_us: Int,
        @Expose
        val net_usage_words: Int,
        @Expose
        val trx: Trx
)

data class Trx(
        @Expose
        val id: String,
        @Expose
        val signatures: List<String>,
        @Expose
        val compression: String,
        @Expose
        val packed_context_free_data: String,
        @Expose
        val context_free_data: List<Any>,
        @Expose
        val packed_trx: String,
        @Expose
        val transaction: Transaction
)

data class Transaction(
        @Expose
        val expiration: String,
        @Expose
        val ref_block_num: Int,
        @Expose
        val ref_block_prefix: Int,
        @Expose
        val max_net_usage_words: Int,
        @Expose
        val max_cpu_usage_ms: Int,
        @Expose
        val delay_sec: Int,
        @Expose
        val context_free_actions: List<Any>,
        @Expose
        val actions: List<Action>,
        @Expose
        val transaction_extensions: List<Any>
)

data class Action(
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

data class Data(
        @Expose
        val memo: String
)

data class Authorization(
        @Expose
        val actor: String,
        @Expose
        val permission: String
)