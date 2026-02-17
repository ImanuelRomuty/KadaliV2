package com.example.kadaliv2.domain.repository

import com.example.kadaliv2.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun getAllRooms(): Flow<List<Room>>
    suspend fun getRoomById(id: Long): Room?
    suspend fun insertRoom(room: Room): Long
    suspend fun updateRoom(room: Room)
    suspend fun deleteRoom(room: Room)
}
