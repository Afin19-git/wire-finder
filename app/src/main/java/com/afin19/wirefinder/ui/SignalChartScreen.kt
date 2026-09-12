package com.afin19.wirefinder.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afin19.wirefinder.i18n.LocalAppStrings
import com.afin19.wirefinder.sensor.DeltaPoint
import com.afin19.wirefinder.sensor.WireFinderState
import com.afin19.wirefinder.ui.theme.AmberPrimary
import com.afin19.wirefinder.ui.theme.CyanAccent
import com.afin19.wirefinder.ui.theme.DarkBackground
import com.afin19.wirefinder.ui.theme.DarkBorder
import com.afin19.wirefinder.ui.theme.DarkSurface
import com.afin19.wirefinder.ui.theme.DarkSurfaceVariant
import com.afin19.wirefinder.ui.theme.SignalDanger
import com.afin19.wirefinder.ui.theme.SignalSafe
import com.afin19.wirefinder.ui.theme.SignalWarning
import com.afin19.wirefinder.ui.theme.TextMuted
import com.afin19.wirefinder.ui.theme.TextPrimary
import com.afin19.wirefinder.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs

/**
 * Dedicated Signal Waveform Chart screen.
 * Displays a single scrolling line of delta(t) = B(t) - baseline(t)
 * with auto-scaling on the Y axis, plus an optional smoothed envelope overlay.
 */
@Composable
fun SignalChartScreen(
    state: WireFinderState,
    onBack: () -> Unit,
    onStartCalibration: () -> Unit,
    onResetPeak: () -> Unit,
    onToggleSound: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showEnvelope by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_to_scanner_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.backToScanner,
                    tint = TextPrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.chartTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = strings.chartFormula,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }

            // Sound feedback toggle
            IconButton(
                onClick = { onToggleSound(!state.soundEnabled) },
                modifier = Modifier.testTag("chart_sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (state.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (state.soundEnabled) strings.soundOnTooltip else strings.soundOffTooltip,
                    tint = if (state.soundEnabled) SignalWarning else TextMuted
                )
            }

            // Frequency chip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (state.isSimulationMode) AmberPrimary else SignalSafe)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${state.samplingRateHz.coerceAtLeast(40)} ${strings.hzUnit}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Telemetry readout cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = strings.metricDeltaCurrent,
                value = String.format(Locale.US, "%+.2f µT", state.delta),
                valueColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = strings.metricSignalSpan,
                value = String.format(Locale.US, "%.2f µT", state.displaySignal),
                valueColor = AmberPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = strings.metricIndicator,
                value = "${state.level.toInt()}%",
                valueColor = when {
                    state.level >= 75f -> SignalDanger
                    state.level >= 45f -> SignalWarning
                    else -> SignalSafe
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dedicated Scrolling Waveform Chart Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .testTag("signal_chart_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                ScrollingDeltaChart(
                    deltaHistory = state.deltaHistory,
                    showEnvelope = showEnvelope,
                    baselineText = strings.baselineGridText,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Controls below chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Envelope toggle switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showEnvelope = !showEnvelope }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Switch(
                    checked = showEnvelope,
                    onCheckedChange = { showEnvelope = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberPrimary,
                        checkedTrackColor = AmberPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("toggle_envelope_switch")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = strings.envelopeTitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (showEnvelope) AmberPrimary else TextSecondary
                    )
                    Text(
                        text = strings.envelopeSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Legend indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp, 3.dp)
                        .background(CyanAccent)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ΔB(t)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action buttons row (Calibrate & Reset peak)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStartCalibration,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("chart_calibrate_button")
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

            OutlinedButton(
                onClick = onResetPeak,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("chart_reset_peak_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.resetPeakButton)
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = valueColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Scrolling Line Chart with Y-axis auto-scaling.
 * Window: last 4.0 seconds.
 * Draws:
 * - Single main line: delta(t) = B(t) - baseline(t)
 * - Optional thinner dashed lines: +envelope and -envelope
 * - 0 µT center baseline and auto-scale grid with scale labels.
 */
@Composable
private fun ScrollingDeltaChart(
    deltaHistory: List<DeltaPoint>,
    showEnvelope: Boolean,
    baselineText: String = " 0.00 µT (baseline)",
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val paddingVertical = 20.dp.toPx()
        val graphHeight = height - (paddingVertical * 2f)
        val centerY = paddingVertical + graphHeight / 2f

        // Window of 4.0 seconds (4000 ms)
        val windowMs = 4000L
        val now = deltaHistory.lastOrNull()?.timestampMs ?: android.os.SystemClock.elapsedRealtime()
        val minTime = now - windowMs

        // Filter points within window
        val visiblePoints = deltaHistory.filter { it.timestampMs >= minTime }

        // Y-axis Auto-scaling: find max absolute value of delta (and envelope if shown)
        var maxObserved = 0f
        for (p in visiblePoints) {
            val absDelta = abs(p.delta)
            if (absDelta > maxObserved) maxObserved = absDelta
            if (showEnvelope && p.envelope > maxObserved) maxObserved = p.envelope
        }

        // Clamp minimum Y scale to 0.4 µT so low idle noise does not blow up into giant spikes
        val yLimit = maxOf(maxObserved * 1.25f, 0.40f)

        // Draw horizontal grid lines (Center Y=0, +yLimit, -yLimit, and halves)
        val gridColor = DarkBorder.copy(alpha = 0.6f)
        val centerLineColor = CyanAccent.copy(alpha = 0.25f)
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(160, 154, 164, 184)
            textSize = 10.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            isAntiAlias = true
        }

        // Top line (+yLimit)
        val topY = paddingVertical
        drawLine(
            color = gridColor,
            start = Offset(0f, topY),
            end = Offset(width, topY),
            strokeWidth = 1f
        )
        drawContext.canvas.nativeCanvas.drawText(
            String.format(Locale.US, "+%.2f µT", yLimit),
            6f,
            topY + 12.sp.toPx(),
            textPaint
        )

        // Center line (0.0 µT)
        drawLine(
            color = centerLineColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.5f
        )
        drawContext.canvas.nativeCanvas.drawText(
            baselineText,
            6f,
            centerY - 4f,
            textPaint
        )

        // Bottom line (-yLimit)
        val bottomY = height - paddingVertical
        drawLine(
            color = gridColor,
            start = Offset(0f, bottomY),
            end = Offset(width, bottomY),
            strokeWidth = 1f
        )
        drawContext.canvas.nativeCanvas.drawText(
            String.format(Locale.US, "-%.2f µT", yLimit),
            6f,
            bottomY - 4f,
            textPaint
        )

        // Vertical time grid lines (every 1 second: -1s, -2s, -3s)
        val timePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(120, 95, 107, 130)
            textSize = 9.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            isAntiAlias = true
        }
        for (sec in 1..3) {
            val x = width - (sec * 1000f / windowMs) * width
            drawLine(
                color = gridColor.copy(alpha = 0.35f),
                start = Offset(x, paddingVertical),
                end = Offset(x, bottomY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            drawContext.canvas.nativeCanvas.drawText(
                "-${sec}с",
                x + 4f,
                bottomY - 4f,
                timePaint
            )
        }

        if (visiblePoints.size < 2) return@Canvas

        // Optional: Draw smoothed envelope lines (+signal and -signal)
        if (showEnvelope) {
            val upperEnvPath = Path()
            val lowerEnvPath = Path()
            var isFirst = true

            for (p in visiblePoints) {
                val age = (now - p.timestampMs).coerceAtLeast(0L)
                val x = width - (age.toFloat() / windowMs) * width
                val envValue = p.envelope.coerceAtLeast(0f)
                val yUpper = centerY - (envValue / yLimit) * (graphHeight / 2f)
                val yLower = centerY + (envValue / yLimit) * (graphHeight / 2f)

                if (isFirst) {
                    upperEnvPath.moveTo(x, yUpper)
                    lowerEnvPath.moveTo(x, yLower)
                    isFirst = false
                } else {
                    upperEnvPath.lineTo(x, yUpper)
                    lowerEnvPath.lineTo(x, yLower)
                }
            }

            val envelopeStroke = Stroke(
                width = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                cap = StrokeCap.Round
            )
            drawPath(
                path = upperEnvPath,
                color = AmberPrimary.copy(alpha = 0.8f),
                style = envelopeStroke
            )
            drawPath(
                path = lowerEnvPath,
                color = AmberPrimary.copy(alpha = 0.8f),
                style = envelopeStroke
            )
        }

        // Draw the MAIN SINGLE LINE: delta(t) = B(t) - baseline(t)
        val deltaPath = Path()
        var first = true

        for (p in visiblePoints) {
            val age = (now - p.timestampMs).coerceAtLeast(0L)
            val x = width - (age.toFloat() / windowMs) * width
            val clampedDelta = p.delta.coerceIn(-yLimit, yLimit)
            val y = centerY - (clampedDelta / yLimit) * (graphHeight / 2f)

            if (first) {
                deltaPath.moveTo(x, y)
                first = false
            } else {
                deltaPath.lineTo(x, y)
            }
        }

        // Main line stroke
        drawPath(
            path = deltaPath,
            color = CyanAccent,
            style = Stroke(
                width = 2.4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw glowing point at current position (right edge)
        val lastPoint = visiblePoints.last()
        val currentClamped = lastPoint.delta.coerceIn(-yLimit, yLimit)
        val currentY = centerY - (currentClamped / yLimit) * (graphHeight / 2f)
        val currentX = width

        drawCircle(
            color = CyanAccent.copy(alpha = 0.35f),
            radius = 7.dp.toPx(),
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = CyanAccent,
            radius = 3.5.dp.toPx(),
            center = Offset(currentX, currentY)
        )
    }
}
