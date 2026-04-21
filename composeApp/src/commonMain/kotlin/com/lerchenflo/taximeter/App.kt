package com.lerchenflo.taximeter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.lerchenflo.taximeter.navigation.AppNavGraph

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.imePadding()
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}
