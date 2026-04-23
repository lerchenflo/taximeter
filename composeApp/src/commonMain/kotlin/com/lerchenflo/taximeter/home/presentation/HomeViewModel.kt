package com.lerchenflo.taximeter.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.taximeter.domain.LocationTracker
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import com.lerchenflo.taximeter.utilities.currentTimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val routeRepository: RouteRepository,
    private val passengerRepository: PassengerRepository,
    private val locationTracker: LocationTracker,
    private val preferencemanager: Preferencemanager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    val state = combine(
        _uiState,
        routeRepository.getAllRoutesWithPassenger()
    ) { uiState, recentRoutes ->
        uiState.copy(recentRoutes = recentRoutes, isLoading = false)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeState()
    )

    private var locationJob: Job? = null
    private var timerJob: Job? = null
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var startTimeMillis: Long = 0L

    init {
        viewModelScope.launch {
            val baseFare = preferencemanager.getBaseFare()
            val pricePerKm = preferencemanager.getPricePerKm()
            _uiState.update {
                it.copy(baseFare = baseFare, pricePerKm = pricePerKm, currentPrice = baseFare)
            }

            val activeRoute = routeRepository.getActiveRoute()
            if (activeRoute != null) {
                val passenger = passengerRepository.getPassengerById(activeRoute.passengerId)
                val price = baseFare + (activeRoute.totalDistanceMeters / 1000.0) * pricePerKm
                _uiState.update {
                    it.copy(
                        activeRoute = activeRoute,
                        activePassenger = passenger,
                        distanceMeters = activeRoute.totalDistanceMeters,
                        currentPrice = price
                    )
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ToggleRunning -> {
                val current = _uiState.value
                if (current.activeRoute == null) return
                if (!current.hasLocationPermission) {
                    viewModelScope.launch {
                        _events.send(HomeEvent.RequestLocationPermission)
                    }
                    return
                }
                if (current.isRunning) {
                    pauseTracking()
                } else {
                    startTracking()
                }
            }

            is HomeAction.StopAndFinish -> finishRoute()

            is HomeAction.OnPermissionResult -> {
                _uiState.update { it.copy(hasLocationPermission = action.granted) }
                if (action.granted && !_uiState.value.isRunning && _uiState.value.activeRoute != null) {
                    startTracking()
                }
            }

            is HomeAction.OpenCustomerPicker -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToCustomerPicker)
                }
            }

            is HomeAction.OpenSettings -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToSettings)
                }
            }

            is HomeAction.ShowAllRoutesMap -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToRouteMap)
                }
            }

            is HomeAction.SelectRecentRoute -> {
                viewModelScope.launch {
                    val route = routeRepository.getRouteById(action.routeId)
                    if (route != null) {
                        _events.send(HomeEvent.NavigateToTaximeter(route.passengerId, route.id))
                    }
                }
            }
        }
    }

    fun refreshActiveRoute() {
        viewModelScope.launch {
            val baseFare = preferencemanager.getBaseFare()
            val pricePerKm = preferencemanager.getPricePerKm()
            _uiState.update {
                it.copy(baseFare = baseFare, pricePerKm = pricePerKm)
            }

            val activeRoute = routeRepository.getActiveRoute()
            if (activeRoute != null) {
                val passenger = passengerRepository.getPassengerById(activeRoute.passengerId)
                val price = baseFare + (activeRoute.totalDistanceMeters / 1000.0) * pricePerKm
                _uiState.update {
                    it.copy(
                        activeRoute = activeRoute,
                        activePassenger = passenger,
                        distanceMeters = activeRoute.totalDistanceMeters,
                        currentPrice = price
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        activeRoute = null,
                        activePassenger = null,
                        isRunning = false,
                        distanceMeters = 0.0,
                        currentPrice = baseFare,
                        durationSeconds = 0L
                    )
                }
            }
        }
    }

    private fun startTracking() {
        val routeId = _uiState.value.activeRoute?.id ?: return
        _uiState.update { it.copy(isRunning = true) }
        startTimeMillis = currentTimeMillis()

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val elapsed = (currentTimeMillis() - startTimeMillis) / 1000
                _uiState.update { it.copy(durationSeconds = elapsed) }
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
                            val newTotalDistance = _uiState.value.distanceMeters + distance
                            val newPrice =
                                _uiState.value.baseFare + (newTotalDistance / 1000.0) * _uiState.value.pricePerKm

                            _uiState.update {
                                it.copy(
                                    distanceMeters = newTotalDistance,
                                    currentPrice = newPrice
                                )
                            }

                            routeRepository.addRoutePoint(routeId, point.latitude, point.longitude)
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
                    } else {
                        routeRepository.addRoutePoint(routeId, point.latitude, point.longitude)
                    }

                    lastLat = point.latitude
                    lastLon = point.longitude
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false) }
                timerJob?.cancel()
            }
        }
    }

    private fun pauseTracking() {
        locationJob?.cancel()
        timerJob?.cancel()
        locationTracker.stopTracking()
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun finishRoute() {
        pauseTracking()
        viewModelScope.launch {
            val route = _uiState.value.activeRoute ?: return@launch
            val currentRoute = routeRepository.getRouteById(route.id)
            if (currentRoute != null) {
                routeRepository.finishRoute(
                    currentRoute.copy(
                        totalDistanceMeters = _uiState.value.distanceMeters,
                        totalPrice = _uiState.value.currentPrice
                    )
                )
            }
            val baseFare = _uiState.value.baseFare
            _uiState.update {
                it.copy(
                    activeRoute = null,
                    activePassenger = null,
                    isRunning = false,
                    distanceMeters = 0.0,
                    currentPrice = baseFare,
                    durationSeconds = 0L
                )
            }
            lastLat = null
            lastLon = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        timerJob?.cancel()
        locationTracker.stopTracking()
    }
}
