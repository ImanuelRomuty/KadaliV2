package com.example.kadaliv2.domain.model

data class Device(
    val id: Long = 0,
    val roomId: Long,
    val name: String,
    val powerWatt: Double,
    val usageHoursPerDay: Double,
    val quantity: Int
)
