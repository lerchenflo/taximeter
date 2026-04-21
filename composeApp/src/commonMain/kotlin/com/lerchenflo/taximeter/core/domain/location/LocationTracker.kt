package com.lerchenflo.taximeter.core.domain.location

import kotlinx.coroutines.flow.Flow

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

interface LocationTracker {
    fun startTracking(): Flow<LocationPoint>
    fun stopTracking()
}
