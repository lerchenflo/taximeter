package com.lerchenflo.taximeter.taximeter.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.core.presentation.ObserveEvents
import com.lerchenflo.taximeter.core.presentation.rememberLocationPermissionState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaximeterScreenRoot(
    passengerId: Long,
    routeId: Long,
    onBack: () -> Unit,
    viewModel: TaximeterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionState = rememberLocationPermissionState()

    LaunchedEffect(permissionState.hasPermission) {
        viewModel.onAction(TaximeterAction.OnPermissionResult(permissionState.hasPermission))
    }

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is TaximeterEvent.RouteCompleted -> onBack()
            is TaximeterEvent.NavigateBack -> onBack()
            is TaximeterEvent.RequestLocationPermission -> {
                permissionState.requestPermission()
            }
        }
    }

    TaximeterScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
