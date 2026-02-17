package com.example.kadaliv2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kadaliv2.data.local.entity.TariffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TariffDao {
    @Query("SELECT * FROM tariff WHERE id = 1")
    fun getTariff(): Flow<TariffEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTariff(tariff: TariffEntity)
}
