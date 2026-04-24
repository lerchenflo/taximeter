package com.lerchenflo.taximeter.passenger.presentation.passenger_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PassengerListViewModel(
    private val passengerRepository: PassengerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PassengerListState())
    private val _events = Channel<PassengerListEvent>()
    val events = _events.receiveAsFlow()

    val state = combine(
        _uiState,
        passengerRepository.getAllPassengers()
    ) { uiState, passengers ->
        uiState.copy(passengers = passengers)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PassengerListState()
    )

    fun onAction(action: PassengerListAction) {
        when (action) {
            is PassengerListAction.ToggleAddDialog -> {
                _uiState.value = _uiState.value.copy(
                    isAddDialogVisible = !_uiState.value.isAddDialogVisible,
                    newPassengerName = ""
                )
            }

            is PassengerListAction.UpdateNewPassengerName -> {
                _uiState.value = _uiState.value.copy(newPassengerName = action.name)
            }

            is PassengerListAction.UpdateNewPassengerColor -> {
                _uiState.value = _uiState.value.copy(newPassengerColor = action.color)
            }

            is PassengerListAction.AddPassenger -> {
                val name = _uiState.value.newPassengerName.trim()
                if (name.isBlank()) return
                val color = _uiState.value.newPassengerColor
                viewModelScope.launch {
                    passengerRepository.addPassenger(name, color)
                    _uiState.value = _uiState.value.copy(
                        isAddDialogVisible = false,
                        newPassengerName = "",
                        newPassengerColor = 0xFFF0A24BL,
                    )
                }
            }

            is PassengerListAction.DeletePassenger -> {
                viewModelScope.launch {
                    passengerRepository.deletePassenger(action.id)
                }
            }

            is PassengerListAction.SelectPassenger -> {
                viewModelScope.launch {
                    _events.send(PassengerListEvent.NavigateToPassengerRoutes(action.id))
                }
            }

            is PassengerListAction.GoBack -> {
                viewModelScope.launch {
                    _events.send(PassengerListEvent.NavigateBack)
                }
            }
        }
    }
}
