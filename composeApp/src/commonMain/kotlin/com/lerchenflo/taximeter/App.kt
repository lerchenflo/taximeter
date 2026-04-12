package com.lerchenflo.taximeter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lerchenflo.taximeter.mainscreen.location.LocationScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Scaffold(
            modifier = Modifier
                .imePadding()
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                LocationScreen()
            }
        }
    }
}