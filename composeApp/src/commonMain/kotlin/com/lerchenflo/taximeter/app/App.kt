package com.lerchenflo.taximeter.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.taximeter.app.navigation.AppNavGraph
import com.lerchenflo.taximeter.app.theme.TaximeterTheme
import com.lerchenflo.taximeter.taximeter.presentation.rememberNotificationPermissionState

@Composable
fun App() {
    TaximeterTheme {
        val notifPermissionState = rememberNotificationPermissionState()
        LaunchedEffect(Unit) {
            if (!notifPermissionState.hasPermission) {
                notifPermissionState.requestPermission()
            }
        }

        val navController = rememberNavController()
        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        )
    }
}
