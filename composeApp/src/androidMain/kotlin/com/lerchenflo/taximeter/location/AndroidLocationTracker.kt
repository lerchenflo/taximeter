package com.lerchenflo.taximeter.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.lerchenflo.taximeter.taximeter.domain.LocationPoint
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationTracker(
    private val context: Context
) : LocationTracker {

    private var locationManager: LocationManager? = null

    @SuppressLint("MissingPermission")
    override fun startTracking(): Flow<LocationPoint> = callbackFlow {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = manager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(
                    LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time
                    )
                )
            }

            @Deprecated("Deprecated in API")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val provider = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider != null) {
            manager.requestLocationUpdates(provider, 2000L, 5f, listener)
        } else {
            close(IllegalStateException("No location provider available"))
        }

        awaitClose {
            manager.removeUpdates(listener)
        }
    }

    override fun stopTracking() {
        locationManager = null
    }
}
