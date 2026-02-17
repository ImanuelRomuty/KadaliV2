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
        val totalMonthlyCost = calculateEnergyUseCase.calculateMonthlyCost(totalDailyCost)

        val deviceItems = devices.map { device ->
            val dailyEnergy = calculateEnergyUseCase.calculateDailyEnergy(device)
            val dailyCost = calculateEnergyUseCase.calculateDailyCost(dailyEnergy, tariffPrice)
            val monthlyCost = calculateEnergyUseCase.calculateMonthlyCost(dailyCost)
            val roomName = rooms.find { it.id == device.roomId }?.name ?: "Unknown"

            DeviceReportItem(
                deviceName = device.name,
                roomName = roomName,
                power = device.powerWatt,
                hours = device.usageHoursPerDay,
                quantity = device.quantity,
                totalEnergy = dailyEnergy,
                cost = monthlyCost // Using monthly cost for report consistency as requested? Or Daily? spec says "Estimated Cost Contribution". Let's use Monthly to match summary.
            )
        }

        val roomItems = rooms.map { room ->
            val roomDevices = devices.filter { it.roomId == room.id }
            val roomEnergy = roomDevices.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
            val roomDailyCost = calculateEnergyUseCase.calculateDailyCost(roomEnergy, tariffPrice)
            val roomMonthlyCost = calculateEnergyUseCase.calculateMonthlyCost(roomDailyCost)
            
            val percentage = if (totalEnergy > 0) (roomEnergy / totalEnergy) * 100 else 0.0

            RoomReportItem(
                roomName = room.name,
                energy = roomEnergy,
                cost = roomMonthlyCost,
                percentage = percentage
            )
        }

        val mostConsumingRoom = roomItems.maxByOrNull { it.energy }?.roomName ?: "N/A"

        return ReportData(
            generatedDate = System.currentTimeMillis(),
            totalEnergy = totalEnergy,
            totalCostMonthly = totalMonthlyCost,
            tariffStart = tariffPrice,
            mostConsumingRoom = mostConsumingRoom,
            roomBreakdown = roomItems,
            deviceBreakdown = deviceItems
        )
    }
}
