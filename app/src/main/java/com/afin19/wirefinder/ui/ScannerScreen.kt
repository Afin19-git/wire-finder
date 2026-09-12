package com.afin19.wirefinder.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afin19.wirefinder.i18n.LocalAppLanguage
import com.afin19.wirefinder.i18n.LocalAppStrings
import com.afin19.wirefinder.sensor.WireFinderState
import com.afin19.wirefinder.ui.theme.AmberPrimary
import com.afin19.wirefinder.ui.theme.CyanAccent
import com.afin19.wirefinder.ui.theme.DarkBackground
import com.afin19.wirefinder.ui.theme.DarkBorder
import com.afin19.wirefinder.ui.theme.DarkSurface
import com.afin19.wirefinder.ui.theme.DarkSurfaceVariant
import com.afin19.wirefinder.ui.theme.SignalCaution
import com.afin19.wirefinder.ui.theme.SignalDanger
import com.afin19.wirefinder.ui.theme.SignalSafe
import com.afin19.wirefinder.ui.theme.SignalWarning
import com.afin19.wirefinder.ui.theme.TextMuted
import com.afin19.wirefinder.ui.theme.TextPrimary
import com.afin19.wirefinder.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    state: WireFinderState,
    onOpenInstructions: () -> Unit,
    onOpenChart: () -> Unit,
    onStartCalibration: () -> Unit,
    onResetPeak: () -> Unit,
    onSensitivityChanged: (Float) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleSimulation: () -> Unit,
    onOpenLanguageSelection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showTelemetryDetails by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sensor status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurface,
                    modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.isSimulationMode) AmberPrimary else SignalSafe
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isSimulationMode) {
                                strings.simMode
                            } else {
                                strings.sensorFormat(state.samplingRateHz.coerceAtLeast(40))
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Language selection button in top bar
                IconButton(
                    onClick = onOpenLanguageSelection,
                    modifier = Modifier.testTag("scanner_language_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = strings.languageTooltip,
                        tint = AmberPrimary
                    )
                }

                // Chart button in top bar
                IconButton(
                    onClick = onOpenChart,
                    modifier = Modifier.testTag("chart_screen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = strings.chartTooltip,
                        tint = CyanAccent
                    )
                }

                // Haptic feedback toggle
                IconButton(
                    onClick = { onToggleHaptic(!state.hapticEnabled) },
                    modifier = Modifier.testTag("haptic_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = if (state.hapticEnabled) strings.hapticOnTooltip else strings.hapticOffTooltip,
                        tint = if (state.hapticEnabled) AmberPrimary else TextMuted
                    )
                }

                // Sound feedback toggle
                IconButton(
                    onClick = { onToggleSound(!state.soundEnabled) },
                    modifier = Modifier.testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (state.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (state.soundEnabled) strings.soundOnTooltip else strings.soundOffTooltip,
                        tint = if (state.soundEnabled) SignalWarning else TextMuted
                    )
                }

                // Help/Instructions button
                IconButton(
                    onClick = onOpenInstructions,
                    modifier = Modifier.testTag("instruction_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = strings.instructionsTooltip,
                        tint = TextPrimary
                    )
                }
            }

            // Hardware missing notice if applicable
            if (!state.isSensorAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.noSensorNotice,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Signal Meter Gauge
            SignalMeter(
                level = state.level,
                peakLevel = state.peakLevel,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-time dynamic history chart
            HistoryChart(
                history = state.history,
                peakLevel = state.peakLevel
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated Signal Waveform Chart Button
            OutlinedButton(
                onClick = onOpenChart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("open_chart_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyanAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.openChartButton,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Calibration Button
                Button(
                    onClick = onStartCalibration,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("calibrate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.calibrateButton,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Reset Peak Button
                OutlinedButton(
                    onClick = onResetPeak,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("reset_peak_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.resetPeakButton,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sensitivity Preset Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.sensitivityLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val options = listOf(
                        "0.7×" to 0.7f,
                        "1.0×" to 1.0f,
                        "1.5×" to 1.5f,
                        "2.0×" to 2.0f
                    )
                    options.forEach { (label, value) ->
                        val isSelected = kotlin.math.abs(state.sensitivity - value) < 0.1f
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSensitivityChanged(value) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = DarkBorder,
                                selectedBorderColor = AmberPrimary
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable Telemetry / Diagnostics Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .clickable { showTelemetryDetails = !showTelemetryDetails }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.physicsTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (showTelemetryDetails) strings.hideDetails else strings.showDetails,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberPrimary
                        )
                    }

                    AnimatedVisibility(visible = showTelemetryDetails) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            TelemetryRow(
                                label = strings.rawMagnitudeLabel,
                                value = "${String.format(Locale.US, "%.1f", state.rawMagnitude)} µT"
                            )
                            TelemetryRow(
                                label = strings.baselineLabel,
                                value = "${String.format(Locale.US, "%.1f", state.baseline)} µT"
                            )
                            TelemetryRow(
                                label = strings.deltaBAmplitudeLabel,
                                value = "${String.format(Locale.US, "%.2f", state.displaySignal)} µT"
                            )
                            TelemetryRow(
                                label = strings.noiseFloorLabel,
                                value = "${String.format(Locale.US, "%.2f", state.noiseFloor)} µT"
                            )
                            TelemetryRow(
                                label = strings.samplingRateLabel,
                                value = "${state.samplingRateHz} ${strings.hzUnit}"
                            )

                            if (state.isSensorAvailable) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = if (state.isSimulationMode) strings.turnOffSim else strings.turnOnSim,
                                        style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent),
                                        modifier = Modifier.clickable { onToggleSimulation() }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
    }
}
