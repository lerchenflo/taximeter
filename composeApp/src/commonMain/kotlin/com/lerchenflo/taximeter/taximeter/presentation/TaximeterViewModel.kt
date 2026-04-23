package com.lerchenflo.taximeter.taximeter.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import com.lerchenflo.taximeter.utilities.currentTimeMillis
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaximeterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val routeRepository: RouteRepository,
    private val locationTracker: LocationTracker,
    private val preferencemanager: Preferencemanager
) : ViewModel() {

    private val passengerId: Long = savedStateHandle.get<Long>("passengerId") ?: -1L
    private val routeId: Long = savedStateHandle.get<Long>("routeId") ?: -1L

    private val _state = MutableStateFlow(TaximeterState())
    val state = _state.asStateFlow()

    private val _events = Channel<TaximeterEvent>()
    val events = _events.receiveAsFlow()

    private var locationJob: Job? = null
    private var timerJob: Job? = null
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var startTimeMillis: Long = 0L
    private var pendingStart: Boolean = false

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
                    pendingStart = true
                    viewModelScope.launch {
                        _events.send(TaximeterEvent.RequestLocationPermission)
                    }
                    return
                }
                if (_state.value.isRunning) {
                    pauseTracking()
                } else {
                    startTracking()
                }
            }

            is TaximeterAction.StopAndFinish -> {
                finishRoute()
            }

            is TaximeterAction.OnPermissionResult -> {
                _state.update { it.copy(hasLocationPermission = action.granted) }
                if (action.granted && pendingStart && !_state.value.isRunning && !_state.value.isRouteCompleted) {
                    pendingStart = false
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
        _state.update { it.copy(isRunning = true) }
        startTimeMillis = currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val elapsed = (currentTimeMillis() - startTimeMillis) / 1000
                _state.update { it.copy(durationSeconds = elapsed) }
            }
        }

        locationJob = viewModelScope.launch {
            try {
                locationTracker.startTracking().collect { point ->
                    val prevLat = lastLat
                    val prevLon = lastLon

                    if (prevLat != null && prevLon != null) {
                        val distance =
                            haversineDistance(prevLat, prevLon, point.latitude, point.longitude)
                        if (distance > 2.0) {
                            val newTotalDistance = _state.value.distanceMeters + distance
                            val newPrice =
                                _state.value.baseFare + (newTotalDistance / 1000.0) * _state.value.pricePerKm

                            _state.update {
                                it.copy(
                                    distanceMeters = newTotalDistance,
                                    currentPrice = newPrice
                                )
                            }

                            if (routeId != -1L) {
                                routeRepository.addRoutePoint(
                                    routeId,
                                    point.latitude,
                                    point.longitude
                                )
                                val route = routeRepository.getRouteById(routeId)
                                if (route != null) {
                                    routeRepository.updateRoute(
                                        route.copy(
                                            totalDistanceMeters = newTotalDistance,
                                            totalPrice = newPrice
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        if (routeId != -1L) {
                            routeRepository.addRoutePoint(
                                routeId,
                                point.latitude,
                                point.longitude
                            )
                        }
                    }

                    lastLat = point.latitude
                    lastLon = point.longitude
                }
            } catch (e: Exception) {
                _state.update { it.copy(isRunning = false) }
                timerJob?.cancel()
            }
        }
    }

    private fun pauseTracking() {
        locationJob?.cancel()
        timerJob?.cancel()
        locationTracker.stopTracking()
        _state.update { it.copy(isRunning = false) }
    }

    private fun finishRoute() {
        pauseTracking()
        viewModelScope.launch {
            if (routeId != -1L) {
                val route = routeRepository.getRouteById(routeId)
                if (route != null) {
                    routeRepository.finishRoute(
                        route.copy(
                            totalDistanceMeters = _state.value.distanceMeters,
                            totalPrice = _state.value.currentPrice
                        )
                    )
                }
            }
            _state.update { it.copy(isRouteCompleted = true) }
            _events.send(TaximeterEvent.RouteCompleted)
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        timerJob?.cancel()
        locationTracker.stopTracking()
    }
}
