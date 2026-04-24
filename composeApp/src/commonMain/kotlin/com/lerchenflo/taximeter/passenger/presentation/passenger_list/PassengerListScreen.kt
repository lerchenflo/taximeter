package com.lerchenflo.taximeter.passenger.presentation.passenger_list

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.lerchenflo.taximeter.app.theme.Mono
import com.lerchenflo.taximeter.app.theme.OnAccent
import com.lerchenflo.taximeter.app.theme.Surface
import com.lerchenflo.taximeter.app.theme.TextPrimary
import com.lerchenflo.taximeter.app.theme.TextSecondary
import com.lerchenflo.taximeter.app.theme.TextTertiary
import com.lerchenflo.taximeter.datasource.database.entities.Passenger
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.toComposeColor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import taximeter.composeapp.generated.resources.Res
import taximeter.composeapp.generated.resources.*

private val swatchColors = listOf(
    0xFFF0A24BL, 0xFF7AD4A5L, 0xFF8BB8F0L, 0xFFE77271L, 0xFFC79BE0L
)

@Composable
fun PassengerListRoot(
    onPassengerClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PassengerListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is PassengerListEvent.NavigateToPassengerRoutes -> onPassengerClick(event.passengerId)
            is PassengerListEvent.NavigateBack -> onBack()
        }
    }

    PassengerListScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun PassengerListScreen(
    state: PassengerListState,
    onAction: (PassengerListAction) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = state.passengers.filter {
        it.name.contains(query, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAction(PassengerListAction.GoBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(Res.string.passenger_list_step_indicator),
                        fontFamily = Mono,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = stringResource(Res.string.passenger_list_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                }
            }

            // Search field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface)
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(stringResource(Res.string.passenger_list_search_placeholder, state.passengers.size), color = TextTertiary, fontSize = 14.sp)
                        }
                        inner()
                    }
                )
            }

            // Result count
            Text(
                text = stringResource(Res.string.passenger_list_result_count, filtered.size, if (filtered.size == 1) "" else "s"),
                fontFamily = Mono,
                fontSize = 10.sp,
                color = TextTertiary,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(start = 18.dp, bottom = 6.dp)
            )

            // List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { passenger ->
                    PassengerItem(
                        passenger = passenger,
                        onClick = { onAction(PassengerListAction.SelectPassenger(passenger.id)) }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(Res.string.passenger_list_no_results, query),
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 44.dp)
                .size(56.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Accent)
                .clickable { onAction(PassengerListAction.ToggleAddDialog) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.passenger_list_add_button_description), tint = OnAccent, modifier = Modifier.size(22.dp))
        }

        // Add passenger sheet
        if (state.isAddDialogVisible) {
            AddPassengerSheet(
                name = state.newPassengerName,
                selectedColor = state.newPassengerColor,
                onNameChange = { onAction(PassengerListAction.UpdateNewPassengerName(it)) },
                onColorSelect = { onAction(PassengerListAction.UpdateNewPassengerColor(it)) },
                onConfirm = { onAction(PassengerListAction.AddPassenger) },
                onDismiss = { onAction(PassengerListAction.ToggleAddDialog) }
            )
        }
    }
}

@Composable
private fun PassengerItem(
    passenger: Passenger,
    onClick: () -> Unit
) {
    val color = passenger.color.toComposeColor()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = passenger.name.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = Mono,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnAccent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = passenger.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "last seen recently",
                    fontFamily = Mono,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 18.dp, end = 18.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
    }
}

@Composable
private fun AddPassengerSheet(
    name: String,
    selectedColor: Long,
    onNameChange: (String) -> Unit,
    onColorSelect: (Long) -> Unit,
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
            Text(
                text = stringResource(Res.string.passenger_list_sheet_header),
                fontFamily = Mono,
                fontSize = 10.sp,
                color = TextTertiary,
                letterSpacing = 1.2.sp
            )
            Text(
                text = stringResource(Res.string.passenger_list_sheet_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bg)
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                decorationBox = { inner ->
                    if (name.isEmpty()) {
                        Text(stringResource(Res.string.passenger_list_name_placeholder), color = TextTertiary, fontSize = 14.sp)
                    }
                    inner()
                }
            )
            // Color swatches
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                swatchColors.forEach { c ->
                    val isSelected = c == selectedColor
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(c.toInt()))
                            .then(
                                if (isSelected) Modifier.border(2.dp, TextPrimary, RoundedCornerShape(10.dp))
                                else Modifier.border(2.dp, Color.Transparent, RoundedCornerShape(10.dp))
                            )
                            .clickable { onColorSelect(c) }
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 18.dp),
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
                    Text(stringResource(Res.string.passenger_list_cancel_button), color = TextSecondary, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (name.isNotBlank()) Accent else AccentDim)
                        .border(1.dp, if (name.isNotBlank()) AccentLine else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable(enabled = name.isNotBlank(), onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(Res.string.passenger_list_add_button),
                        color = if (name.isNotBlank()) OnAccent else Accent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
