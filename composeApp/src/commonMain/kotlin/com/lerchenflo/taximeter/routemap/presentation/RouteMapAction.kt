package com.lerchenflo.taximeter.routemap.presentation

import androidx.compose.ui.unit.DpOffset
import org.maplibre.spatialk.geojson.Position

sealed interface RouteMapAction {
    data class SelectPassenger(val passengerId: Long) : RouteMapAction
    data object GoBack : RouteMapAction
    data class LineClicked(val routeId: Long, val position: Position, val screenOffset: DpOffset) : RouteMapAction
    data object DismissTooltip : RouteMapAction
}
