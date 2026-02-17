package com.example.kadaliv2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kadaliv2.domain.model.Room

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?
)

fun RoomEntity.toDomain() = Room(
    id = id,
    name = name,
    description = description
)

fun Room.toEntity() = RoomEntity(
    id = id,
    name = name,
    description = description
)
