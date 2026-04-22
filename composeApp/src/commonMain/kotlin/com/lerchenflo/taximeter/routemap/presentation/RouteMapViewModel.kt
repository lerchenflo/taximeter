package com.lerchenflo.taximeter.routemap.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteMapViewModel(
    savedStateHandle: SavedStateHandle,
    private val passengerRepository: PassengerRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val initialPassengerId: Long = savedStateHandle.get<Long>("passengerId") ?: -1L

    private val _uiState = MutableStateFlow(RouteMapState(selectedPassengerId = initialPassengerId))
    private val _events = Channel<RouteMapEvent>()
    val events = _events.receiveAsFlow()

    val state = combine(
        _uiState,
        passengerRepository.getAllPassengers()
    ) { uiState, passengers ->
        uiState.copy(passengers = passengers)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RouteMapState(selectedPassengerId = initialPassengerId)
    )

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
        }
    }

    private fun loadRoutes(passengerId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val routes: List<Route> = if (passengerId == -1L) {
                routeRepository.getAllRoutes().first()
            } else {
                routeRepository.getRoutesForPassenger(passengerId).first()
            }

            val passengers = passengerRepository.getAllPassengers().first()
            val passengerMap = passengers.associateBy { it.id }

            val polylines = routes.mapIndexed { index, route ->
                async {
                    val points = routeRepository.getRoutePointsOnce(route.id)
                    if (points.size < 2) return@async null
                    RoutePolyline(
                        routeId = route.id,
                        passengerName = passengerMap[route.passengerId]?.name ?: "Unknown",
                        latitudes = points.map { it.latitude },
                        longitudes = points.map { it.longitude },
                        colorIndex = index % PALETTE_SIZE
                    )
                }
            }.awaitAll().filterNotNull()

            _uiState.update { it.copy(isLoading = false, routePolylines = polylines) }
        }
    }

    companion object {
        const val PALETTE_SIZE = 8
    }
}
