package com.lerchenflo.taximeter.homescreen.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.service.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long? = null,
    val isTracking: Boolean = false,
    val error: String? = null
)

sealed interface LocationEvent {
    data object StartTracking : LocationEvent
    data object StopTracking : LocationEvent
}

class LocationViewModel(
    private val locationService: LocationService
) : ViewModel() {

    private val _state = MutableStateFlow(LocationUiState())
    val state: StateFlow<LocationUiState> = _state.asStateFlow()

    private var trackingJob: Job? = null

    fun onEvent(event: LocationEvent) {
        when (event) {
            LocationEvent.StartTracking -> startTracking()
            LocationEvent.StopTracking -> stopTracking()
        }
    }

    private fun startTracking() {
        if (trackingJob?.isActive == true) return

        trackingJob = viewModelScope.launch {
            _state.update { it.copy(isTracking = true, error = null) }

            locationService.observeLocation()
                .catch { e ->
                    _state.update {
                        it.copy(
                            isTracking = false,
                            error = e.message ?: "Unknown location error"
                        )
                    }
                }
                .collect { location ->
                    _state.update {
                        it.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = location.timestamp
                        )
                    }
                }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _state.update { it.copy(isTracking = false) }
    }
}
