package com.lerchenflo.taximeter.passenger.presentation.passenger_list

import com.lerchenflo.taximeter.datasource.database.entities.Passenger

data class PassengerListState(
    val passengers: List<Passenger> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val newPassengerName: String = "",
    val newPassengerColor: Long = 0xFFF0A24BL,
)
