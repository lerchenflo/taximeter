package com.lerchenflo.taximeter.di

import com.lerchenflo.taximeter.datasource.AppRepository
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.datasource.database.CreateAppDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sharedModule = module {
    // Database: expects platform-specific RoomDatabase.Builder in graph
    single<AppDatabase> { CreateAppDatabase(get()).getDatabase() }

    // App repository
    singleOf(::AppRepository)
}
