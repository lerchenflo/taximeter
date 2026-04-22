package com.lerchenflo.taximeter.routemap.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.utilities.ObserveEvents
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.seconds

private val routeColorPalette = listOf(
    Color(0xFFE53935),
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFFDD835),
    Color(0xFF8E24AA),
    Color(0xFFFF6F00),
    Color(0xFF00ACC1),
    Color(0xFFD81B60),
)

@Composable
fun RouteMapRoot(
    passengerId: Long,
    onBack: () -> Unit,
    viewModel: RouteMapViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is RouteMapEvent.NavigateBack -> onBack()
        }
    }

    RouteMapScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    state: RouteMapState,
    onAction: (RouteMapAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Map") },
                navigationIcon = {
                    IconButton(onClick = { onAction(RouteMapAction.GoBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.passengers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedPassengerId == -1L,
                        onClick = { onAction(RouteMapAction.SelectPassenger(-1L)) },
                        label = { Text("All") }
                    )
                    state.passengers.forEach { passenger ->
                        FilterChip(
                            selected = state.selectedPassengerId == passenger.id,
                            onClick = { onAction(RouteMapAction.SelectPassenger(passenger.id)) },
                            label = { Text(passenger.name) }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val allPoints = state.routePolylines.flatMap { polyline ->
                    polyline.latitudes.zip(polyline.longitudes)
                }

                val defaultPosition = Position(longitude = 11.576, latitude = 48.137)

                val cameraState = rememberCameraState(
                    firstPosition = CameraPosition(target = defaultPosition, zoom = 12.0)
                )

                LaunchedEffect(state.routePolylines) {
                    if (allPoints.isNotEmpty()) {
                        val minLat = allPoints.minOf { it.first }
                        val maxLat = allPoints.maxOf { it.first }
                        val minLng = allPoints.minOf { it.second }
                        val maxLng = allPoints.maxOf { it.second }
                        val bounds = BoundingBox(
                            southwest = Position(longitude = minLng, latitude = minLat),
                            northeast = Position(longitude = maxLng, latitude = maxLat)
                        )
                        cameraState.animateTo(
                            boundingBox = bounds,
                            duration = 1.seconds
                        )
                    }
                }

                MaplibreMap(
                    cameraState = cameraState,
                    baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                    modifier = Modifier.fillMaxSize()
                ) {
                    state.routePolylines.forEach { polyline ->
                        key(polyline.routeId) {
                            val positions = polyline.latitudes.zip(polyline.longitudes).map { (lat, lng) ->
                                Position(longitude = lng, latitude = lat)
                            }
                            val feature = Feature(
                                geometry = LineString(positions),
                                properties = buildJsonObject {}
                            )
                            val source = rememberGeoJsonSource(
                                data = GeoJsonData.Features(FeatureCollection(feature))
                            )
                            LineLayer(
                                id = "route-${polyline.routeId}",
                                source = source,
                                color = const(routeColorPalette[polyline.colorIndex]),
                                width = const(4.dp),
                                cap = const(LineCap.Round),
                                join = const(LineJoin.Round)
                            )
                        }
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
