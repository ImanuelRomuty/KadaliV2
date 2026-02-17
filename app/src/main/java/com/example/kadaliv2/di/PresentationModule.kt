package com.example.kadaliv2.di

import com.example.kadaliv2.ui.viewmodel.DashboardViewModel
import com.example.kadaliv2.ui.viewmodel.DeviceViewModel
import com.example.kadaliv2.ui.viewmodel.RoomViewModel
import com.example.kadaliv2.ui.viewmodel.SettingsViewModel
import com.example.kadaliv2.ui.viewmodel.SimulationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { RoomViewModel(get(), get(), get(), get()) }
    viewModel { DeviceViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { SimulationViewModel(get()) }
}

