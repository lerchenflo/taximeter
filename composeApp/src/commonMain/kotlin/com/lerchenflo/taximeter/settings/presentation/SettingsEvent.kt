package com.lerchenflo.taximeter.settings.presentation

sealed interface SettingsEvent {
    data object NavigateBack : SettingsEvent
}
