package com.lerchenflo.taximeter.settings.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.taximeter.app.theme.Accent
import com.lerchenflo.taximeter.app.theme.AccentLine
import com.lerchenflo.taximeter.app.theme.Bg
import com.lerchenflo.taximeter.app.theme.Line
import com.lerchenflo.taximeter.app.theme.Live
import com.lerchenflo.taximeter.app.theme.Mono

import com.lerchenflo.taximeter.app.theme.OnAccent
import com.lerchenflo.taximeter.app.theme.Surface
import com.lerchenflo.taximeter.app.theme.TextPrimary
import com.lerchenflo.taximeter.app.theme.TextSecondary
import com.lerchenflo.taximeter.app.theme.TextTertiary
import com.lerchenflo.taximeter.utilities.ObserveEvents
import com.lerchenflo.taximeter.utilities.format2f
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoot(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.NavigateBack -> onBack()
        }
    }

    SettingsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit
) {
    val baseFare = state.baseFare.toDoubleOrNull() ?: 3.50
    val perKm = state.pricePerKm.toDoubleOrNull() ?: 1.80
    val idleRate = state.idleRate.toDoubleOrNull() ?: 0.35
    val farePreview = baseFare + 10.0 * perKm + 15.0 * idleRate
    val dirty = !state.isSaved

    val saveBg by animateColorAsState(
        targetValue = when {
            state.isSaved -> Live
            dirty -> Accent
            else -> Color(0x14FFFFFF)
        },
        animationSpec = tween(200),
        label = "saveBg"
    )
    val saveText by animateColorAsState(
        targetValue = when {
            state.isSaved || dirty -> OnAccent
            else -> TextTertiary
        },
        animationSpec = tween(200),
        label = "saveText"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAction(SettingsAction.GoBack) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Column {
                Text("PREFERENCES", fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.2.sp)
                Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, letterSpacing = (-0.3).sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionLabel("Tariff")
            SectionCard {
                StepperRow(
                    label = "Base fare",
                    sub = "Charged immediately",
                    unit = "€",
                    value = baseFare,
                    step = 0.10,
                    onChange = { onAction(SettingsAction.UpdateBaseFare(it.toString())) }
                )
                CardDivider()
                StepperRow(
                    label = "Price per km",
                    sub = "After first 200 m",
                    unit = "€/km",
                    value = perKm,
                    step = 0.05,
                    onChange = { onAction(SettingsAction.UpdatePricePerKm(it.toString())) }
                )
                CardDivider()
                StepperRow(
                    label = "Idle rate",
                    sub = "Charged when stopped",
                    unit = "€/min",
                    value = idleRate,
                    step = 0.05,
                    onChange = { onAction(SettingsAction.UpdateIdleRate(it.toString())) }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Fare preview")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .border(1.dp, Line, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text("A 10 KM · 15 MIN RIDE", fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.4.sp)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = farePreview.format2f(),
                            fontFamily = Mono,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 44.sp
                        )
                        Text("EUR", fontFamily = Mono, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Text("${baseFare.format2f()} base", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary)
                        Text("+ ${(10.0 * perKm).format2f()} dist", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary)
                        Text("+ ${(15.0 * idleRate).format2f()} idle", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Taximeter · v1.3.0", fontFamily = Mono, fontSize = 11.sp, color = TextTertiary, letterSpacing = 1.4.sp)
                Text("local-first · no account", fontFamily = Mono, fontSize = 10.sp, color = TextTertiary, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(100.dp))
        }

        // Sticky save button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(saveBg)
                .then(
                    if (dirty && !state.isSaved)
                        Modifier.border(1.dp, AccentLine, RoundedCornerShape(14.dp))
                    else Modifier
                )
                .clickable(enabled = dirty) { onAction(SettingsAction.Save) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = saveText, modifier = Modifier.size(16.dp))
                Text(
                    text = when {
                        state.isSaved -> "Saved"
                        dirty -> "Save changes"
                        else -> "Up to date"
                    },
                    color = saveText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = Mono,
        fontSize = 10.sp,
        color = TextTertiary,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        content()
    }
}

@Composable
private fun CardDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(Line)
    )
}

@Composable
private fun StepperRow(
    label: String,
    sub: String,
    unit: String,
    value: Double,
    step: Double,
    onChange: (Double) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogInput by remember { mutableStateOf("") }
    val currentValue by rememberUpdatedState(value)
    val currentStep by rememberUpdatedState(step)
    val currentOnChange by rememberUpdatedState(onChange)
    val scope = rememberCoroutineScope()

    val stepUp: () -> Double = { kotlin.math.round((currentValue + currentStep) * 100) / 100.0 }
    val stepDown: () -> Double = { kotlin.math.round((currentValue - currentStep).coerceAtLeast(0.0) * 100) / 100.0 }

    if (showDialog) {
        val focusRequester = remember { FocusRequester() }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(label, fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = dialogInput,
                    onValueChange = { dialogInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    suffix = { Text(unit, fontFamily = Mono, fontSize = 12.sp) },
                    modifier = Modifier.focusRequester(focusRequester)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = dialogInput.replace(',', '.').toDoubleOrNull()
                    if (parsed != null) {
                        onChange(kotlin.math.round(parsed.coerceAtLeast(0.0) * 100) / 100.0)
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(sub, fontSize = 11.sp, color = TextTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        // Minus - hold to repeat
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Bg)
                .border(1.dp, Line, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown().also { it.consume() }
                        val job = scope.launch {
                            currentOnChange(stepDown())
                            delay(400)
                            while (true) {
                                currentOnChange(stepDown())
                                delay(80)
                            }
                        }
                        waitForUpOrCancellation()
                        job.cancel()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("−", fontFamily = Mono, fontSize = 16.sp, color = TextSecondary)
        }
        Spacer(Modifier.width(6.dp))
        // Value display — tap to edit
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Bg)
                .border(1.dp, Line, RoundedCornerShape(10.dp))
                .clickable {
                    dialogInput = value.format2f().replace(',', '.')
                    showDialog = true
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value.format2f(),
                fontFamily = Mono,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                letterSpacing = (-0.3).sp
            )
            Text(unit, fontFamily = Mono, fontSize = 9.sp, color = TextTertiary)
        }
        Spacer(Modifier.width(6.dp))
        // Plus - hold to repeat
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Bg)
                .border(1.dp, Line, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown().also { it.consume() }
                        val job = scope.launch {
                            currentOnChange(stepUp())
                            delay(400)
                            while (true) {
                                currentOnChange(stepUp())
                                delay(80)
                            }
                        }
                        waitForUpOrCancellation()
                        job.cancel()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontFamily = Mono, fontSize = 16.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ReadonlyRow(label: String, sub: String, value: String, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(sub, fontSize = 11.sp, color = TextTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Bg)
                .border(1.dp, Line, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontFamily = Mono, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(unit, fontFamily = Mono, fontSize = 9.sp, color = TextTertiary)
        }
    }
}
