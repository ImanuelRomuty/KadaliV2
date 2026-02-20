package com.example.kadaliv2.domain.usecase

import com.example.kadaliv2.domain.model.Room
import com.example.kadaliv2.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow

class GetRoomsUseCase(private val repository: RoomRepository) {
    operator fun invoke(): Flow<List<Room>> = repository.getAllRooms()
}

class SaveRoomUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(room: Room) = repository.insertRoom(room)
}

class DeleteRoomUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(room: Room) = repository.deleteRoom(room)
}

class UpdateRoomUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(room: Room) = repository.updateRoom(room)
}

class GetRoomByIdUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(id: String): Room? = repository.getRoomById(id)
}
