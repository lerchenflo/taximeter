package com.lerchenflo.taximeter.routemap.presentation

sealed interface RouteMapAction {
    data class SelectPassenger(val passengerId: Long) : RouteMapAction
    data object GoBack : RouteMapAction
}
