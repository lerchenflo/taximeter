package com.lerchenflo.taximeter

import androidx.compose.ui.window.ComposeUIViewController
import com.lerchenflo.taximeter.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { 
    onAppStart()
    App() 
}

fun onAppStart() {
    // Platform-specific app startup logic
}