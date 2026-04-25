package com.lerchenflo.taximeter.taximeter.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.database.entities.RoutePoint
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.routemap.presentation.RouteMapState
import com.lerchenflo.taximeter.routemap.presentation.RoutePolyline
import com.lerchenflo.taximeter.taximeter.domain.TrackingServiceController
import com.lerchenflo.taximeter.taximeter.domain.TrackingStateHolder
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import com.lerchenflo.taximeter.utilities.currentTimeMillis
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaximeterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val routeRepository: RouteRepository,
    private val passengerRepository: PassengerRepository,
    private val trackingServiceController: TrackingServiceController,
    private val trackingStateHolder: TrackingStateHolder,
    private val preferencemanager: Preferencemanager,
) : ViewModel() {

    private val passengerId: Long = savedStateHandle.get<Long>("passengerId") ?: -1L
    private val routeId: Long = savedStateHandle.get<Long>("routeId") ?: -1L

    private val _state = MutableStateFlow(TaximeterState())

    private val ticker = flow { while (true) { emit(Unit); delay(1000) } }

    val state = combine(
        _state,
        trackingStateHolder.state,
        ticker
    ) { uiState, tracking, _ ->
        if (tracking.gpsError != null) {
            viewModelScope.launch {
                _events.send(TaximeterEvent.GpsErrorOccurred(tracking.gpsError))
                trackingStateHolder.update { it.copy(gpsError = null) }
            }
        }

        if (tracking.isRunning && tracking.routeId == routeId) {
            val nowMillis = currentTimeMillis()
            val gpsFixFresh = tracking.hasEverHadFix &&
                (nowMillis - (tracking.lastFixTimestampMillis ?: 0L)) < 10_000L
            val gpsSearching = !tracking.hasEverHadFix

            uiState.copy(
                isRunning = true,
                distanceMeters = tracking.distanceMeters,
                currentPrice = tracking.currentPrice,
                durationSeconds = tracking.durationSeconds,
                gpsFixFresh = gpsFixFresh,
                gpsSearching = gpsSearching,
            )
        } else {
            uiState.copy(isRunning = false, gpsFixFresh = false, gpsSearching = false)
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
                it.copy(baseFare = baseFare, pricePerKm = pricePerKm, currentPrice = baseFare)
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

            val passengerName = passengerRepository.getPassengerById(passengerId)?.name ?: ""
            _state.update { it.copy(passengerName = passengerName) }
        }

        if (routeId != -1L) {
            viewModelScope.launch {
                val vehicleType = preferencemanager.getVehicleType()
                val passenger = passengerRepository.getPassengerById(passengerId)
                val passengerColor = passenger?.color ?: 0xFFE53935L

                combine(
                    routeRepository.getRoutePoints(routeId),
                    trackingStateHolder.state
                ) { points, tracking -> Pair(points, tracking) }
                    .collect { (points, tracking) ->
                        if (tracking.isRunning && tracking.routeId == routeId && points.size >= 2) {
                            val speeds = computeSpeedsForLiveMap(points)
                            val polyline = RoutePolyline(
                                routeId = routeId,
                                passengerName = passenger?.name ?: "",
                                latitudes = points.map { it.latitude },
                                longitudes = points.map { it.longitude },
                                timestamps = points.map { it.timestamp },
                                color = passengerColor,
                                speeds = speeds,
                                isLive = true
                            )
                            _state.update {
                                it.copy(
                                    liveMapState = RouteMapState(
                                        isLoading = false,
                                        routePolylines = listOf(polyline),
                                        vehicleType = vehicleType
                                    )
                                )
                            }
                        } else {
                            _state.update { it.copy(liveMapState = null) }
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
                    _state.update { it.copy(pendingStart = true) }
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
                if (action.granted && _state.value.pendingStart && !trackingStateHolder.state.value.isRunning) {
                    _state.update { it.copy(pendingStart = false) }
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
        trackingServiceController.startTracking(routeId)
    }

    private fun stopTracking() {
        trackingServiceController.stopTracking()
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

    private fun computeSpeedsForLiveMap(points: List<RoutePoint>): List<Float> {
        if (points.isEmpty()) return emptyList()
        val hasStoredSpeed = points.any { it.speed > 0f }
        if (hasStoredSpeed) return points.map { it.speed }
        val speeds = mutableListOf(0f)
        for (i in 1 until points.size) {
            val dist = haversineDistance(points[i - 1].latitude, points[i - 1].longitude, points[i].latitude, points[i].longitude)
            val timeDelta = (points[i].timestamp - points[i - 1].timestamp) / 1000.0
            speeds.add(if (timeDelta > 0) (dist / timeDelta).toFloat() else 0f)
        }
        return speeds
    }

    override fun onCleared() {
        super.onCleared()
    }
}
