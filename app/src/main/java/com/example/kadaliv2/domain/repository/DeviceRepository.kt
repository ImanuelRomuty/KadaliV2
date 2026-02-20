package com.example.kadaliv2.domain.repository

import com.example.kadaliv2.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDevicesByRoomId(roomId: String): Flow<List<Device>>
    suspend fun getDeviceById(id: String): Device?
    suspend fun insertDevice(device: Device)
    suspend fun updateDevice(device: Device)
    suspend fun deleteDevice(device: Device)
    fun getAllDevices(): Flow<List<Device>>
}
