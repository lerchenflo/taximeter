package com.lerchenflo.taximeter.di

import androidx.room.RoomDatabase
import com.lerchenflo.taximeter.database.androidAppDatabaseBuilder
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidUserDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        androidAppDatabaseBuilder(androidContext())
    }
}
