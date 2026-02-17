package com.example.kadaliv2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kadaliv2.domain.model.Tariff
import com.example.kadaliv2.domain.usecase.GetTariffUseCase
import com.example.kadaliv2.domain.usecase.SaveTariffUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    getTariffUseCase: GetTariffUseCase,
    private val saveTariffUseCase: SaveTariffUseCase
) : ViewModel() {

    val tariff = getTariffUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveTariff(price: Double) {
        viewModelScope.launch {
            saveTariffUseCase(Tariff(pricePerKwh = price))
        }
    }
}
