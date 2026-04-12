package com.lerchenflo.taximeter.di

import org.koin.core.context.startKoin

fun initKoin() {
    if (org.koin.core.KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            modules(iosDatabaseModule, sharedModule)
        }
    }
}
