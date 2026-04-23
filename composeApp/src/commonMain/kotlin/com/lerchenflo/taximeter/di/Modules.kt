package com.lerchenflo.taximeter.di

import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.datasource.database.CreateAppDatabase
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.home.presentation.HomeViewModel
import com.lerchenflo.taximeter.passenger.presentation.passenger_list.PassengerListViewModel
import com.lerchenflo.taximeter.passenger.presentation.passenger_routes.PassengerRoutesViewModel
import com.lerchenflo.taximeter.routemap.presentation.RouteMapViewModel
import com.lerchenflo.taximeter.settings.presentation.SettingsViewModel
import com.lerchenflo.taximeter.taximeter.presentation.TaximeterViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val datasourceModule = module {
    single<AppDatabase> { CreateAppDatabase(get()).getDatabase() }

    single { get<AppDatabase>().passengerDao() }
    single { get<AppDatabase>().routeDao() }
    single { get<AppDatabase>().routePointDao() }

    singleOf(::Preferencemanager)
    singleOf(::PassengerRepository)
    singleOf(::RouteRepository)
}

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::PassengerListViewModel)
    viewModelOf(::PassengerRoutesViewModel)
    viewModelOf(::TaximeterViewModel)
    viewModelOf(::RouteMapViewModel)
    viewModelOf(::SettingsViewModel)
}
