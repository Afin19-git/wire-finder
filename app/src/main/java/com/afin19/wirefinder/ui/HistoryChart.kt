package com.afin19.wirefinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afin19.wirefinder.i18n.LocalAppStrings
import com.afin19.wirefinder.sensor.LevelPoint
import com.afin19.wirefinder.ui.theme.AmberPrimary
import com.afin19.wirefinder.ui.theme.DarkBorder
import com.afin19.wirefinder.ui.theme.DarkSurface
import com.afin19.wirefinder.ui.theme.DarkSurfaceVariant
import com.afin19.wirefinder.ui.theme.SignalDanger
import com.afin19.wirefinder.ui.theme.TextMuted
import com.afin19.wirefinder.ui.theme.TextSecondary

@Composable
fun HistoryChart(
    history: List<LevelPoint>,
    peakLevel: Float,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.historyChartTitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            if (peakLevel > 1f) {
                Text(
                    text = "${strings.peakLabel}: ${peakLevel.toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AmberPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw background grid lines (0%, 50%, 100%)
                val y50 = h * 0.5f
                val y100 = 4.dp.toPx()
                val y0 = h - 4.dp.toPx()

                drawLine(
                    color = DarkBorder,
                    start = Offset(0f, y50),
                    end = Offset(w, y50),
                    strokeWidth = 1.dp.toPx()
                )

                if (history.size < 2) return@Canvas

                val now = history.last().timestampMs
                val timeSpan = 8000L // 8 seconds window

                val path = Path()
                val fillPath = Path()

                var maxPointX = -1f
                var maxPointY = -1f
                var localMaxLevel = -1f

                history.forEachIndexed { index, point ->
                    val age = (now - point.timestampMs).coerceAtLeast(0L)
                    val x = w - (age.toFloat() / timeSpan.toFloat()) * w
                    val normalizedLevel = (point.level / 100f).coerceIn(0f, 1f)
                    val y = y0 - normalizedLevel * (y0 - y100)

                    if (point.level > localMaxLevel) {
                        localMaxLevel = point.level
                        maxPointX = x
                        maxPointY = y
                    }

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, y0)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                // Close fill path
                fillPath.lineTo(w, y0)
                fillPath.close()

                // Draw gradient under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AmberPrimary.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        startY = y100,
                        endY = y0
                    )
                )

                // Draw signal line
                drawPath(
                    path = path,
                    color = AmberPrimary,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Draw peak indicator circle if significant
                if (localMaxLevel > 15f && maxPointX >= 0f) {
                    drawCircle(
                        color = SignalDanger,
                        radius = 4.dp.toPx(),
                        center = Offset(maxPointX, maxPointY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(maxPointX, maxPointY)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = strings.historySecondsAgo,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = strings.historyNow,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
        }
    }
}
