package com.lerchenflo.taximeter.taximeter.presentation

sealed interface TaximeterEvent {
    data object RouteCompleted : TaximeterEvent
    data object NavigateBack : TaximeterEvent
    data object RequestLocationPermission : TaximeterEvent
}
