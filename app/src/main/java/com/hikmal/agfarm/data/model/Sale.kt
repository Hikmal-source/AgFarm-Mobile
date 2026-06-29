package com.hikmal.agfarm.data.model

data class Sale(
    val id: String = "",
    val userId: String = "", // Kunci pemilik riwayat transaksi penjualan
    val fruitName: String = "",
    val weightSoldInKg: Double = 0.0,
    val totalPrice: Long = 0L,
    val buyerName: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)