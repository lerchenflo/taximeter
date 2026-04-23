package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

sealed interface PassengerRoutesAction {
    data object ShowStartRideDialog : PassengerRoutesAction
    data object DismissStartRideDialog : PassengerRoutesAction
    data class UpdateRouteName(val name: String) : PassengerRoutesAction
    data object ConfirmStartRoute : PassengerRoutesAction
    data class SelectRoute(val routeId: Long) : PassengerRoutesAction
    data class DeleteRoute(val routeId: Long) : PassengerRoutesAction
    data object GoBack : PassengerRoutesAction
    data object ShowRouteMap : PassengerRoutesAction
}
