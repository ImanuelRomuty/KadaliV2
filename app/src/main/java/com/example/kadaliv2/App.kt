package com.example.kadaliv2

import android.app.Application
import com.example.kadaliv2.di.dataModule
import com.example.kadaliv2.di.domainModule
import com.example.kadaliv2.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(dataModule, domainModule, presentationModule))
        }
    }
}
