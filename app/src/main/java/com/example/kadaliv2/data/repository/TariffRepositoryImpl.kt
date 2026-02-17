package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.local.dao.TariffDao
import com.example.kadaliv2.data.local.entity.toDomain
import com.example.kadaliv2.data.local.entity.toEntity
import com.example.kadaliv2.domain.model.Tariff
import com.example.kadaliv2.domain.repository.TariffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TariffRepositoryImpl(private val tariffDao: TariffDao) : TariffRepository {
    override fun getTariff(): Flow<Tariff?> {
        return tariffDao.getTariff().map { it?.toDomain() }
    }

    override suspend fun saveTariff(tariff: Tariff) {
        tariffDao.saveTariff(tariff.toEntity())
    }
}
