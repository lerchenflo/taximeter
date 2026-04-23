package com.lerchenflo.taximeter.taximeter.presentation

import androidx.compose.runtime.Composable

interface NotificationPermissionState {
    val hasPermission: Boolean
    fun requestPermission()
}

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState
