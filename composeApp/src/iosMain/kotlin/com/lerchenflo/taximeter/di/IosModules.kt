package com.lerchenflo.taximeter.di

import androidx.room.RoomDatabase
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.service.LocationService
import org.koin.dsl.module

val iosDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { iosAppDatabaseBuilder() }

    // Location
    single { LocationService() }
}
