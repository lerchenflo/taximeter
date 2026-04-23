package com.lerchenflo.taximeter.routemap.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.toComposeColor
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.value.ColorValue
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration.Companion.seconds

private val speedGreen = Color(0xFF4CAF50)
private val speedYellow = Color(0xFFFFC107)
private val speedRed = Color(0xFFF44336)
private val markerGreen = Color(0xFF4CAF50)
private val markerRed = Color(0xFFF44336)

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
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(passenger.color.toComposeColor(), CircleShape)
                                    )
                                    Text(passenger.name)
                                }
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val allPoints = state.routePolylines.flatMap { polyline ->
                    polyline.latitudes.zip(polyline.longitudes)
                }

                val defaultPosition = Position(latitude = 47.417, longitude = 9.738)

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

                            val hasSpeedData = polyline.speeds.any { it > 0f }

                            if (hasSpeedData) {
                                val lineFeature = Feature(
                                    geometry = LineString(positions),
                                    properties = buildJsonObject {}
                                )
                                val lineSource = rememberGeoJsonSource(
                                    data = GeoJsonData.Features(FeatureCollection(lineFeature)),
                                    options = GeoJsonOptions(lineMetrics = true)
                                )
                                val gradientExpr = buildSpeedGradient(
                                    polyline.latitudes, polyline.longitudes, polyline.speeds
                                )
                                LineLayer(
                                    id = "route-${polyline.routeId}",
                                    source = lineSource,
                                    gradient = gradientExpr,
                                    width = const(4.dp),
                                    cap = const(LineCap.Round),
                                    join = const(LineJoin.Round)
                                )
                            } else {
                                val lineFeature = Feature(
                                    geometry = LineString(positions),
                                    properties = buildJsonObject {}
                                )
                                val lineSource = rememberGeoJsonSource(
                                    data = GeoJsonData.Features(FeatureCollection(lineFeature))
                                )
                                LineLayer(
                                    id = "route-${polyline.routeId}",
                                    source = lineSource,
                                    color = const(polyline.color.toComposeColor()),
                                    width = const(4.dp),
                                    cap = const(LineCap.Round),
                                    join = const(LineJoin.Round)
                                )
                            }

                            // Start marker
                            val startFeature = Feature(
                                geometry = Point(
                                    Position(
                                        longitude = polyline.longitudes.first(),
                                        latitude = polyline.latitudes.first()
                                    )
                                ),
                                properties = buildJsonObject {}
                            )
                            val startSource = rememberGeoJsonSource(
                                data = GeoJsonData.Features(FeatureCollection(startFeature))
                            )
                            CircleLayer(
                                id = "start-${polyline.routeId}",
                                source = startSource,
                                color = const(markerGreen),
                                radius = const(6.dp),
                                strokeColor = const(Color.White),
                                strokeWidth = const(2.dp)
                            )

                            // End marker
                            val endFeature = Feature(
                                geometry = Point(
                                    Position(
                                        longitude = polyline.longitudes.last(),
                                        latitude = polyline.latitudes.last()
                                    )
                                ),
                                properties = buildJsonObject {}
                            )
                            val endSource = rememberGeoJsonSource(
                                data = GeoJsonData.Features(FeatureCollection(endFeature))
                            )
                            CircleLayer(
                                id = "end-${polyline.routeId}",
                                source = endSource,
                                color = const(markerRed),
                                radius = const(6.dp),
                                strokeColor = const(Color.White),
                                strokeWidth = const(2.dp)
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

private fun buildSpeedGradient(
    latitudes: List<Double>,
    longitudes: List<Double>,
    speeds: List<Float>
): Expression<ColorValue> {
    if (latitudes.size < 2) return const(speedGreen)

    val distances = mutableListOf(0.0)
    for (i in 1 until latitudes.size) {
        val d = haversineDistance(latitudes[i - 1], longitudes[i - 1], latitudes[i], longitudes[i])
        distances.add(distances.last() + d)
    }
    val totalDist = distances.last()
    if (totalDist <= 0.0) return const(speedGreen)

    val stops = mutableListOf<Pair<Number, Expression<ColorValue>>>()
    for (i in speeds.indices) {
        val progress = (distances[i] / totalDist).toFloat()
        val color = speedToColor(speeds[i])
        if (i == 0 || speedToColor(speeds[i - 1]) != color) {
            stops.add(progress to const(color))
        }
    }

    if (stops.isEmpty()) return const(speedGreen)
    val fallback = stops.first().second
    val remainingStops = if (stops.size > 1) stops.subList(1, stops.size) else emptyList()

    return step(
        feature.lineProgress(),
        fallback,
        *remainingStops.toTypedArray()
    )
}

private fun speedToColor(speedMs: Float): Color {
    val speedKmh = speedMs * 3.6f
    return when {
        speedKmh < 30f -> speedGreen
        speedKmh < 60f -> speedYellow
        else -> speedRed
    }
}
