package com.example.kadaliv2.domain.model

data class ReportData(
    val generatedDate: Long,
    val totalEnergy: Double,
    val totalCostMonthly: Double,
    val tariffStart: Double,
    val mostConsumingRoom: String,
    val roomBreakdown: List<RoomReportItem>,
    val deviceBreakdown: List<DeviceReportItem>
)

data class RoomReportItem(
    val roomName: String,
    val energy: Double,
    val cost: Double,
    val percentage: Double
)

data class DeviceReportItem(
    val deviceName: String,
    val roomName: String,
    val power: Double,
    val hours: Double,
    val quantity: Int,
    val totalEnergy: Double,
    val cost: Double
)
