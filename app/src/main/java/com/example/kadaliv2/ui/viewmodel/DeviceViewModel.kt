package com.example.kadaliv2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kadaliv2.domain.model.Device
import com.example.kadaliv2.domain.usecase.CalculateEnergyUseCase
import com.example.kadaliv2.domain.usecase.DeleteDeviceUseCase
import com.example.kadaliv2.domain.usecase.GetRoomDevicesUseCase
import com.example.kadaliv2.domain.usecase.GetTariffUseCase
import com.example.kadaliv2.domain.usecase.SaveDeviceUseCase
import com.example.kadaliv2.domain.usecase.UpdateDeviceUseCase
import com.example.kadaliv2.domain.usecase.GetDeviceByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeviceViewModel(
    private val getRoomDevicesUseCase: GetRoomDevicesUseCase,
    private val saveDeviceUseCase: SaveDeviceUseCase,
    private val updateDeviceUseCase: UpdateDeviceUseCase,
    private val deleteDeviceUseCase: DeleteDeviceUseCase,
    private val getDeviceByIdUseCase: GetDeviceByIdUseCase,
    getTariffUseCase: GetTariffUseCase,
    private val calculateEnergyUseCase: CalculateEnergyUseCase
) : ViewModel() {

    private val _roomId = MutableStateFlow<String>("")
    
    val roomDevices = _roomId.flatMapLatest { id ->
        getRoomDevicesUseCase(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val tariff = getTariffUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roomStats = combine(roomDevices, tariff) { devices, tariffValue ->
        val totalEnergy = devices.sumOf { calculateEnergyUseCase.calculateDailyEnergy(it) }
        val totalCost = if (tariffValue != null) {
            calculateEnergyUseCase.calculateDailyCost(totalEnergy, tariffValue.pricePerKwh)
        } else {
            0.0
        }
        val monthlyCost = calculateEnergyUseCase.calculateMonthlyCost(totalCost)
        Pair(totalEnergy, monthlyCost)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0.0, 0.0))

    private val _device = MutableStateFlow<Device?>(null)
    val device = _device.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getDevice(id: String) {
        viewModelScope.launch {
            _device.value = getDeviceByIdUseCase(id)
        }
    }

    fun setRoomId(id: String) {
        _roomId.value = id
    }

    fun saveDevice(roomId: String, name: String, power: Double, hours: Double, quantity: Int) {
        viewModelScope.launch {
            val device = Device(
                roomId = roomId,
                name = name,
                powerWatt = power,
                usageHoursPerDay = hours,
                quantity = quantity
            )
            saveDeviceUseCase(device)
        }
    }

    fun updateDevice(id: String, roomId: String, name: String, power: Double, hours: Double, quantity: Int) {
        viewModelScope.launch {
            val device = Device(
                id = id,
                roomId = roomId,
                name = name,
                powerWatt = power,
                usageHoursPerDay = hours,
                quantity = quantity
            )
            updateDeviceUseCase(device)
        }
    }
    
    fun deleteDevice(device: Device) {
        viewModelScope.launch {
            deleteDeviceUseCase(device)
        }
    }
}
