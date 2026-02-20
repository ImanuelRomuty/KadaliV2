package com.example.kadaliv2.domain.model

data class Device(
    val id: String = "",
    val roomId: String,
    val name: String,
    val powerWatt: Double,
    val usageHoursPerDay: Double,
    val quantity: Int
)
