package com.lerchenflo.taximeter.service

import android.content.Context
import android.content.Intent
import com.lerchenflo.taximeter.taximeter.domain.TrackingServiceController

class AndroidTrackingServiceController(
    private val context: Context
) : TrackingServiceController {

    override fun startTracking(routeId: Long) {
        val intent = Intent(context, TrackingService::class.java).apply {
            putExtra("routeId", routeId)
        }
        context.startForegroundService(intent)
    }

    override fun stopTracking() {
        context.stopService(Intent(context, TrackingService::class.java))
    }
}
