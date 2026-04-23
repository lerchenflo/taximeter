package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

sealed interface PassengerRoutesEvent {
    data object NavigateToHome : PassengerRoutesEvent
    data class NavigateToTaximeter(val passengerId: Long, val routeId: Long) : PassengerRoutesEvent
    data object NavigateBack : PassengerRoutesEvent
    data class NavigateToRouteMap(val passengerId: Long) : PassengerRoutesEvent
}
