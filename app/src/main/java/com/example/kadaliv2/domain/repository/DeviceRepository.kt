package com.example.kadaliv2.domain.repository

import com.example.kadaliv2.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDevicesByRoomId(roomId: Long): Flow<List<Device>>
    suspend fun getDeviceById(id: Long): Device?
    suspend fun insertDevice(device: Device): Long
    suspend fun updateDevice(device: Device)
    suspend fun deleteDevice(device: Device)
    fun getAllDevices(): Flow<List<Device>>
}
