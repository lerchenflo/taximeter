package com.lerchenflo.taximeter.routemap.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lerchenflo.taximeter.taximeter.domain.bearingDegrees
import com.lerchenflo.taximeter.taximeter.domain.haversineDistance
import com.lerchenflo.taximeter.utilities.formatTimeOfDay
import com.lerchenflo.taximeter.utilities.toComposeColor
import kotlinx.serialization.json.buildJsonObject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.value.ColorValue
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.TextRotationAlignment
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
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
import org.maplibre.compose.util.ClickResult
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

internal val speedGreen = Color(0xFF4CAF50)
internal val speedYellowGreen = Color(0xFFC0CA33)
internal val speedYellow = Color(0xFFFFC107)
internal val speedOrange = Color(0xFFFF7043)
internal val speedRed = Color(0xFFD32F2F)
internal val markerGreen = Color(0xFF4CAF50)
internal val markerRed = Color(0xFFF44336)

private val legendGradientColors = listOf(speedGreen, speedYellowGreen, speedYellow, speedOrange, speedRed)

@Composable
fun LiveTaximeterMap(
    state: RouteMapState,
    modifier: Modifier = Modifier,
    followVehicle: Boolean = false,
    onAction: (RouteMapAction) -> Unit = {}
) {
    val allPoints = state.routePolylines.flatMap { polyline ->
        polyline.latitudes.zip(polyline.longitudes)
    }

    val defaultPosition = Position(latitude = 47.417, longitude = 9.738)
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = defaultPosition, zoom = 12.0)
    )

    val livePolyline = state.routePolylines.firstOrNull { it.isLive }
    var lastClickData by remember { mutableStateOf<Pair<Position, DpOffset>?>(null) }

    LaunchedEffect(state.routePolylines) {
        if (livePolyline != null) {
            val lastLat = livePolyline.latitudes.last()
            val lastLng = livePolyline.longitudes.last()
            cameraState.animateTo(
                CameraPosition(
                    target = Position(longitude = lastLng, latitude = lastLat),
                    zoom = 16.0
                ),
                duration = 1.seconds
            )
        } else if (allPoints.isNotEmpty()) {
            val minLat = allPoints.minOf { it.first }
            val maxLat = allPoints.maxOf { it.first }
            val minLng = allPoints.minOf { it.second }
            val maxLng = allPoints.maxOf { it.second }
            val bounds = BoundingBox(
                southwest = Position(longitude = minLng, latitude = minLat),
                northeast = Position(longitude = maxLng, latitude = maxLat)
            )
            cameraState.animateTo(boundingBox = bounds, duration = 1.seconds)
        }
    }

    Box(modifier = modifier) {
        MaplibreMap(
            cameraState = cameraState,
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            modifier = Modifier.fillMaxSize(),
            onMapClick = { position, dpOffset ->
                lastClickData = Pair(position, dpOffset)
                onAction(RouteMapAction.DismissTooltip)
                ClickResult.Pass
            }
        ) {
            livePolyline?.let { live ->
                if (live.latitudes.size >= 2) {
                    val lastIdx = live.latitudes.lastIndex
                    val vehicleLat = live.latitudes[lastIdx]
                    val vehicleLon = live.longitudes[lastIdx]
                    val bearing = bearingDegrees(
                        live.latitudes[lastIdx - 1], live.longitudes[lastIdx - 1],
                        vehicleLat, vehicleLon
                    )
                    val vehicleSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            FeatureCollection(
                                Feature(
                                    geometry = Point(Position(longitude = vehicleLon, latitude = vehicleLat)),
                                    properties = buildJsonObject {}
                                )
                            )
                        )
                    )
                    SymbolLayer(
                        id = "live-vehicle",
                        source = vehicleSource,
                        textField = const(state.vehicleType.emoji()),
                        textSize = const(28.sp),
                        textRotate = const((bearing - 90f + 360f) % 360f),
                        textRotationAlignment = const(TextRotationAlignment.Map),
                        textIgnorePlacement = const(true),
                        textAllowOverlap = const(true)
                    )
                }
            }

            state.routePolylines.forEach { polyline ->
                key(polyline.routeId) {
                    val positions = polyline.latitudes.zip(polyline.longitudes).map { (lat, lng) ->
                        Position(longitude = lng, latitude = lat)
                    }
                    val hasSpeedData = polyline.speeds.any { it > 0f }

                    if (hasSpeedData) {
                        val lineFeature = Feature(geometry = LineString(positions), properties = buildJsonObject {})
                        val lineSource = rememberGeoJsonSource(
                            data = GeoJsonData.Features(FeatureCollection(lineFeature)),
                            options = GeoJsonOptions(lineMetrics = true)
                        )
                        val gradientExpr = buildSpeedGradient(polyline.latitudes, polyline.longitudes, polyline.speeds)
                        LineLayer(
                            id = "route-${polyline.routeId}",
                            source = lineSource,
                            gradient = gradientExpr,
                            width = const(4.dp),
                            cap = const(LineCap.Round),
                            join = const(LineJoin.Round),
                            onClick = { _ ->
                                lastClickData?.let { (pos, offset) ->
                                    onAction(RouteMapAction.LineClicked(polyline.routeId, pos, offset))
                                }
                                ClickResult.Consume
                            }
                        )
                    } else {
                        val lineFeature = Feature(geometry = LineString(positions), properties = buildJsonObject {})
                        val lineSource = rememberGeoJsonSource(
                            data = GeoJsonData.Features(FeatureCollection(lineFeature))
                        )
                        LineLayer(
                            id = "route-${polyline.routeId}",
                            source = lineSource,
                            color = const(polyline.color.toComposeColor()),
                            width = const(4.dp),
                            cap = const(LineCap.Round),
                            join = const(LineJoin.Round),
                            onClick = { _ ->
                                lastClickData?.let { (pos, offset) ->
                                    onAction(RouteMapAction.LineClicked(polyline.routeId, pos, offset))
                                }
                                ClickResult.Consume
                            }
                        )
                    }

                    val startSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            FeatureCollection(
                                Feature(
                                    geometry = Point(Position(longitude = polyline.longitudes.first(), latitude = polyline.latitudes.first())),
                                    properties = buildJsonObject {}
                                )
                            )
                        )
                    )
                    CircleLayer(id = "start-${polyline.routeId}", source = startSource, color = const(markerGreen), radius = const(6.dp), strokeColor = const(Color.White), strokeWidth = const(2.dp))

                    val endSource = rememberGeoJsonSource(
                        data = GeoJsonData.Features(
                            FeatureCollection(
                                Feature(
                                    geometry = Point(Position(longitude = polyline.longitudes.last(), latitude = polyline.latitudes.last())),
                                    properties = buildJsonObject {}
                                )
                            )
                        )
                    )
                    CircleLayer(id = "end-${polyline.routeId}", source = endSource, color = const(markerRed), radius = const(6.dp), strokeColor = const(Color.White), strokeWidth = const(2.dp))
                }
            }
        }

        SpeedLegend(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))

        state.tooltip?.let { tooltip ->
            Box(
                modifier = Modifier
                    .absoluteOffset(
                        x = (tooltip.screenOffsetDp.x - 60.dp).coerceAtLeast(8.dp),
                        y = (tooltip.screenOffsetDp.y - 90.dp).coerceAtLeast(8.dp)
                    )
                    .width(130.dp)
                    .background(Color(0xEE17191B), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "${tooltip.speedKmh.roundToInt()} km/h",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = speedToColor(tooltip.speedKmh / 3.6f)
                    )
                    Text(
                        text = tooltip.timestamp.formatTimeOfDay(),
                        fontSize = 11.sp,
                        color = Color(0xFF9C9891),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

internal fun buildSpeedGradient(
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

internal fun speedToColor(speedMs: Float): Color {
    val kmh = speedMs * 3.6f
    return when {
        kmh < 15f -> speedGreen
        kmh < 30f -> speedYellowGreen
        kmh < 45f -> speedYellow
        kmh < 60f -> speedOrange
        else      -> speedRed
    }
}

@Composable
private fun SpeedLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xCC17191B), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("SPEED  km/h", fontSize = 9.sp, color = Color(0xFF9C9891), fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp)
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.horizontalGradient(legendGradientColors))
        )
        Row(
            modifier = Modifier.width(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0", "30", "60", "90+").forEach { label ->
                Text(label, fontSize = 8.sp, color = Color(0xFF9C9891), fontFamily = FontFamily.Monospace)
            }
        }
    }
}
