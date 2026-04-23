package com.lerchenflo.taximeter.taximeter.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState = remember {
    object : NotificationPermissionState {
        override val hasPermission = true
        override fun requestPermission() {}
    }
}
