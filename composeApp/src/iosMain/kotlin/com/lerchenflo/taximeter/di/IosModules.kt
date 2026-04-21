package com.lerchenflo.taximeter.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.location.IosLocationTracker
import org.koin.dsl.module

val iosDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> { iosAppDatabaseBuilder() }
    single<LocationTracker> { IosLocationTracker() }
    single<DataStore<Preferences>> { iosDatastoreBuilder() }
}
