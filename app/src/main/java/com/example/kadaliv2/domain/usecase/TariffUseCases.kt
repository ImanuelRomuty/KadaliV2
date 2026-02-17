package com.example.kadaliv2.domain.usecase

import com.example.kadaliv2.domain.model.Tariff
import com.example.kadaliv2.domain.repository.TariffRepository
import kotlinx.coroutines.flow.Flow

class GetTariffUseCase(private val repository: TariffRepository) {
    operator fun invoke(): Flow<Tariff?> = repository.getTariff()
}

class SaveTariffUseCase(private val repository: TariffRepository) {
    suspend operator fun invoke(tariff: Tariff) = repository.saveTariff(tariff)
}
