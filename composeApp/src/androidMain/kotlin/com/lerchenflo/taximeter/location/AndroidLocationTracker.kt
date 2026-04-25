package com.lerchenflo.taximeter.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.lerchenflo.taximeter.taximeter.domain.LocationPoint
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationTracker(
    private val context: Context
) : LocationTracker {

    override fun startTracking(intervalMs: Long, minDistanceMeters: Float): Flow<LocationPoint> = callbackFlow {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val provider = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                close(IllegalStateException("No location provider enabled"))
                return@callbackFlow
            }
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(
                    LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time,
                        speed = if (location.hasSpeed()) location.speed else 0f
                    )
                )
            }

            @Deprecated("Deprecated in API")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        @SuppressLint("MissingPermission")
        manager.requestLocationUpdates(provider, intervalMs, minDistanceMeters, listener, Looper.getMainLooper())

        awaitClose {
            manager.removeUpdates(listener)
        }
    }

    override fun stopTracking() {}
}
