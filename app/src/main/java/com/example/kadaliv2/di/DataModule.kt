package com.example.kadaliv2.di

import com.example.kadaliv2.data.remote.FirestoreService
import com.example.kadaliv2.data.repository.DeviceRepositoryImpl
import com.example.kadaliv2.data.repository.RoomRepositoryImpl
import com.example.kadaliv2.data.repository.TariffRepositoryImpl
import com.example.kadaliv2.domain.repository.DeviceRepository
import com.example.kadaliv2.domain.repository.RoomRepository
import com.example.kadaliv2.domain.repository.TariffRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // Firestore remote layer
    single { FirestoreService() }

    // Repositories (Cloud-First)
    single<RoomRepository> { RoomRepositoryImpl(get()) }
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<TariffRepository> { TariffRepositoryImpl(get()) }
    
    single { com.example.kadaliv2.data.report.PdfReportGenerator(androidContext()) }
}
