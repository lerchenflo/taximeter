package com.lerchenflo.taximeter.home.presentation

import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.datasource.database.entities.RouteWithPassenger

data class HomeState(
    val activeRoute: Route? = null,
    val activePassenger: Passenger? = null,
    val recentRoutes: List<RouteWithPassenger> = emptyList(),
    val isLoading: Boolean = true,
    val isRunning: Boolean = false,
    val currentPrice: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val baseFare: Double = 3.50,
    val pricePerKm: Double = 1.80,
    val hasLocationPermission: Boolean = false
)
