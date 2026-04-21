package com.lerchenflo.taximeter.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState {
    return remember {
        object : LocationPermissionState {
            override val hasPermission: Boolean = false
            override fun requestPermission() {}
        }
    }
}
