package com.lerchenflo.taximeter.settings.domain

enum class SpeedScale(val maxKmh: Float, val thresholds: FloatArray) {
    SLOW(40f, floatArrayOf(5f, 10f, 20f, 30f, 40f)),
    MEDIUM_FAST(80f, floatArrayOf(10f, 25f, 40f, 55f, 80f)),
    FAST(160f, floatArrayOf(20f, 50f, 80f, 110f, 160f)),
}
