package com.lerchenflo.taximeter.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lerchenflo.taximeter.datasource.preferences.Preferencemanager
import com.lerchenflo.taximeter.datasource.repository.PassengerRepository
import com.lerchenflo.taximeter.settings.domain.SpeedScale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencemanager: Preferencemanager,
    private val passengerRepository: PassengerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val baseFare = preferencemanager.getBaseFare()
            val pricePerKm = preferencemanager.getPricePerKm()
            val idleRate = preferencemanager.getIdleRate()
            val vehicleType = preferencemanager.getVehicleType()
            val speedScale = preferencemanager.getSpeedScale()
            val gpsIntervalMs = preferencemanager.getGpsIntervalMs()
            val gpsMinDistanceM = preferencemanager.getGpsMinDistanceM()
            _state.update {
                it.copy(
                    baseFare = formatDouble(baseFare),
                    pricePerKm = formatDouble(pricePerKm),
                    idleRate = formatDouble(idleRate),
                    vehicleType = vehicleType,
                    speedScale = speedScale,
                    gpsIntervalMs = gpsIntervalMs.toString(),
                    gpsMinDistanceM = gpsMinDistanceM.toInt().toString()
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

            is SettingsAction.UpdateIdleRate -> {
                _state.update { it.copy(idleRate = action.value, isSaved = false) }
            }

            is SettingsAction.UpdateGpsInterval -> {
                _state.update { it.copy(gpsIntervalMs = action.value, isSaved = false) }
            }

            is SettingsAction.UpdateGpsMinDistance -> {
                _state.update { it.copy(gpsMinDistanceM = action.value, isSaved = false) }
            }

            is SettingsAction.Save -> {
                val baseFare = _state.value.baseFare.toDoubleOrNull() ?: return
                val pricePerKm = _state.value.pricePerKm.toDoubleOrNull() ?: return
                val idleRate = _state.value.idleRate.toDoubleOrNull() ?: return
                val intervalMs = (_state.value.gpsIntervalMs.toLongOrNull() ?: Preferencemanager.DEFAULT_GPS_INTERVAL_MS).coerceAtLeast(500L)
                val minDistM = (_state.value.gpsMinDistanceM.toFloatOrNull() ?: Preferencemanager.DEFAULT_GPS_MIN_DISTANCE_M).coerceAtLeast(0f)
                viewModelScope.launch {
                    preferencemanager.saveBaseFare(baseFare)
                    preferencemanager.savePricePerKm(pricePerKm)
                    preferencemanager.saveIdleRate(idleRate)
                    preferencemanager.saveGpsIntervalMs(intervalMs)
                    preferencemanager.saveGpsMinDistanceM(minDistM)
                    _state.update {
                        it.copy(
                            isSaved = true,
                            gpsIntervalMs = intervalMs.toString(),
                            gpsMinDistanceM = minDistM.toInt().toString()
                        )
                    }
                }
            }

            is SettingsAction.GoBack -> {
                viewModelScope.launch {
                    _events.send(SettingsEvent.NavigateBack)
                }
            }

            is SettingsAction.UpdateVehicleType -> {
                viewModelScope.launch {
                    preferencemanager.saveVehicleType(action.type)
                    _state.update { it.copy(vehicleType = action.type) }
                }
            }

            is SettingsAction.UpdateSpeedScale -> {
                viewModelScope.launch {
                    preferencemanager.saveSpeedScale(action.scale)
                    _state.update { it.copy(speedScale = action.scale) }
                }
            }

            is SettingsAction.ShowClearConfirmation -> {
                _state.update { it.copy(isShowingClearConfirmDialog = true) }
            }

            is SettingsAction.DismissClearConfirmation -> {
                _state.update { it.copy(isShowingClearConfirmDialog = false) }
            }

            is SettingsAction.ConfirmClearData -> {
                viewModelScope.launch {
                    _state.update { it.copy(isClearing = true, isShowingClearConfirmDialog = false) }
                    passengerRepository.clearAllData()
                    preferencemanager.clearAll()
                    _state.update {
                        it.copy(
                            isClearing = false,
                            baseFare = formatDouble(Preferencemanager.DEFAULT_BASE_FARE),
                            pricePerKm = formatDouble(Preferencemanager.DEFAULT_PRICE_PER_KM),
                            idleRate = formatDouble(Preferencemanager.DEFAULT_IDLE_RATE),
                            gpsIntervalMs = Preferencemanager.DEFAULT_GPS_INTERVAL_MS.toString(),
                            gpsMinDistanceM = Preferencemanager.DEFAULT_GPS_MIN_DISTANCE_M.toInt().toString(),
                            isSaved = false
                        )
                    }
                }
            }
        }
    }
}
