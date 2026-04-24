package com.lerchenflo.taximeter.taximeter.presentation

import com.lerchenflo.taximeter.taximeter.domain.GpsError

sealed interface TaximeterEvent {
    data object RouteCompleted : TaximeterEvent
    data object NavigateBack : TaximeterEvent
    data object RequestLocationPermission : TaximeterEvent
    data class GpsErrorOccurred(val error: GpsError) : TaximeterEvent
}
