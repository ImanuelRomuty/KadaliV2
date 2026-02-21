package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.remote.FirestoreService
import com.example.kadaliv2.domain.model.Room
import com.example.kadaliv2.domain.repository.RoomRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRepositoryImpl(
    private val firestoreService: FirestoreService
) : RoomRepository {

    private val collection = "rooms"

    override fun getAllRooms(): Flow<List<Room>> {
        return firestoreService.getDocumentsFlow(collection).map { docs ->
            docs.map { (id, data) ->
                Room(
                    id = id,
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String,
                    createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: 0L
                )
            }
        }
    }

    override suspend fun getRoomById(id: String): Room? {
        val data = firestoreService.getDocument(collection, id) ?: return null
        return Room(
            id = id,
            name = data["name"] as? String ?: "",
            description = data["description"] as? String,
            createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: 0L
        )
    }

    override suspend fun insertRoom(room: Room) {
        val data = mapOf(
            "name" to room.name,
            "description" to room.description,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestoreService.addDocument(collection, data)
    }

    override suspend fun updateRoom(room: Room) {
        val data = mapOf(
            "name" to room.name,
            "description" to room.description
        )
        firestoreService.updateDocument(collection, room.id, data)
    }

    override suspend fun deleteRoom(room: Room) {
        firestoreService.deleteDocument(collection, room.id)
    }
}
