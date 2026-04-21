package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

sealed interface PassengerRoutesAction {
    data object StartNewRoute : PassengerRoutesAction
    data class SelectRoute(val routeId: Long) : PassengerRoutesAction
    data object GoBack : PassengerRoutesAction
}
