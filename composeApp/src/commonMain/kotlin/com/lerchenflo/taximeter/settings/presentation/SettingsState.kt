package com.lerchenflo.taximeter.settings.presentation

import com.lerchenflo.taximeter.settings.domain.SpeedScale
import com.lerchenflo.taximeter.settings.domain.VehicleType

data class SettingsState(
    val baseFare: String = "",
    val pricePerKm: String = "",
    val idleRate: String = "",
    val gpsIntervalMs: String = "2000",
    val gpsMinDistanceM: String = "5",
    val isSaved: Boolean = false,
    val vehicleType: VehicleType = VehicleType.CAR,
    val speedScale: SpeedScale = SpeedScale.MEDIUM_FAST,
    val isShowingClearConfirmDialog: Boolean = false,
    val isClearing: Boolean = false
)
