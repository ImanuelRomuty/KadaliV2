package com.example.kadaliv2.domain.usecase

import com.example.kadaliv2.domain.model.Device
import com.example.kadaliv2.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class GetRoomDevicesUseCase(private val repository: DeviceRepository) {
    operator fun invoke(roomId: Long): Flow<List<Device>> = repository.getDevicesByRoomId(roomId)
}

class SaveDeviceUseCase(private val repository: DeviceRepository) {
    suspend operator fun invoke(device: Device): Long = repository.insertDevice(device)
}

class DeleteDeviceUseCase(private val repository: DeviceRepository) {
    suspend operator fun invoke(device: Device) = repository.deleteDevice(device)
}

class UpdateDeviceUseCase(private val repository: DeviceRepository) {
    suspend operator fun invoke(device: Device) = repository.updateDevice(device)
}

class GetDeviceByIdUseCase(private val repository: DeviceRepository) {
    suspend operator fun invoke(id: Long): Device? = repository.getDeviceById(id)
}

class GetAllDevicesUseCase(private val repository: DeviceRepository) {
    operator fun invoke(): Flow<List<Device>> = repository.getAllDevices()
}
