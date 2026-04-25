package com.lerchenflo.taximeter.settings.domain

enum class VehicleType {
    CAR, MOTORCYCLE;

    fun emoji(): String = when (this) {
        CAR -> "🚗"
        MOTORCYCLE -> "🏍"
    }
}
