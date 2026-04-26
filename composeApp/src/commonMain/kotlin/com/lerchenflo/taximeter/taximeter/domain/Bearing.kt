package com.lerchenflo.taximeter.taximeter.domain

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val lat1Rad = lat1 * PI / 180.0
    val lat2Rad = lat2 * PI / 180.0
    val dLonRad = (lon2 - lon1) * PI / 180.0
    val y = sin(dLonRad) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLonRad)
    val deg = atan2(y, x) * 180.0 / PI
    return ((deg + 360.0) % 360.0).toFloat()
}
