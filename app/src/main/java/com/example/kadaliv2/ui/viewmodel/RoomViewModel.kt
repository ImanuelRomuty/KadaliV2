package com.example.kadaliv2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kadaliv2.domain.model.Room
import com.example.kadaliv2.domain.usecase.DeleteRoomUseCase
import com.example.kadaliv2.domain.usecase.GetRoomByIdUseCase
import com.example.kadaliv2.domain.usecase.SaveRoomUseCase
import com.example.kadaliv2.domain.usecase.UpdateRoomUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val saveRoomUseCase: SaveRoomUseCase,
    private val updateRoomUseCase: UpdateRoomUseCase,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val getRoomByIdUseCase: GetRoomByIdUseCase
) : ViewModel() {

    private val _room = MutableStateFlow<Room?>(null)
    val room = _room.asStateFlow()

    fun getRoom(id: Long) {
        viewModelScope.launch {
            _room.value = getRoomByIdUseCase(id)
        }
    }

    fun saveRoom(name: String, description: String?) {
        viewModelScope.launch {
            val room = Room(name = name, description = description)
            saveRoomUseCase(room)
        }
    }
    
    fun updateRoom(id: Long, name: String, description: String?) {
        viewModelScope.launch {
            val room = Room(id = id, name = name, description = description)
            updateRoomUseCase(room)
        }
    }
    
    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            deleteRoomUseCase(room)
        }
    }
}
