package com.lerchenflo.taximeter.home.presentation

sealed interface HomeAction {
    data object OpenCustomerPicker : HomeAction
    data object OpenSettings : HomeAction
    data object ShowAllRoutesMap : HomeAction
    data class SelectRecentRoute(val routeId: Long) : HomeAction
}
