package com.lerchenflo.taximeter.taximeter.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.taximeter.domain.TrackingServiceController
import com.lerchenflo.taximeter.taximeter.domain.TrackingStateHolder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaximeterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val routeRepository: RouteRepository,
    private val trackingServiceController: TrackingServiceController,
    private val trackingStateHolder: TrackingStateHolder,
    private val preferencemanager: Preferencemanager
) : ViewModel() {

    private val passengerId: Long = savedStateHandle.get<Long>("passengerId") ?: -1L
    private val routeId: Long = savedStateHandle.get<Long>("routeId") ?: -1L

    private val _state = MutableStateFlow(TaximeterState())
    val state = combine(
        _state,
        trackingStateHolder.state
    ) { uiState, tracking ->
        if (tracking.isRunning && tracking.routeId == routeId) {
            uiState.copy(
                isRunning = true,
                distanceMeters = tracking.distanceMeters,
                currentPrice = tracking.currentPrice,
                durationSeconds = tracking.durationSeconds
            )
        } else {
            uiState.copy(isRunning = false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TaximeterState()
    )

    private val _events = Channel<TaximeterEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val baseFare = preferencemanager.getBaseFare()
            val pricePerKm = preferencemanager.getPricePerKm()
            _state.update {
                it.copy(
                    baseFare = baseFare,
                    pricePerKm = pricePerKm,
                    currentPrice = baseFare
                )
            }

            if (routeId != -1L) {
                val route = routeRepository.getRouteById(routeId)
                if (route != null && !route.isActive) {
                    _state.update {
                        it.copy(
                            distanceMeters = route.totalDistanceMeters,
                            currentPrice = route.totalPrice,
                            isRouteCompleted = true,
                            durationSeconds = if (route.endTime != null) {
                                (route.endTime - route.startTime) / 1000
                            } else 0L
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: TaximeterAction) {
        when (action) {
            is TaximeterAction.ToggleRunning -> {
                if (_state.value.isRouteCompleted) return
                if (!_state.value.hasLocationPermission) {
                    viewModelScope.launch {
                        _events.send(TaximeterEvent.RequestLocationPermission)
                    }
                    return
                }
                if (trackingStateHolder.state.value.isRunning) {
                    stopTracking()
                } else {
                    startTracking()
                }
            }

            is TaximeterAction.StopAndFinish -> finishRoute()

            is TaximeterAction.OnPermissionResult -> {
                _state.update { it.copy(hasLocationPermission = action.granted) }
                if (action.granted && !trackingStateHolder.state.value.isRunning && !_state.value.isRouteCompleted) {
                    startTracking()
                }
            }

            is TaximeterAction.GoBack -> {
                viewModelScope.launch {
                    _events.send(TaximeterEvent.NavigateBack)
                }
            }
        }
    }

    private fun startTracking() {
        if (routeId == -1L) return
        trackingStateHolder.update { it.copy(isRunning = true, routeId = routeId) }
        trackingServiceController.startTracking(routeId)
    }

    private fun stopTracking() {
        trackingServiceController.stopTracking()
        trackingStateHolder.update { it.copy(isRunning = false) }
    }

    private fun finishRoute() {
        stopTracking()
        viewModelScope.launch {
            if (routeId != -1L) {
                val route = routeRepository.getRouteById(routeId)
                if (route != null) {
                    val tracking = trackingStateHolder.state.value
                    routeRepository.finishRoute(
                        route.copy(
                            totalDistanceMeters = tracking.distanceMeters.takeIf { it > 0 }
                                ?: _state.value.distanceMeters,
                            totalPrice = tracking.currentPrice.takeIf { it > 0 }
                                ?: _state.value.currentPrice
                        )
                    )
                }
            }
            trackingStateHolder.reset()
            _state.update { it.copy(isRouteCompleted = true) }
            _events.send(TaximeterEvent.RouteCompleted)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
