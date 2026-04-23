package com.lerchenflo.taximeter.home.presentation

sealed interface HomeEvent {
    data object NavigateToCustomerPicker : HomeEvent
    data object NavigateToSettings : HomeEvent
    data object NavigateToRouteMap : HomeEvent
    data class NavigateToTaximeter(val passengerId: Long, val routeId: Long) : HomeEvent
    data object RequestLocationPermission : HomeEvent
}
