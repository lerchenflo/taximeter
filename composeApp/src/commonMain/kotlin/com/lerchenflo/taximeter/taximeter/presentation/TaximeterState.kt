package com.lerchenflo.taximeter.taximeter.presentation

data class TaximeterState(
    val isRunning: Boolean = false,
    val currentPrice: Double = 0.0,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val baseFare: Double = 3.50,
    val pricePerKm: Double = 1.80,
    val hasLocationPermission: Boolean = false,
    val isRouteCompleted: Boolean = false,
    val passengerName: String = "",
    val pendingStart: Boolean = false,
    val gpsFixFresh: Boolean = false,
    val gpsSearching: Boolean = false,
)
