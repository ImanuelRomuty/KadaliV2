package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.remote.FirestoreService
import com.example.kadaliv2.domain.model.Device
import com.example.kadaliv2.domain.repository.DeviceRepository
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepositoryImpl(
    private val firestoreService: FirestoreService
) : DeviceRepository {

    private val collection = "devices"

    override fun getDevicesByRoomId(roomId: String): Flow<List<Device>> {
        return firestoreService.getDocumentsByFieldFlow(collection, "roomId", roomId).map { docs ->
            docs.map { (id, data) ->
                Device(
                    id = id,
                    roomId = data["roomId"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    powerWatt = (data["powerWatt"] as? Number)?.toDouble() ?: 0.0,
                    usageHoursPerDay = (data["usageHours"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (data["quantity"] as? Number)?.toInt() ?: 1
                )
            }
        }
    }

    override suspend fun getDeviceById(id: String): Device? {
        val data = firestoreService.getDocument(collection, id) ?: return null
        return Device(
            id = id,
            roomId = data["roomId"] as? String ?: "",
            name = data["name"] as? String ?: "",
            powerWatt = (data["powerWatt"] as? Number)?.toDouble() ?: 0.0,
            usageHoursPerDay = (data["usageHours"] as? Number)?.toDouble() ?: 0.0,
            quantity = (data["quantity"] as? Number)?.toInt() ?: 1
        )
    }

    override suspend fun insertDevice(device: Device) {
        val data = mapOf(
            "name" to device.name,
            "powerWatt" to device.powerWatt,
            "usageHours" to device.usageHoursPerDay,
            "quantity" to device.quantity,
            "roomId" to device.roomId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestoreService.addDocument(collection, data)
    }

    override suspend fun updateDevice(device: Device) {
        val data = mapOf(
            "name" to device.name,
            "powerWatt" to device.powerWatt,
            "usageHours" to device.usageHoursPerDay,
            "quantity" to device.quantity,
            "roomId" to device.roomId
        )
        firestoreService.updateDocument(collection, device.id, data)
    }

    override suspend fun deleteDevice(device: Device) {
        firestoreService.deleteDocument(collection, device.id)
    }

    override fun getAllDevices(): Flow<List<Device>> {
        return firestoreService.getDocumentsFlow(collection).map { docs ->
            docs.map { (id, data) ->
                Device(
                    id = id,
                    roomId = data["roomId"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    powerWatt = (data["powerWatt"] as? Number)?.toDouble() ?: 0.0,
                    usageHoursPerDay = (data["usageHours"] as? Number)?.toDouble() ?: 0.0,
                    quantity = (data["quantity"] as? Number)?.toInt() ?: 1
                )
            }
        }
    }
}
