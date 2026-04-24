package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.datasource.database.entities.Route

data class PassengerRoutesState(
    val passenger: Passenger? = null,
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
    val isStartRideDialogVisible: Boolean = false,
    val newRouteName: String = "",
    val routeToDeleteId: Long? = null
)
