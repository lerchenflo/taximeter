package com.lerchenflo.taximeter.passenger.presentation.passenger_routes

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.app.theme.Accent
import com.lerchenflo.taximeter.app.theme.AccentDim
import com.lerchenflo.taximeter.app.theme.AccentLine
import com.lerchenflo.taximeter.app.theme.Bg
import com.lerchenflo.taximeter.app.theme.Line
import com.lerchenflo.taximeter.app.theme.LineHi
import com.lerchenflo.taximeter.app.theme.Live
import com.lerchenflo.taximeter.app.theme.LiveDim
import com.lerchenflo.taximeter.app.theme.Mono
import com.lerchenflo.taximeter.app.theme.OnAccent
import com.lerchenflo.taximeter.app.theme.Surface
import com.lerchenflo.taximeter.app.theme.TextPrimary
import com.lerchenflo.taximeter.app.theme.TextSecondary
import com.lerchenflo.taximeter.app.theme.TextTertiary
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.datasource.database.entities.Route
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.formatDateTime
import com.lerchenflo.taximeter.utilities.formatPrice
import com.lerchenflo.taximeter.utilities.toComposeColor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PassengerRoutesRoot(
    passengerId: Long,
    onRouteClick: (Long, Long) -> Unit,
    onShowMap: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PassengerRoutesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is PassengerRoutesEvent.NavigateToTaximeter -> onRouteClick(event.passengerId, event.routeId)
            is PassengerRoutesEvent.NavigateBack -> onBack()
            is PassengerRoutesEvent.NavigateToRouteMap -> onShowMap(event.passengerId)
        }
    }

    PassengerRoutesScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun PassengerRoutesScreen(
    state: PassengerRoutesState,
    onAction: (PassengerRoutesAction) -> Unit
) {
    val passenger = state.passenger
    val totalDist = state.routes.sumOf { it.totalDistanceMeters } / 1000.0
    val totalPrice = state.routes.sumOf { it.totalPrice }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAction(PassengerRoutesAction.GoBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAction(PassengerRoutesAction.ShowRouteMap) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Map", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            // Passenger header
            if (passenger != null) {
                PassengerHeader(passenger = passenger, routeCount = state.routes.size)
                StatsStrip(
                    rides = state.routes.size,
                    distKm = totalDist,
                    totalEur = totalPrice,
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
            }

            // Section label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RIDE HISTORY", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary, letterSpacing = 1.4.sp)
                Text("ALL TIME", fontFamily = Mono, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
            }

            // Route list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (state.routes.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No rides yet. Tap Start ride to begin.", color = TextTertiary, fontSize = 13.sp)
                        }
                    }
                }
                items(state.routes, key = { it.id }) { route ->
                    RouteItem(
                        route = route,
                        onClick = { onAction(PassengerRoutesAction.SelectRoute(route.id)) }
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Accent)
                .clickable { onAction(PassengerRoutesAction.ShowStartRideDialog) }
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OnAccent, modifier = Modifier.size(16.dp))
                Text("Start ride", color = OnAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        // Start ride dialog
        if (state.isStartRideDialogVisible) {
            StartRideSheet(
                passenger = passenger,
                name = state.newRouteName,
                onNameChange = { onAction(PassengerRoutesAction.UpdateRouteName(it)) },
                onConfirm = { onAction(PassengerRoutesAction.ConfirmStartRoute) },
                onDismiss = { onAction(PassengerRoutesAction.DismissStartRideDialog) }
            )
        }
    }
}

@Composable
private fun PassengerHeader(passenger: Passenger, routeCount: Int) {
    val color = passenger.color.toComposeColor()
    Row(
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = passenger.name.firstOrNull()?.uppercase() ?: "?",
                fontFamily = Mono,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnAccent
            )
        }
        Column {
            Text(
                text = passenger.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                letterSpacing = (-0.4).sp
            )
            Text(
                text = "PASSENGER · $routeCount RIDES TOTAL",
                fontFamily = Mono,
                fontSize = 11.sp,
                color = TextTertiary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun StatsStrip(rides: Int, distKm: Double, totalEur: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
    ) {
        listOf(
            "RIDES" to rides.toString(),
            "DIST" to "${"%.1f".format(distKm)} km",
            "TOTAL" to "€ ${"%.0f".format(totalEur)}"
        ).forEachIndexed { i, (label, value) ->
            if (i > 0) {
                Box(modifier = Modifier.width(1.dp).height(52.dp).background(Line))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(label, fontFamily = Mono, fontSize = 9.sp, color = TextTertiary, letterSpacing = 1.4.sp)
                Text(
                    value,
                    fontFamily = Mono,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun RouteItem(route: Route, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (route.isActive) AccentDim else Color(0x0AFFFFFF))
                    .border(1.dp, if (route.isActive) AccentLine else Line, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = if (route.isActive) Accent else TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = route.name.ifBlank { "Untitled ride" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (route.isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(LiveDim)
                                .border(1.dp, Color(0x4D7AD4A5), RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("● LIVE", fontFamily = Mono, fontSize = 10.sp, color = Live, letterSpacing = 0.8.sp)
                        }
                    }
                }
                Text(
                    text = "${route.startTime.formatDateTime()} · ${"%.1f".format(route.totalDistanceMeters / 1000.0)} km",
                    fontFamily = Mono,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = route.totalPrice.formatPrice(),
                    fontFamily = Mono,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (route.isActive) Accent else TextPrimary,
                    letterSpacing = (-0.2).sp
                )
                Text("EUR", fontFamily = Mono, fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.8.sp)
            }
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
    }
}

@Composable
private fun StartRideSheet(
    passenger: Passenger?,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBF0A0B0C))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, LineHi, RoundedCornerShape(20.dp))
                .clickable(enabled = false, onClick = {})
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentDim)
                        .border(1.dp, AccentLine, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("START RIDE", fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.2.sp)
                    Text(passenger?.name ?: "", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
            }
            Text("ROUTE NAME — OPTIONAL", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary, letterSpacing = 1.sp)
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bg)
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                decorationBox = { inner ->
                    if (name.isEmpty()) {
                        Text("e.g. Airport transfer", color = TextTertiary, fontSize = 14.sp)
                    }
                    inner()
                }
            )
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = TextSecondary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Accent)
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OnAccent, modifier = Modifier.size(14.dp))
                        Text("Start meter", color = OnAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
