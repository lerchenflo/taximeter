package com.lerchenflo.taximeter.di

import androidx.room.RoomDatabase
import com.lerchenflo.taximeter.core.domain.location.LocationTracker
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.location.IosLocationTracker
import org.koin.dsl.module

val iosDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { iosAppDatabaseBuilder() }
    single<LocationTracker> { IosLocationTracker() }
}
