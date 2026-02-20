package com.example.kadaliv2.domain.model

data class ReportData(
    val generatedDate: Long,
    val officeName: String,
    val currency: String = "Rp",
    val totalRooms: Int,
    val totalDevices: Int,
    val totalEnergy: Double,
    val totalDailyCost: Double,
    val totalCostMonthly: Double,
    val weeklyCost: Double,
    val yearlyCost: Double,
    val avgDailyCost: Double,
    val tariffStart: Double,
    val mostConsumingRoom: String,
    val roomBreakdown: List<RoomReportItem>,
    val recommendations: String = "Standard optimization recommended."
)

data class RoomReportItem(
    val roomName: String,
    val roomDescription: String,
    val deviceCount: Int,
    val energy: Double,
    val cost: Double,
    val percentage: Double,
    val devices: List<DeviceReportItem>
)

data class DeviceReportItem(
    val deviceName: String,
    val power: Double,
    val quantity: Int,
    val hours: Double,
    val dailyEnergy: Double,
    val dailyCost: Double,
    val monthlyEnergy: Double,
    val yearlyEnergy: Double,
    val connectedLoad: Double
)
