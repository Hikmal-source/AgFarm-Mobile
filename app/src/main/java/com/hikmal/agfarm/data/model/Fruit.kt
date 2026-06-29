package com.hikmal.agfarm.data.model

data class Fruit(
    val id: String = "",
    val userId: String = "", // Kunci pemisah untuk jenis buah/tanaman
    val name: String = "",
    val type: String = ""
)