package com.lerchenflo.taximeter.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.datasource.repository.RouteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val routeRepository: RouteRepository,
    private val passengerRepository: PassengerRepository
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

    fun onAction(action: HomeAction) {
        when (action) {
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
}
