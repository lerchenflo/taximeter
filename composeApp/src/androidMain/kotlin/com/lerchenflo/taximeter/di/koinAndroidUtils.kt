package com.lerchenflo.taximeter.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun startKoinAndroid(androidContext: Context) {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            androidContext(androidContext)
            modules(androidUserDatabaseModule, sharedModule)
        }
    }
}
