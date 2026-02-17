package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.local.dao.RoomDao
import com.example.kadaliv2.data.local.entity.toDomain
import com.example.kadaliv2.data.local.entity.toEntity
import com.example.kadaliv2.domain.model.Room
import com.example.kadaliv2.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRepositoryImpl(private val roomDao: RoomDao) : RoomRepository {
    override fun getAllRooms(): Flow<List<Room>> {
        return roomDao.getAllRooms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRoomById(id: Long): Room? {
        return roomDao.getRoomById(id)?.toDomain()
    }

    override suspend fun insertRoom(room: Room): Long {
        return roomDao.insertRoom(room.toEntity())
    }

    override suspend fun updateRoom(room: Room) {
        roomDao.updateRoom(room.toEntity())
    }

    override suspend fun deleteRoom(room: Room) {
        roomDao.deleteRoom(room.toEntity())
    }
}
