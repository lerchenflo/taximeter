package com.lerchenflo.taximeter.settings.presentation

sealed interface SettingsAction {
    data class UpdateBaseFare(val value: String) : SettingsAction
    data class UpdatePricePerKm(val value: String) : SettingsAction
    data object ToggleBgGps : SettingsAction
    data object ToggleSpeedColor : SettingsAction
    data object ToggleNotifMeter : SettingsAction
    data object Save : SettingsAction
    data object GoBack : SettingsAction
}
