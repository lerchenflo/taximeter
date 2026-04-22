package com.lerchenflo.taximeter.routemap.presentation

import com.lerchenflo.taximeter.datasource.database.entities.Passenger

data class RouteMapState(
    val isLoading: Boolean = true,
    val passengers: List<Passenger> = emptyList(),
    val selectedPassengerId: Long = -1L,
    val routePolylines: List<RoutePolyline> = emptyList()
)

data class RoutePolyline(
    val routeId: Long,
    val passengerName: String,
    val latitudes: List<Double>,
    val longitudes: List<Double>,
    val colorIndex: Int
)
