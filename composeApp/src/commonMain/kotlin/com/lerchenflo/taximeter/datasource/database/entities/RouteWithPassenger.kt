package com.lerchenflo.taximeter.datasource.database.entities

import androidx.room.Embedded

data class RouteWithPassenger(
    @Embedded val route: Route,
    val passengerName: String,
    val passengerColor: Long = 0xFFF0A24BL,
)
