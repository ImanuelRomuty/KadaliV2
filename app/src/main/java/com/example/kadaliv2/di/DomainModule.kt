package com.example.kadaliv2.di

import com.example.kadaliv2.domain.usecase.CalculateEnergyUseCase
import com.example.kadaliv2.domain.usecase.DeleteDeviceUseCase
import com.example.kadaliv2.domain.usecase.DeleteRoomUseCase
import com.example.kadaliv2.domain.usecase.GenerateReportUseCase
import com.example.kadaliv2.domain.usecase.GetAllDevicesUseCase
import com.example.kadaliv2.domain.usecase.GetDeviceByIdUseCase
import com.example.kadaliv2.domain.usecase.GetRoomDevicesUseCase
import com.example.kadaliv2.domain.usecase.GetRoomsUseCase
import com.example.kadaliv2.domain.usecase.GetRoomByIdUseCase
import com.example.kadaliv2.domain.usecase.GetTariffUseCase
import com.example.kadaliv2.domain.usecase.SaveDeviceUseCase
import com.example.kadaliv2.domain.usecase.SaveRoomUseCase
import com.example.kadaliv2.domain.usecase.SaveTariffUseCase
import com.example.kadaliv2.domain.usecase.UpdateDeviceUseCase
import com.example.kadaliv2.domain.usecase.UpdateRoomUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CalculateEnergyUseCase() }
    
    factory { GetRoomsUseCase(get()) }
    factory { SaveRoomUseCase(get()) }
    factory { UpdateRoomUseCase(get()) }
    factory { DeleteRoomUseCase(get()) }
    factory { GetRoomByIdUseCase(get()) }
    
    factory { GetRoomDevicesUseCase(get()) }
    factory { SaveDeviceUseCase(get()) }
    factory { UpdateDeviceUseCase(get()) }
    factory { DeleteDeviceUseCase(get()) }
    factory { GetDeviceByIdUseCase(get()) }
    factory { GetAllDevicesUseCase(get()) }
    
    factory { GetTariffUseCase(get()) }
    factory { SaveTariffUseCase(get()) }
    
    factory { GenerateReportUseCase(get(), get(), get(), get()) }
}
