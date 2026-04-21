package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.core.presentation.ObserveEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PassengerRoutesScreenRoot(
    passengerId: Long,
    onStartRoute: (Long) -> Unit,
    onRouteClick: (Long, Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PassengerRoutesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is PassengerRoutesEvent.NavigateToTaximeter -> {
                if (event.routeId == -1L) {
                    onStartRoute(event.passengerId)
                } else {
                    onRouteClick(event.passengerId, event.routeId)
                }
            }
            is PassengerRoutesEvent.NavigateBack -> onBack()
        }
    }

    PassengerRoutesScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
