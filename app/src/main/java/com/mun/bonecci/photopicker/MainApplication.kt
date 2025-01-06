package com.mun.bonecci.photopicker

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module



class MainApplication : Application() {

    val appModule = module {
        viewModel { MainViewModel() }
    }

    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }
}