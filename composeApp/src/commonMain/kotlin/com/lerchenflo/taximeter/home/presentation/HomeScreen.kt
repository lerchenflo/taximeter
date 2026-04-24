package com.lerchenflo.taximeter.home.presentation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.lerchenflo.taximeter.app.theme.Mono
import com.lerchenflo.taximeter.app.theme.OnAccent
import com.lerchenflo.taximeter.app.theme.Surface
import com.lerchenflo.taximeter.app.theme.TextPrimary
import com.lerchenflo.taximeter.app.theme.TextSecondary
import com.lerchenflo.taximeter.app.theme.TextTertiary
import com.lerchenflo.taximeter.datasource.database.entities.RouteWithPassenger
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.currentTimeMillis
import com.lerchenflo.taximeter.utilities.format1f
import com.lerchenflo.taximeter.utilities.formatDateTime
import com.lerchenflo.taximeter.utilities.formatPrice
import com.lerchenflo.taximeter.utilities.toComposeColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import taximeter.composeapp.generated.resources.Res
import taximeter.composeapp.generated.resources.*

@Composable
fun HomeRoot(
    onOpenCustomerPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowMap: () -> Unit,
    onRouteClick: (Long, Long) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is HomeEvent.NavigateToCustomerPicker -> onOpenCustomerPicker()
            is HomeEvent.NavigateToSettings -> onOpenSettings()
            is HomeEvent.NavigateToRouteMap -> onShowMap()
            is HomeEvent.NavigateToTaximeter -> onRouteClick(event.passengerId, event.routeId)
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    val todayCutoff = currentTimeMillis() - 24 * 60 * 60 * 1000L
    val todayRoutes = state.recentRoutes.filter { it.route.startTime >= todayCutoff }
    val totalEarnings = todayRoutes.sumOf { it.route.totalPrice }
    val totalDistKm = todayRoutes.sumOf { it.route.totalDistanceMeters } / 1000.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 14.dp, top = 18.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LogoMark()
                        Column {
                            Text(
                                text = stringResource(Res.string.home_app_title),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                letterSpacing = (-0.2).sp
                            )
                            Text(
                                text = stringResource(Res.string.home_month_year),
                                fontFamily = Mono,
                                fontSize = 11.sp,
                                color = TextTertiary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Row {
                        IconButton(onClick = { onAction(HomeAction.ShowAllRoutesMap) }) {
                            Icon(Icons.Default.Map, contentDescription = "Map", tint = TextSecondary)
                        }
                        IconButton(onClick = { onAction(HomeAction.OpenSettings) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                        }
                    }
                }
            }

            item {
                TodaySummaryCard(
                    rideCount = todayRoutes.size,
                    totalEarnings = totalEarnings,
                    totalDistKm = totalDistKm,
                    modifier = Modifier.padding(horizontal = 22.dp).padding(bottom = 18.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.home_recent_rides_header),
                        fontFamily = Mono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "${state.recentRoutes.size}",
                        fontFamily = Mono,
                        fontSize = 11.sp,
                        color = TextTertiary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            items(state.recentRoutes, key = { it.route.id }) { rp ->
                RecentRouteItem(
                    routeWithPassenger = rp,
                    onClick = { onAction(HomeAction.SelectRecentRoute(rp.route.id)) }
                )
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Accent)
                .clickable { onAction(HomeAction.OpenCustomerPicker) }
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = OnAccent, modifier = Modifier.size(18.dp))
                Text(text = stringResource(Res.string.home_new_ride_button), color = OnAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Accent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⊟",
            fontFamily = Mono,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = OnAccent
        )
    }
}

@Composable
private fun TodaySummaryCard(
    rideCount: Int,
    totalEarnings: Double,
    totalDistKm: Double,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Surface, Color(0xFF1C1916)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(140.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentDim, Color.Transparent)
                    )
                )
        )
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Today · $rideCount rides",
                fontFamily = Mono,
                fontSize = 10.sp,
                color = TextTertiary,
                letterSpacing = 1.4.sp
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = totalEarnings.formatPrice(),
                    fontFamily = Mono,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 44.sp
                )
                Text(
                    text = "EUR",
                    fontFamily = Mono,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Row {
                    Text("DIST ", fontFamily = Mono, fontSize = 12.sp, color = TextTertiary)
                    Text(
                        text = "${totalDistKm.format1f()} km",
                        fontFamily = Mono,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRouteItem(
    routeWithPassenger: RouteWithPassenger,
    onClick: () -> Unit
) {
    val route = routeWithPassenger.route
    val passengerColor = routeWithPassenger.passengerColor.toComposeColor()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(passengerColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = routeWithPassenger.passengerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                if (route.name.isNotBlank()) {
                    Text(
                        text = route.name,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 3.dp),
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "—",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Text(
                    text = "${route.startTime.formatDateTime()} · ${(route.totalDistanceMeters / 1000.0).format1f()} km",
                    fontFamily = Mono,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = route.totalPrice.formatPrice(),
                    fontFamily = Mono,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "EUR",
                    fontFamily = Mono,
                    fontSize = 10.sp,
                    color = TextTertiary,
                    letterSpacing = 0.5.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(start = 22.dp, end = 22.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
    }
}

