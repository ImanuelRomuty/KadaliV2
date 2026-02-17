package com.example.kadaliv2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kadaliv2.domain.model.Tariff

@Entity(tableName = "tariff")
data class TariffEntity(
    @PrimaryKey
    val id: Int = 1,
    val pricePerKwh: Double
)

fun TariffEntity.toDomain() = Tariff(
    id = id,
    pricePerKwh = pricePerKwh
)

fun Tariff.toEntity() = TariffEntity(
    id = id,
    pricePerKwh = pricePerKwh
)
