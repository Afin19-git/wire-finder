package com.afin19.wirefinder.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afin19.wirefinder.i18n.LocalAppStrings
import com.afin19.wirefinder.ui.theme.DarkBorder
import com.afin19.wirefinder.ui.theme.SignalCaution
import com.afin19.wirefinder.ui.theme.SignalDanger
import com.afin19.wirefinder.ui.theme.SignalSafe
import com.afin19.wirefinder.ui.theme.SignalWarning
import com.afin19.wirefinder.ui.theme.TextMuted
import com.afin19.wirefinder.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SignalMeter(
    level: Float, // 0..100
    peakLevel: Float,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val animatedLevel by animateFloatAsState(
        targetValue = level,
        animationSpec = tween(durationMillis = 80),
        label = "meter_level"
    )

    val activeColor by animateColorAsState(
        targetValue = when {
            animatedLevel >= 80f -> SignalDanger
            animatedLevel >= 55f -> SignalWarning
            animatedLevel >= 25f -> SignalCaution
            else -> SignalSafe
        },
        animationSpec = tween(durationMillis = 150),
        label = "meter_color"
    )

    val statusLabel = when {
        animatedLevel >= 80f -> strings.meterWireHere
        animatedLevel >= 55f -> strings.meterStrongSignal
        animatedLevel >= 25f -> strings.meterApproaching
        else -> strings.meterBackground
    }

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth, strokeWidth)

            // Start angle at 135 degrees (bottom left), sweeping 270 degrees to 405 (bottom right)
            val startAngle = 135f
            val totalSweep = 270f

            // 1. Background Arc Track
            drawArc(
                color = DarkBorder,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Level Arc
            val currentSweep = (animatedLevel / 100f).coerceIn(0f, 1f) * totalSweep
            if (currentSweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.375f to SignalSafe,
                        0.625f to SignalCaution,
                        0.875f to SignalDanger,
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Peak Marker Tick
            if (peakLevel > 2f) {
                val peakSweep = (peakLevel / 100f).coerceIn(0f, 1f) * totalSweep
                val peakAngleRad = (startAngle + peakSweep) * (PI / 180.0)
                val radius = diameter / 2f
                val cx = center.x
                val cy = center.y

                val innerR = radius - strokeWidth * 0.7f
                val outerR = radius + strokeWidth * 0.7f

                val p1 = Offset(
                    (cx + innerR * cos(peakAngleRad)).toFloat(),
                    (cy + innerR * sin(peakAngleRad)).toFloat()
                )
                val p2 = Offset(
                    (cx + outerR * cos(peakAngleRad)).toFloat(),
                    (cy + outerR * sin(peakAngleRad)).toFloat()
                )

                drawLine(
                    color = Color.White,
                    start = p1,
                    end = p2,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Subtle Tick Marks along the arc
            val tickCount = 10
            for (i in 0..tickCount) {
                val tickFraction = i / tickCount.toFloat()
                val tickAngleRad = (startAngle + tickFraction * totalSweep) * (PI / 180.0)
                val tickRadius = (diameter / 2f) - strokeWidth * 0.9f
                val tickLen = if (i % 5 == 0) 8.dp.toPx() else 4.dp.toPx()

                val startPoint = Offset(
                    (center.x + tickRadius * cos(tickAngleRad)).toFloat(),
                    (center.y + tickRadius * sin(tickAngleRad)).toFloat()
                )
                val endPoint = Offset(
                    (center.x + (tickRadius - tickLen) * cos(tickAngleRad)).toFloat(),
                    (center.y + (tickRadius - tickLen) * sin(tickAngleRad)).toFloat()
                )

                drawLine(
                    color = TextMuted.copy(alpha = 0.5f),
                    start = startPoint,
                    end = endPoint,
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Central value readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedLevel.toInt()}%",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp
                ),
                color = activeColor
            )

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = activeColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (peakLevel > 0f) {
                Text(
                    text = "${strings.peakLabel}: ${peakLevel.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
