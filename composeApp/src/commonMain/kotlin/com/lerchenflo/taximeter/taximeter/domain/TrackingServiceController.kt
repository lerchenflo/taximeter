package com.lerchenflo.taximeter.taximeter.domain

interface TrackingServiceController {
    fun startTracking(routeId: Long)
    fun stopTracking()
}
