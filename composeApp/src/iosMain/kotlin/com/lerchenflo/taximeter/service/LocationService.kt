package com.lerchenflo.taximeter.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

actual class LocationService {

    @OptIn(ExperimentalForeignApi::class)
    actual fun observeLocation(): Flow<DeviceLocation> = callbackFlow {
        val locationManager = CLLocationManager()

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                    ?: return
                trySend(
                    DeviceLocation(
                        latitude = location.coordinate.latitude,
                        longitude = location.coordinate.longitude,
                        timestamp = (location.timestamp.timeIntervalSince1970 * 1000).toLong()
                    )
                )
            }

            override fun locationManager(
                manager: CLLocationManager,
                didFailWithError: NSError
            ) {
                // Location updates failed — flow stays open, waiting for recovery
            }
        }

        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
        }
    }
}