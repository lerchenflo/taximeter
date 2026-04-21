package com.lerchenflo.taximeter.passenger.presentation.passenger_list

sealed interface PassengerListEvent {
    data class NavigateToPassengerRoutes(val passengerId: Long) : PassengerListEvent
}
