package com.lerchenflo.taximeter.service

import kotlinx.coroutines.flow.Flow

expect class LocationService {
    fun observeLocation(): Flow<DeviceLocation>
}

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)