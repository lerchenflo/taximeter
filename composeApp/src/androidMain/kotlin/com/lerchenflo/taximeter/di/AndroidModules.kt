package com.lerchenflo.taximeter.di

import androidx.room.RoomDatabase
import com.lerchenflo.taximeter.core.domain.location.LocationTracker
import com.lerchenflo.taximeter.database.androidAppDatabaseBuilder
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.location.AndroidLocationTracker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidUserDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        androidAppDatabaseBuilder(androidContext())
    }
    single<LocationTracker> { AndroidLocationTracker(androidContext()) }
}
