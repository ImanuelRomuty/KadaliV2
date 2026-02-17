package com.example.kadaliv2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kadaliv2.data.local.dao.DeviceDao
import com.example.kadaliv2.data.local.dao.RoomDao
import com.example.kadaliv2.data.local.dao.TariffDao
import com.example.kadaliv2.data.local.entity.DeviceEntity
import com.example.kadaliv2.data.local.entity.RoomEntity
import com.example.kadaliv2.data.local.entity.TariffEntity

@Database(
    entities = [RoomEntity::class, DeviceEntity::class, TariffEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun deviceDao(): DeviceDao
    abstract fun tariffDao(): TariffDao
}
