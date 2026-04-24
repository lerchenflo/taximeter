package com.lerchenflo.taximeter.taximeter.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.formatDuration
import com.lerchenflo.taximeter.utilities.formatPrice
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaximeterRoot(
    passengerId: Long,
    routeId: Long,
    onBack: () -> Unit,
    viewModel: TaximeterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionState = rememberLocationPermissionState()
    val notifPermissionState = rememberNotificationPermissionState()

    LaunchedEffect(permissionState.hasPermission) {
        viewModel.onAction(TaximeterAction.OnPermissionResult(permissionState.hasPermission))
    }

    LaunchedEffect(Unit) {
        if (!notifPermissionState.hasPermission) {
            notifPermissionState.requestPermission()
        }
    }

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is TaximeterEvent.RouteCompleted -> onBack()
            is TaximeterEvent.NavigateBack -> onBack()
            is TaximeterEvent.RequestLocationPermission -> permissionState.requestPermission()
        }
    }

    TaximeterScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun TaximeterScreen(
    state: TaximeterState,
    onAction: (TaximeterAction) -> Unit
) {
    val price = if (state.isRouteCompleted) state.currentPrice else state.currentPrice
    val priceStr = price.formatPrice()
    val parts = priceStr.split(".")
    val intPart = parts.getOrElse(0) { "0" }
    val decPart = parts.getOrElse(1) { "00" }

    val distKm = state.distanceMeters / 1000.0
    val avgKmh = if (state.durationSeconds > 0)
        distKm / (state.durationSeconds / 3600.0)
    else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
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
                    text = if (state.isRunning) "ACTIVE RIDE" else if (state.isRouteCompleted) "COMPLETED" else "PAUSED",
                    fontFamily = Mono,
                    fontSize = 10.sp,
                    color = TextTertiary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = state.passengerName.ifBlank { "Ride" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            // GPS pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (state.isRunning) LiveDim else Color(0x0AFFFFFF))
                    .border(1.dp, if (state.isRunning) Color(0x4D7AD4A5) else Line, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (state.isRunning) "● GPS LOCK" else "○ IDLE",
                    fontFamily = Mono,
                    fontSize = 10.sp,
                    color = if (state.isRunning) Live else TextTertiary,
                    letterSpacing = 0.8.sp
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
                    Text("FARE · EUR", fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.4.sp)
                    Text(
                        text = if (state.isRunning) "● RECORDING" else "○ PAUSED",
                        fontFamily = Mono,
                        fontSize = 10.sp,
                        color = if (state.isRunning) Live else TextTertiary,
                        letterSpacing = 1.4.sp
                    )
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
                        text = "BASE ${state.baseFare.formatPrice()}",
                        fontFamily = Mono,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "+ ${(price - state.baseFare).coerceAtLeast(0.0).formatPrice()} · ${"%.1f".format(distKm)}km",
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
            StatCard("Distance", "${"%.1f".format(distKm)}", "km", Modifier.weight(1f))
            StatCard("Duration", state.durationSeconds.formatDuration(), "min", Modifier.weight(1f))
            StatCard("Avg", "${"%.1f".format(avgKmh)}", "km/h", Modifier.weight(1f))
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
                    Text("TARIFF A · DAY", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary, letterSpacing = 1.2.sp)
                    Text("CEST", fontFamily = Mono, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    TariffItem("Base", state.baseFare.formatPrice())
                    TariffItem("Per km", state.pricePerKm.formatPrice())
                    TariffItem("Per min idle", "0.35")
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live map button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .clickable { /* navigate to map */ },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Text("Live map", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Play/Stop circular button
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
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = OnAccent, modifier = Modifier.size(28.dp))
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
                    Text("Done", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Note button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Line, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Note", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
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
            Text(value, fontFamily = Mono, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = TextPrimary, letterSpacing = (-0.8).sp)
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
