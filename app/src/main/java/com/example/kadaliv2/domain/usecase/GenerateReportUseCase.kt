package com.example.kadaliv2.domain.usecase

import com.example.kadaliv2.domain.model.DeviceReportItem
import com.example.kadaliv2.domain.model.ReportData
import com.example.kadaliv2.domain.model.RoomReportItem
import kotlinx.coroutines.flow.first

class GenerateReportUseCase(
    private val getRoomsUseCase: GetRoomsUseCase,
    private val getAllDevicesUseCase: GetAllDevicesUseCase,
    private val getTariffUseCase: GetTariffUseCase,
    private val calculateEnergyUseCase: CalculateEnergyUseCase
) {

    suspend operator fun invoke(): ReportData {
        val rooms = getRoomsUseCase().first()
        val devices = getAllDevicesUseCase().first()
        val tariff = getTariffUseCase().first()
        val tariffPrice = tariff?.pricePerKwh ?: 0.0

        val totalEnergy = devices.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
        val totalDailyCost = calculateEnergyUseCase.calculateDailyCost(totalEnergy, tariffPrice)
        val weeklyCost = calculateEnergyUseCase.calculateWeeklyCost(totalDailyCost)
        val monthlyCost = calculateEnergyUseCase.calculateMonthlyCost(totalDailyCost)
        val yearlyCost = calculateEnergyUseCase.calculateYearlyCost(totalDailyCost)
        val avgDailyCost = totalDailyCost // Simpler representation for "Average Daily Cost" section

        val roomItems = rooms.map { room ->
            val roomDevices = devices.filter { it.roomId == room.id }
            val roomEnergy = roomDevices.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
            val roomDailyCost = calculateEnergyUseCase.calculateDailyCost(roomEnergy, tariffPrice)
            val roomMonthlyCost = calculateEnergyUseCase.calculateMonthlyCost(roomDailyCost)
            
            val deviceItems = roomDevices.map { device ->
                val dailyEnergy = calculateEnergyUseCase.calculateDailyEnergy(device)
                val dailyCost = calculateEnergyUseCase.calculateDailyCost(dailyEnergy, tariffPrice)
                val monthlyEnergy = dailyEnergy * 30
                val yearlyEnergy = dailyEnergy * 365
                val connectedLoad = device.powerWatt * device.quantity

                DeviceReportItem(
                    deviceName = device.name,
                    power = device.powerWatt,
                    quantity = device.quantity,
                    hours = device.usageHoursPerDay,
                    dailyEnergy = dailyEnergy,
                    dailyCost = dailyCost,
                    monthlyEnergy = monthlyEnergy,
                    yearlyEnergy = yearlyEnergy,
                    connectedLoad = connectedLoad
                )
            }

            val percentage = if (totalEnergy > 0) (roomEnergy / totalEnergy) * 100 else 0.0

            RoomReportItem(
                roomName = room.name,
                roomDescription = room.description ?: "",
                deviceCount = roomDevices.size,
                energy = roomEnergy,
                cost = roomMonthlyCost,
                percentage = percentage,
                devices = deviceItems
            )
        }

        val mostConsumingRoom: String = roomItems.maxByOrNull { it.energy }?.roomName ?: "N/A"

        return ReportData(
            generatedDate = System.currentTimeMillis(),
            officeName = "Kadali Analysis Center",
            totalRooms = rooms.size,
            totalDevices = devices.size,
            totalEnergy = totalEnergy,
            totalDailyCost = totalDailyCost,
            totalCostMonthly = monthlyCost,
            weeklyCost = weeklyCost,
            yearlyCost = yearlyCost,
            avgDailyCost = avgDailyCost,
            tariffStart = tariffPrice,
            mostConsumingRoom = mostConsumingRoom,
            roomBreakdown = roomItems
        )
    }
}
