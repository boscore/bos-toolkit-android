package com.hconline.base.eos.eosRes



data class RamPrice(
    val code: Int,
    val message: Message,
    val data: List<List<String>>,
    val error: String,
    val total_ram: Double,
    val used_ram: Double
)