package com.lerchenflo.taximeter.settings.presentation

import com.lerchenflo.taximeter.settings.domain.SpeedScale
import com.lerchenflo.taximeter.settings.domain.VehicleType

sealed interface SettingsAction {
    data class UpdateBaseFare(val value: String) : SettingsAction
    data class UpdatePricePerKm(val value: String) : SettingsAction
    data class UpdateIdleRate(val value: String) : SettingsAction
    data object Save : SettingsAction
    data object GoBack : SettingsAction
    data class UpdateVehicleType(val type: VehicleType) : SettingsAction
    data class UpdateSpeedScale(val scale: SpeedScale) : SettingsAction
    data class UpdateGpsInterval(val value: String) : SettingsAction
    data class UpdateGpsMinDistance(val value: String) : SettingsAction
    data object ShowClearConfirmation : SettingsAction
    data object ConfirmClearData : SettingsAction
    data object DismissClearConfirmation : SettingsAction
}
