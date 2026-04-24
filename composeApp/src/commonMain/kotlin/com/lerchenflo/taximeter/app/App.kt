package com.lerchenflo.taximeter.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.taximeter.app.navigation.AppNavGraph
import com.lerchenflo.taximeter.app.theme.TaximeterTheme

@Composable
fun App() {
    TaximeterTheme {
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
