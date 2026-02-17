package com.example.kadaliv2.domain.repository

import com.example.kadaliv2.domain.model.Tariff
import kotlinx.coroutines.flow.Flow

interface TariffRepository {
    fun getTariff(): Flow<Tariff?>
    suspend fun saveTariff(tariff: Tariff)
}
