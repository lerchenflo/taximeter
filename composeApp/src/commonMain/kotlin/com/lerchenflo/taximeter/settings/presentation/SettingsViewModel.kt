package com.lerchenflo.taximeter.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencemanager: Preferencemanager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val baseFare = preferencemanager.getBaseFare()
            val pricePerKm = preferencemanager.getPricePerKm()
            _state.update {
                it.copy(
                    baseFare = formatDouble(baseFare),
                    pricePerKm = formatDouble(pricePerKm)
                )
            }
        }
    }

    private fun formatDouble(value: Double): String {
        val text = value.toString()
        return if (text.endsWith(".0")) text.dropLast(2) else text
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.UpdateBaseFare -> {
                _state.update { it.copy(baseFare = action.value, isSaved = false) }
            }

            is SettingsAction.UpdatePricePerKm -> {
                _state.update { it.copy(pricePerKm = action.value, isSaved = false) }
            }

            is SettingsAction.Save -> {
                val baseFare = _state.value.baseFare.toDoubleOrNull() ?: return
                val pricePerKm = _state.value.pricePerKm.toDoubleOrNull() ?: return
                viewModelScope.launch {
                    preferencemanager.saveBaseFare(baseFare)
                    preferencemanager.savePricePerKm(pricePerKm)
                    _state.update { it.copy(isSaved = true) }
                }
            }

            is SettingsAction.GoBack -> {
                viewModelScope.launch {
                    _events.send(SettingsEvent.NavigateBack)
                }
            }
        }
    }
}
