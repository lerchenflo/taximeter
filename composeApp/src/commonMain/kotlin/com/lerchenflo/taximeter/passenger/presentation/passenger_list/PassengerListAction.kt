package com.lerchenflo.taximeter.passenger.presentation.passenger_list

sealed interface PassengerListAction {
    data object ToggleAddDialog : PassengerListAction
    data class UpdateNewPassengerName(val name: String) : PassengerListAction
    data object AddPassenger : PassengerListAction
    data class DeletePassenger(val id: Long) : PassengerListAction
    data class SelectPassenger(val id: Long) : PassengerListAction
}
