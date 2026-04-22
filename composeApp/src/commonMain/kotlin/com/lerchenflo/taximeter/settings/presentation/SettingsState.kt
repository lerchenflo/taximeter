package com.lerchenflo.taximeter.settings.presentation

data class SettingsState(
    val baseFare: String = "",
    val pricePerKm: String = "",
    val isSaved: Boolean = false
)
