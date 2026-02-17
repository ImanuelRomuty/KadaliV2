package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.local.dao.DeviceDao
import com.example.kadaliv2.data.local.entity.toDomain
import com.example.kadaliv2.data.local.entity.toEntity
import com.example.kadaliv2.domain.model.Device
import com.example.kadaliv2.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepositoryImpl(private val deviceDao: DeviceDao) : DeviceRepository {
    override fun getDevicesByRoomId(roomId: Long): Flow<List<Device>> {
        return deviceDao.getDevicesByRoomId(roomId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDeviceById(id: Long): Device? {
        return deviceDao.getDeviceById(id)?.toDomain()
    }

    override suspend fun insertDevice(device: Device): Long {
        return deviceDao.insertDevice(device.toEntity())
    }

    override suspend fun updateDevice(device: Device) {
        deviceDao.updateDevice(device.toEntity())
    }

    override suspend fun deleteDevice(device: Device) {
        deviceDao.deleteDevice(device.toEntity())
    }

    override fun getAllDevices(): Flow<List<Device>> {
        return deviceDao.getAllDevices().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
