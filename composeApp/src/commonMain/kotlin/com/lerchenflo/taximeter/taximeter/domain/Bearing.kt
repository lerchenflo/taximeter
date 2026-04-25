package com.lerchenflo.taximeter.taximeter.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val y = sin(dLon) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360) % 360).toFloat()
}
