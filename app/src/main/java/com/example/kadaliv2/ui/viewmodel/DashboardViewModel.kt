package com.example.kadaliv2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kadaliv2.domain.usecase.CalculateEnergyUseCase
import com.example.kadaliv2.domain.usecase.GetAllDevicesUseCase
import com.example.kadaliv2.domain.usecase.GetRoomsUseCase
import com.example.kadaliv2.domain.usecase.GetTariffUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class DashboardViewModel(
    private val getRoomsUseCase: GetRoomsUseCase,
    private val getAllDevicesUseCase: GetAllDevicesUseCase,
    private val getTariffUseCase: GetTariffUseCase,
    private val calculateEnergyUseCase: CalculateEnergyUseCase
) : ViewModel() {

    val tariff = getTariffUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rooms = getRoomsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val devices = getAllDevicesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardState = combine(rooms, devices, tariff) { roomList, deviceList, tariffValue ->
        val totalEnergy = deviceList.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
        // tariff is guaranteed non-null (falls back to PLN default in TariffRepositoryImpl)
        val dailyCost = calculateEnergyUseCase.calculateDailyCost(
            totalEnergy,
            tariffValue?.pricePerKwh ?: 1444.70
        )
        val weeklyCost = calculateEnergyUseCase.calculateWeeklyCost(dailyCost)
        val monthlyCost = calculateEnergyUseCase.calculateMonthlyCost(dailyCost)
        val yearlyCost = calculateEnergyUseCase.calculateYearlyCost(dailyCost)
        
        // Group devices by room and calculate per-room energy and daily cost
        val roomConsumption = roomList.associate { room ->
            val roomDevices = deviceList.filter { it.roomId == room.id }
            val roomEnergy = roomDevices.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
            room.name to roomEnergy
        }
        val tariffRate = tariffValue?.pricePerKwh ?: 1444.70
        val roomDailyCost = roomConsumption.mapValues { (_, energy) ->
            calculateEnergyUseCase.calculateDailyCost(energy, tariffRate)
        }
        
        // Chart data: total energy per device type (summed across all rooms)
        val deviceTypeTotals = deviceList
            .groupBy { it.name.trim() }
            .map { (typeName, devices) ->
                typeName to devices.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
            }
            .sortedByDescending { it.second }
        
        DashboardState(
            totalEnergy = totalEnergy,
            dailyCost = dailyCost,
            weeklyCost = weeklyCost,
            monthlyCost = monthlyCost,
            yearlyCost = yearlyCost,
            deviceCount = deviceList.size,
            roomConsumption = roomConsumption,
            roomDailyCost = roomDailyCost,
            deviceTypeTotals = deviceTypeTotals,
            isRoomListEmpty = roomList.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    data class DashboardState(
        val totalEnergy: Double = 0.0,
        val dailyCost: Double = 0.0,
        val weeklyCost: Double = 0.0,
        val monthlyCost: Double = 0.0,
        val yearlyCost: Double = 0.0,
        val deviceCount: Int = 0,
        val roomConsumption: Map<String, Double> = emptyMap(),
        val roomDailyCost: Map<String, Double> = emptyMap(),
        val deviceTypeTotals: List<Pair<String, Double>> = emptyList(),
        val isRoomListEmpty: Boolean = true
    )

    fun generateReport(pdfReportGenerator: com.example.kadaliv2.data.report.PdfReportGenerator, onSuccess: (String) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                // Get fresh data
                val reportData = com.example.kadaliv2.domain.usecase.GenerateReportUseCase(
                    getRoomsUseCase, getAllDevicesUseCase, getTariffUseCase, calculateEnergyUseCase
                ).invoke()
                
                // Generate PDF (IO operation should be on IO dispatcher ideally, but PdfReportGenerator uses simplified main thread methods. 
                // For proper IO, we should switch context.
                // Let's assume PdfReportGenerator is lightweight or we wrap it.
                // Actually PdfReportGenerator uses IO stream, so better safe.
                val path = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    pdfReportGenerator.generateReport(reportData)
                }
                
                if (path != null) {
                    onSuccess(path)
                } else {
                    onError()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }
}
