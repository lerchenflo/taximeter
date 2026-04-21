package com.lerchenflo.taximeter.di

import com.lerchenflo.taximeter.datasource.Preferencemanager
import com.lerchenflo.taximeter.datasource.database.AppDatabase
import com.lerchenflo.taximeter.datasource.database.CreateAppDatabase
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.passenger.presentation.passenger_list.PassengerListViewModel
import com.lerchenflo.taximeter.passenger.presentation.passenger_routes.PassengerRoutesViewModel
import com.lerchenflo.taximeter.taximeter.presentation.TaximeterViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    single<AppDatabase> { CreateAppDatabase(get()).getDatabase() }

    single { get<AppDatabase>().passengerDao() }
    single { get<AppDatabase>().routeDao() }
    single { get<AppDatabase>().routePointDao() }

    singleOf(::Preferencemanager)
    singleOf(::PassengerRepository)
    singleOf(::RouteRepository)

    viewModelOf(::PassengerListViewModel)
    viewModelOf(::PassengerRoutesViewModel)
    viewModelOf(::TaximeterViewModel)
}
