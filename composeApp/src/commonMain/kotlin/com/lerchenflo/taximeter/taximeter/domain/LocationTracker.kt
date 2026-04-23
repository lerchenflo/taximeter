package com.lerchenflo.taximeter.taximeter.domain

import kotlinx.coroutines.flow.Flow

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Float = 0f
)

interface LocationTracker {
    fun startTracking(): Flow<LocationPoint>
    fun stopTracking()
}
