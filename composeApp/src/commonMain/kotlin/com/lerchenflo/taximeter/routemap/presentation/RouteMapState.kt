package com.lerchenflo.taximeter.routemap.presentation

import androidx.compose.ui.unit.DpOffset
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.settings.domain.SpeedScale
import com.lerchenflo.taximeter.settings.domain.VehicleType

data class RouteMapState(
    val isLoading: Boolean = true,
    val passengers: List<Passenger> = emptyList(),
    val selectedPassengerId: Long = -1L,
    val routePolylines: List<RoutePolyline> = emptyList(),
    val vehicleType: VehicleType = VehicleType.CAR,
    val speedScale: SpeedScale = SpeedScale.MEDIUM_FAST,
    val tooltip: TooltipState? = null
)

data class TooltipState(
    val screenOffsetDp: DpOffset,
    val speedKmh: Float,
    val timestamp: Long
)

data class RoutePolyline(
    val routeId: Long,
    val passengerName: String,
    val latitudes: List<Double>,
    val longitudes: List<Double>,
    val timestamps: List<Long> = emptyList(),
    val color: Long,
    val speeds: List<Float> = emptyList(),
    val isLive: Boolean = false
)
