package com.lerchenflo.taximeter.routemap.presentation

sealed interface RouteMapEvent {
    data object NavigateBack : RouteMapEvent
}
