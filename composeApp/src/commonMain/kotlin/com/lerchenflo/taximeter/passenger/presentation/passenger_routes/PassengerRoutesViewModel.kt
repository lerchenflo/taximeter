package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PassengerRoutesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val passengerRepository: PassengerRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val passengerId: Long = savedStateHandle.get<Long>("passengerId") ?: -1L

    private val _uiState = MutableStateFlow(PassengerRoutesState())
    private val _events = Channel<PassengerRoutesEvent>()
    val events = _events.receiveAsFlow()

    val state = combine(
        _uiState,
        routeRepository.getRoutesForPassenger(passengerId)
    ) { uiState, routes ->
        uiState.copy(routes = routes, isLoading = false)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PassengerRoutesState()
    )

    init {
        viewModelScope.launch {
            val passenger = passengerRepository.getPassengerById(passengerId)
            _uiState.update { it.copy(passenger = passenger) }
        }
    }

    fun onAction(action: PassengerRoutesAction) {
        when (action) {
            is PassengerRoutesAction.StartNewRoute -> {
                viewModelScope.launch {
                    val routeId = routeRepository.startRoute(passengerId)
                    _events.send(PassengerRoutesEvent.NavigateToTaximeter(passengerId, routeId))
                }
            }

            is PassengerRoutesAction.SelectRoute -> {
                viewModelScope.launch {
                    _events.send(PassengerRoutesEvent.NavigateToTaximeter(passengerId, action.routeId))
                }
            }

            is PassengerRoutesAction.GoBack -> {
                viewModelScope.launch {
                    _events.send(PassengerRoutesEvent.NavigateBack)
                }
            }
        }
    }
}
