package com.lerchenflo.taximeter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.taximeter.domain.GpsError
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import com.lerchenflo.taximeter.taximeter.domain.TrackingStateHolder
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import com.lerchenflo.taximeter.utilities.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TrackingService : Service(), KoinComponent {

    private val locationTracker: LocationTracker by inject()
    private val routeRepository: RouteRepository by inject()
    private val preferencemanager: Preferencemanager by inject()
    private val trackingStateHolder: TrackingStateHolder by inject()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var locationJob: Job? = null
    private var timerJob: Job? = null

    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var totalDistance: Double = 0.0
    private var baseFare: Double = 0.0
    private var pricePerKm: Double = 0.0
    private var startTimeMillis: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val routeId = intent?.getLongExtra("routeId", -1L) ?: -1L
        if (routeId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Reset member state explicitly (new service instance each time, but be explicit)
        lastLat = null
        lastLon = null
        totalDistance = 0.0

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        startTimeMillis = currentTimeMillis()
        trackingStateHolder.reset()
        trackingStateHolder.update { it.copy(isRunning = true, routeId = routeId) }

        scope.launch {
            baseFare = preferencemanager.getBaseFare()
            pricePerKm = preferencemanager.getPricePerKm()
            val gpsIntervalMs = preferencemanager.getGpsIntervalMs()
            val gpsMinDistanceM = preferencemanager.getGpsMinDistanceM()

            val existingRoute = routeRepository.getRouteById(routeId)
            totalDistance = existingRoute?.totalDistanceMeters ?: 0.0

            trackingStateHolder.update {
                it.copy(
                    distanceMeters = totalDistance,
                    currentPrice = baseFare + (totalDistance / 1000.0) * pricePerKm
                )
            }

            timerJob = scope.launch {
                while (true) {
                    delay(1000)
                    val elapsed = (currentTimeMillis() - startTimeMillis) / 1000
                    trackingStateHolder.update { it.copy(durationSeconds = elapsed) }
                }
            }

            locationJob = scope.launch {
                try {
                    locationTracker.startTracking(gpsIntervalMs, gpsMinDistanceM).collect { point ->
                        trackingStateHolder.update {
                            it.copy(
                                lastFixTimestampMillis = currentTimeMillis(),
                                hasEverHadFix = true
                            )
                        }

                        val prevLat = lastLat
                        val prevLon = lastLon

                        if (prevLat != null && prevLon != null) {
                            val distance = haversineDistance(prevLat, prevLon, point.latitude, point.longitude)
                            if (distance > 2.0) {
                                totalDistance += distance
                                val newPrice = baseFare + (totalDistance / 1000.0) * pricePerKm

                                trackingStateHolder.update {
                                    it.copy(distanceMeters = totalDistance, currentPrice = newPrice)
                                }

                                routeRepository.addRoutePoint(routeId, point.latitude, point.longitude, point.speed)
                                val route = routeRepository.getRouteById(routeId)
                                if (route != null) {
                                    routeRepository.updateRoute(
                                        route.copy(totalDistanceMeters = totalDistance, totalPrice = newPrice)
                                    )
                                }
                            }
                        } else {
                            routeRepository.addRoutePoint(routeId, point.latitude, point.longitude, point.speed)
                        }

                        lastLat = point.latitude
                        lastLon = point.longitude
                    }
                } catch (e: SecurityException) {
                    trackingStateHolder.update { it.copy(gpsError = GpsError.PermissionRevoked) }
                    stopSelf()
                } catch (e: IllegalStateException) {
                    trackingStateHolder.update { it.copy(gpsError = GpsError.NoProvider) }
                    stopSelf()
                } catch (_: Exception) {
                    // transient GPS failure — keep service running, distance just won't advance
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationJob?.cancel()
        timerJob?.cancel()
        trackingStateHolder.update { it.copy(isRunning = false, routeId = -1L) }
        scope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ride Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Ride Active")
            .setContentText("Tracking your ride...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1001
    }
}
