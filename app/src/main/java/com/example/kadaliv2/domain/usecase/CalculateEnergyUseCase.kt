package com.example.kadaliv2.domain.usecase

import com.example.kadaliv2.domain.model.Device

class CalculateEnergyUseCase {

    fun calculateDailyEnergy(device: Device): Double {
        // Energy (kWh) = (Power (Watt) × Usage Hours × Quantity) / 1000
        return (device.powerWatt * device.usageHoursPerDay * device.quantity) / 1000.0
    }

    fun calculateDailyCost(dailyEnergyKwh: Double, tariffPricePerKwh: Double): Double {
        // Cost = Energy (kWh) × Electricity Tariff (Rp/kWh)
        return dailyEnergyKwh * tariffPricePerKwh
    }

    fun calculateWeeklyCost(dailyCost: Double): Double {
        return dailyCost * 7
    }

    fun calculateMonthlyCost(dailyCost: Double): Double {
        return dailyCost * 30
    }

    fun calculateYearlyCost(dailyCost: Double): Double {
        return dailyCost * 365
    }
}
