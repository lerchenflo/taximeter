package com.lerchenflo.taximeter.core.presentation

import androidx.compose.runtime.Composable

@Composable
expect fun rememberLocationPermissionState(): LocationPermissionState

interface LocationPermissionState {
    val hasPermission: Boolean
    fun requestPermission()
}
