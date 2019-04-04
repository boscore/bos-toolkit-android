package com.hconline.base.eos.eosRes

import com.google.gson.annotations.Expose
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by haichecker on 18-6-19.
 */

data class EOSGetInfo(
        @Expose
        val server_version: String,
        @Expose
        val chain_id: String,
        @Expose
        val head_block_num: Int,
        @Expose
        val last_irreversible_block_num: Int,
        @Expose
        val last_irreversible_block_id: String,
        @Expose
        val head_block_id: String,
        @Expose
        val head_block_time: String,
        @Expose
        val head_block_producer: String,
        @Expose
        val virtual_block_cpu_limit: Int,
        @Expose
        val virtual_block_net_limit: Int,
        @Expose
        val block_cpu_limit: Int,
        @Expose
        val block_net_limit: Int
) : EOSAbstractRes() {
    fun getTimeAfterHeadBlockTime(diffInMilSec: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            var date = sdf.parse(this.head_block_time)

            val c = Calendar.getInstance()
            c.time = date
            c.add(Calendar.MILLISECOND, diffInMilSec)
            date = c.time

            return sdf.format(date)

        } catch (e: ParseException) {
            e.printStackTrace()
            return this.head_block_time
        }

    }
}