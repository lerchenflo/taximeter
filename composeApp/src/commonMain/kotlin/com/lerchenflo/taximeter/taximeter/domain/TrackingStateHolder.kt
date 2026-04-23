package com.lerchenflo.taximeter.taximeter.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrackingStateData(
    val isRunning: Boolean = false,
    val routeId: Long = -1L,
    val distanceMeters: Double = 0.0,
    val currentPrice: Double = 0.0,
    val durationSeconds: Long = 0L
)

class TrackingStateHolder {
    private val _state = MutableStateFlow(TrackingStateData())
    val state: StateFlow<TrackingStateData> = _state.asStateFlow()

    fun update(transform: (TrackingStateData) -> TrackingStateData) {
        _state.update(transform)
    }

    fun reset() {
        _state.value = TrackingStateData()
    }
}
