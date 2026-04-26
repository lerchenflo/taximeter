package com.lerchenflo.taximeter.taximeter.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.app.theme.Accent
import com.lerchenflo.taximeter.app.theme.AccentDim
import com.lerchenflo.taximeter.app.theme.AccentLine
import com.lerchenflo.taximeter.app.theme.Bg
import com.lerchenflo.taximeter.app.theme.Line
import com.lerchenflo.taximeter.app.theme.Live
import com.lerchenflo.taximeter.app.theme.LiveDim
import com.lerchenflo.taximeter.app.theme.Mono
import com.lerchenflo.taximeter.app.theme.OnAccent
import com.lerchenflo.taximeter.app.theme.Red
import com.lerchenflo.taximeter.app.theme.Surface
import com.lerchenflo.taximeter.app.theme.TextPrimary
import com.lerchenflo.taximeter.app.theme.TextSecondary
import com.lerchenflo.taximeter.app.theme.TextTertiary
import com.lerchenflo.taximeter.routemap.presentation.LiveTaximeterMap
import com.lerchenflo.taximeter.taximeter.domain.GpsError
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.format1f
import com.lerchenflo.taximeter.utilities.formatDuration
import com.lerchenflo.taximeter.utilities.formatPrice
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import taximeter.composeapp.generated.resources.Res
import taximeter.composeapp.generated.resources.taximeter_base_label
import taximeter.composeapp.generated.resources.taximeter_default_ride_name
import taximeter.composeapp.generated.resources.taximeter_done_button
import taximeter.composeapp.generated.resources.taximeter_fare_label
import taximeter.composeapp.generated.resources.taximeter_gps_error_disabled
import taximeter.composeapp.generated.resources.taximeter_gps_error_permission
import taximeter.composeapp.generated.resources.taximeter_gps_status_idle
import taximeter.composeapp.generated.resources.taximeter_gps_status_lock
import taximeter.composeapp.generated.resources.taximeter_gps_status_searching
import taximeter.composeapp.generated.resources.taximeter_paused_label
import taximeter.composeapp.generated.resources.taximeter_recording_label
import taximeter.composeapp.generated.resources.taximeter_searching_label
import taximeter.composeapp.generated.resources.taximeter_start_button_description
import taximeter.composeapp.generated.resources.taximeter_status_active
import taximeter.composeapp.generated.resources.taximeter_status_completed
import taximeter.composeapp.generated.resources.taximeter_status_paused
import taximeter.composeapp.generated.resources.taximeter_tariff_base
import taximeter.composeapp.generated.resources.taximeter_tariff_per_km
import taximeter.composeapp.generated.resources.taximeter_tariff_per_min_idle
import taximeter.composeapp.generated.resources.taximeter_tariff_section
import taximeter.composeapp.generated.resources.taximeter_timezone

@Composable
fun TaximeterRoot(
    onBack: () -> Unit,
    viewModel: TaximeterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionState = rememberLocationPermissionState()

    var gpsErrorMessage by remember { mutableStateOf<String?>(null) }
    val gpsErrorDisabled = stringResource(Res.string.taximeter_gps_error_disabled)
    val gpsErrorPermission = stringResource(Res.string.taximeter_gps_error_permission)

    LaunchedEffect(permissionState.hasPermission) {
        viewModel.onAction(TaximeterAction.OnPermissionResult(permissionState.hasPermission))
    }

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is TaximeterEvent.RouteCompleted -> onBack()
            is TaximeterEvent.NavigateBack -> onBack()
            is TaximeterEvent.RequestLocationPermission -> permissionState.requestPermission()
            is TaximeterEvent.GpsErrorOccurred -> {
                gpsErrorMessage = when (event.error) {
                    GpsError.NoProvider -> "⚠ $gpsErrorDisabled"
                    GpsError.PermissionRevoked -> "⚠ $gpsErrorPermission"
                }
            }
        }
    }

    TaximeterScreen(
        state = state,
        gpsErrorMessage = gpsErrorMessage,
        onDismissError = { gpsErrorMessage = null },
        onAction = viewModel::onAction,
    )
}

@Composable
fun TaximeterScreen(
    state: TaximeterState,
    gpsErrorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onAction: (TaximeterAction) -> Unit,
) {
    val price = state.currentPrice
    val priceStr = price.formatPrice()
    val parts = priceStr.split(".")
    val intPart = parts.getOrElse(0) { "0" }
    val decPart = parts.getOrElse(1) { "00" }

    val distKm = state.distanceMeters / 1000.0
    val avgKmh = if (state.durationSeconds > 0)
        distKm / (state.durationSeconds / 3600.0)
    else 0.0

    val searchingPulse by rememberInfiniteTransition(label = "gps_pulse").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gps_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Error banner (tap to dismiss)
        if (gpsErrorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Red.copy(alpha = 0.12f))
                    .border(1.dp, Red.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                    .clickable { onDismissError() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = gpsErrorMessage,
                    fontFamily = Mono,
                    fontSize = 11.sp,
                    color = Red,
                    letterSpacing = 0.4.sp
                )
            }
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAction(TaximeterAction.GoBack) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.isRunning) stringResource(Res.string.taximeter_status_active) else if (state.isRouteCompleted) stringResource(Res.string.taximeter_status_completed) else stringResource(Res.string.taximeter_status_paused),
                    fontFamily = Mono,
                    fontSize = 10.sp,
                    color = TextTertiary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = state.passengerName.ifBlank { stringResource(Res.string.taximeter_default_ride_name) },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            // GPS pill
            when {
                state.gpsFixFresh -> GpsPill(
                    label = stringResource(Res.string.taximeter_gps_status_lock),
                    textColor = Live,
                    bgColor = LiveDim,
                    borderColor = Color(0x4D7AD4A5),
                )
                state.gpsSearching -> GpsPill(
                    label = stringResource(Res.string.taximeter_gps_status_searching),
                    textColor = Accent.copy(alpha = searchingPulse),
                    bgColor = AccentDim,
                    borderColor = AccentLine.copy(alpha = 0.4f),
                )
                else -> GpsPill(
                    label = stringResource(Res.string.taximeter_gps_status_idle),
                    textColor = TextTertiary,
                    bgColor = Color(0x0AFFFFFF),
                    borderColor = Line,
                )
            }
        }

        // Fare card
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, Line, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            listOf(AccentDim, Color.Transparent),
                            radius = 600f
                        )
                    )
            )
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(Res.string.taximeter_fare_label), fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.4.sp)
                    when {
                        state.gpsFixFresh -> Text(
                            text = stringResource(Res.string.taximeter_recording_label),
                            fontFamily = Mono,
                            fontSize = 10.sp,
                            color = Live,
                            letterSpacing = 1.4.sp
                        )
                        state.gpsSearching -> Text(
                            text = stringResource(Res.string.taximeter_searching_label),
                            fontFamily = Mono,
                            fontSize = 10.sp,
                            color = Accent.copy(alpha = searchingPulse),
                            letterSpacing = 1.4.sp
                        )
                        else -> Text(
                            text = stringResource(Res.string.taximeter_paused_label),
                            fontFamily = Mono,
                            fontSize = 10.sp,
                            color = TextTertiary,
                            letterSpacing = 1.4.sp
                        )
                    }
                }
                // Big price number
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = intPart,
                        fontFamily = Mono,
                        fontSize = 92.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        letterSpacing = (-3).sp,
                        lineHeight = 88.sp
                    )
                    Text(
                        text = ".",
                        fontFamily = Mono,
                        fontSize = 92.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary,
                        letterSpacing = (-3).sp,
                        lineHeight = 88.sp
                    )
                    Text(
                        text = decPart,
                        fontFamily = Mono,
                        fontSize = 92.sp,
                        fontWeight = FontWeight.Medium,
                        color = Accent,
                        letterSpacing = (-3).sp,
                        lineHeight = 88.sp
                    )
                }
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 4.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Line)
                ) {
                    val progress = (distKm / 25.0).coerceIn(0.0, 1.0).toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(Accent)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(Res.string.taximeter_base_label)}${state.baseFare.formatPrice()}",
                        fontFamily = Mono,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "+ ${(price - state.baseFare).coerceAtLeast(0.0).formatPrice()} · ${distKm.format1f()}km",
                        fontFamily = Mono,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Stats grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard("Distance", distKm.format1f(), "km", Modifier.weight(1f))
            StatCard("Duration", state.durationSeconds.formatDuration(), "min", Modifier.weight(1f))
            StatCard("Avg", avgKmh.format1f(), "km/h", Modifier.weight(1f))
        }

        // Tariff card
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.dp, Line, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(Res.string.taximeter_tariff_section), fontFamily = Mono, fontSize = 11.sp, color = TextTertiary, letterSpacing = 1.2.sp)
                    Text(stringResource(Res.string.taximeter_timezone), fontFamily = Mono, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    TariffItem(stringResource(Res.string.taximeter_tariff_base), state.baseFare.formatPrice())
                    TariffItem(stringResource(Res.string.taximeter_tariff_per_km), state.pricePerKm.formatPrice())
                    TariffItem(stringResource(Res.string.taximeter_tariff_per_min_idle), "0.35")
                }
            }
        }

        if (state.liveMapState != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
            ) {
                LiveTaximeterMap(
                    state = state.liveMapState,
                    modifier = Modifier.fillMaxSize(),
                    followVehicle = true
                )
            }
            Spacer(Modifier.height(12.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }

        // Bottom controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!state.isRouteCompleted) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(if (state.isRunning) Red else Accent)
                        .clickable {
                            if (state.isRunning) onAction(TaximeterAction.StopAndFinish)
                            else onAction(TaximeterAction.ToggleRunning)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isRunning) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(OnAccent)
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.taximeter_start_button_description), tint = OnAccent, modifier = Modifier.size(28.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .border(1.dp, Line, CircleShape)
                        .clickable { onAction(TaximeterAction.GoBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.taximeter_done_button), color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun GpsPill(
    label: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = Mono,
            fontSize = 10.sp,
            color = textColor,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    val valueFontSize = when {
        value.length > 6 -> 15.sp
        value.length > 4 -> 18.sp
        else -> 22.sp
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label.uppercase(), fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.2.sp)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(value, fontFamily = Mono, fontSize = valueFontSize, fontWeight = FontWeight.Medium, color = TextPrimary, letterSpacing = (-0.8).sp, maxLines = 1)
            Text(unit, fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
    }
}

@Composable
private fun TariffItem(label: String, value: String) {
    Column {
        Text(label.uppercase(), fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 0.8.sp)
        Text("€ $value", fontFamily = Mono, fontSize = 15.sp, color = TextPrimary, letterSpacing = (-0.2).sp, modifier = Modifier.padding(top = 2.dp))
    }
}
