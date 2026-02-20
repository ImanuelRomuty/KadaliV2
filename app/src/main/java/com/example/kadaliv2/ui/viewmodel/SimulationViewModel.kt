package com.example.kadaliv2.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.kadaliv2.domain.model.Device
import com.example.kadaliv2.domain.usecase.CalculateEnergyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SimulationViewModel(
    private val calculateEnergyUseCase: CalculateEnergyUseCase
) : ViewModel() {

    private val _simulationResult = MutableStateFlow<SimulationResult?>(null)
    val simulationResult: StateFlow<SimulationResult?> = _simulationResult.asStateFlow()

    fun calculate(power: Double, hours: Double, pricePerKwh: Double) {
        // Create a dummy device for calculation
        val device = Device(roomId = "", name = "Sim", powerWatt = power, usageHoursPerDay = hours, quantity = 1)
        
        val dailyEnergy = calculateEnergyUseCase.calculateDailyEnergy(device)
        val dailyCost = calculateEnergyUseCase.calculateDailyCost(dailyEnergy, pricePerKwh)
        val monthlyCost = calculateEnergyUseCase.calculateMonthlyCost(dailyCost)
        val yearlyCost = calculateEnergyUseCase.calculateYearlyCost(dailyCost)
        
        _simulationResult.value = SimulationResult(dailyCost, monthlyCost, yearlyCost)
    }
    
    data class SimulationResult(
        val daily: Double,
        val monthly: Double,
        val yearly: Double
    )
}
