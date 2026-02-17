package com.example.kadaliv2.di

import androidx.room.Room
import com.example.kadaliv2.data.local.AppDatabase
import com.example.kadaliv2.data.repository.DeviceRepositoryImpl
import com.example.kadaliv2.data.repository.RoomRepositoryImpl
import com.example.kadaliv2.data.repository.TariffRepositoryImpl
import com.example.kadaliv2.domain.repository.DeviceRepository
import com.example.kadaliv2.domain.repository.RoomRepository
import com.example.kadaliv2.domain.repository.TariffRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "kadali_v2_db"
        ).build()
    }

    single { get<AppDatabase>().roomDao() }
    single { get<AppDatabase>().deviceDao() }
    single { get<AppDatabase>().tariffDao() }

    single<RoomRepository> { RoomRepositoryImpl(get()) }
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<TariffRepository> { TariffRepositoryImpl(get()) }
    
    single { com.example.kadaliv2.data.report.PdfReportGenerator(androidContext()) }
}
