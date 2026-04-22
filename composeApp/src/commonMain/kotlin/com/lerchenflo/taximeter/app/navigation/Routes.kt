package com.lerchenflo.taximeter.app.navigation

import kotlinx.serialization.Serializable

@Serializable
object PassengerListRoute

@Serializable
data class PassengerRoutesRoute(val passengerId: Long)

@Serializable
data class TaximeterRoute(val passengerId: Long, val routeId: Long = -1L)

@Serializable
data class RouteMapRoute(val passengerId: Long = -1L)

@Serializable
object SettingsRoute
