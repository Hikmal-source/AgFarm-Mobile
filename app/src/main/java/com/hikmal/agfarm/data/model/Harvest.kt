package com.hikmal.agfarm.data.model

data class Harvest(
    val id: String = "",
    val userId: String = "", // Kunci pemilik catatan panen
    val landId: String = "",
    val fruitName: String = "",
    val weightInKg: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis()
)