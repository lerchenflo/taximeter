package com.lerchenflo.taximeter.taximeter.presentation

sealed interface TaximeterAction {
    data object ToggleRunning : TaximeterAction
    data object StopAndFinish : TaximeterAction
    data class OnPermissionResult(val granted: Boolean) : TaximeterAction
    data object GoBack : TaximeterAction
}
