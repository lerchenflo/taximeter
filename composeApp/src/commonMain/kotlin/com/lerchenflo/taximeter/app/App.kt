package com.lerchenflo.taximeter.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.taximeter.app.navigation.AppNavGraph

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        )
    }
}
