package com.lerchenflo.taximeter.routemap.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.datasource.database.entities.RoutePoint
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteMapViewModel(
    savedStateHandle: SavedStateHandle,
    private val passengerRepository: PassengerRepository,
    private val routeRepository: RouteRepository,
    private val preferencemanager: Preferencemanager
) : ViewModel() {

    private val initialPassengerId: Long = savedStateHandle.get<Long>("passengerId") ?: -1L

    private val _uiState = MutableStateFlow(RouteMapState(selectedPassengerId = initialPassengerId))
    private val _events = Channel<RouteMapEvent>()
    val events = _events.receiveAsFlow()

    val state = combine(
        _uiState,
        passengerRepository.getAllPassengers(),
        preferencemanager.getVehicleTypeFlow()
    ) { uiState, passengers, vehicleType ->
        uiState.copy(passengers = passengers, vehicleType = vehicleType)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RouteMapState(selectedPassengerId = initialPassengerId)
    )

    private var loadRoutesJob: Job? = null

    init {
        loadRoutes(initialPassengerId)
    }

    fun onAction(action: RouteMapAction) {
        when (action) {
            is RouteMapAction.SelectPassenger -> {
                _uiState.update { it.copy(selectedPassengerId = action.passengerId) }
                loadRoutes(action.passengerId)
            }
            is RouteMapAction.GoBack -> {
                viewModelScope.launch {
                    _events.send(RouteMapEvent.NavigateBack)
                }
            }

            is RouteMapAction.DismissTooltip -> {
                _uiState.update { it.copy(tooltip = null) }
            }

            is RouteMapAction.LineClicked -> {
                val polyline = _uiState.value.routePolylines.find { it.routeId == action.routeId }
                    ?: return
                val nearestIdx = polyline.latitudes.indices.minByOrNull { i ->
                    haversineDistance(
                        polyline.latitudes[i], polyline.longitudes[i],
                        action.position.latitude, action.position.longitude
                    )
                } ?: return
                val speedMs = polyline.speeds.getOrNull(nearestIdx) ?: 0f
                val timestamp = polyline.timestamps.getOrNull(nearestIdx) ?: 0L
                _uiState.update {
                    it.copy(
                        tooltip = TooltipState(
                            screenOffsetDp = action.screenOffset,
                            speedKmh = (speedMs * 3.6f * 10).roundToInt() / 10f,
                            timestamp = timestamp
                        )
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadRoutes(passengerId: Long) {
        loadRoutesJob?.cancel()
        loadRoutesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val completedRoutesFlow = if (passengerId == -1L) {
                routeRepository.getAllRoutes()
            } else {
                routeRepository.getRoutesForPassenger(passengerId)
            }

            val activeRouteFlow = routeRepository.getActiveRouteFlow()
            val activeRoutePointsFlow = activeRouteFlow.flatMapLatest { active ->
                if (active == null) flowOf(emptyList()) else routeRepository.getRoutePoints(active.id)
            }

            combine(completedRoutesFlow, activeRouteFlow, activeRoutePointsFlow) { completed, active, livePoints ->
                Triple(completed.filter { !it.isActive }, active, livePoints)
            }.collectLatest { (completedRoutes, activeRoute, livePoints) ->
                val passengers = passengerRepository.getAllPassengers().first()
                val passengerMap = passengers.associateBy { it.id }

                val completedPolylines = completedRoutes.map { route ->
                    async {
                        val points = routeRepository.getRoutePointsOnce(route.id)
                        if (points.size < 2) return@async null
                        buildPolyline(route, points, passengerMap, isLive = false)
                    }
                }.awaitAll().filterNotNull()

                val livePoly = if (activeRoute != null && livePoints.size >= 2) {
                    buildPolyline(activeRoute, livePoints, passengerMap, isLive = true)
                } else null

                _uiState.update {
                    it.copy(isLoading = false, routePolylines = completedPolylines + listOfNotNull(livePoly))
                }
            }
        }
    }

    private fun buildPolyline(
        route: Route,
        points: List<RoutePoint>,
        passengerMap: Map<Long, com.lerchenflo.taximeter.datasource.database.entities.Passenger>,
        isLive: Boolean
    ): RoutePolyline {
        val passengerColor = passengerMap[route.passengerId]?.color ?: 0xFFE53935L
        val speeds = computeSpeeds(points)
        return RoutePolyline(
            routeId = route.id,
            passengerName = passengerMap[route.passengerId]?.name ?: "Unknown",
            latitudes = points.map { it.latitude },
            longitudes = points.map { it.longitude },
            timestamps = points.map { it.timestamp },
            color = passengerColor,
            speeds = speeds,
            isLive = isLive
        )
    }

    private fun computeSpeeds(points: List<RoutePoint>): List<Float> {
        if (points.isEmpty()) return emptyList()
        val hasStoredSpeed = points.any { it.speed > 0f }
        if (hasStoredSpeed) return points.map { it.speed }

        val speeds = mutableListOf(0f)
        for (i in 1 until points.size) {
            val dist = haversineDistance(
                points[i - 1].latitude, points[i - 1].longitude,
                points[i].latitude, points[i].longitude
            )
            val timeDelta = (points[i].timestamp - points[i - 1].timestamp) / 1000.0
            speeds.add(if (timeDelta > 0) (dist / timeDelta).toFloat() else 0f)
        }
        return speeds
    }
}
