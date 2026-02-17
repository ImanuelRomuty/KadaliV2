package com.example.kadaliv2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.kadaliv2.domain.model.Device

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["roomId"])]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val name: String,
    val powerWatt: Double,
    val usageHoursPerDay: Double,
    val quantity: Int
)

fun DeviceEntity.toDomain() = Device(
    id = id,
    roomId = roomId,
    name = name,
    powerWatt = powerWatt,
    usageHoursPerDay = usageHoursPerDay,
    quantity = quantity
)

fun Device.toEntity() = DeviceEntity(
    id = id,
    roomId = roomId,
    name = name,
    powerWatt = powerWatt,
    usageHoursPerDay = usageHoursPerDay,
    quantity = quantity
)
