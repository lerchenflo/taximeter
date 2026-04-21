package com.lerchenflo.taximeter.passenger.presentation.passenger_list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.core.presentation.ObserveEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PassengerListScreenRoot(
    onPassengerClick: (Long) -> Unit,
    viewModel: PassengerListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is PassengerListEvent.NavigateToPassengerRoutes -> {
                onPassengerClick(event.passengerId)
            }
        }
    }

    PassengerListScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
