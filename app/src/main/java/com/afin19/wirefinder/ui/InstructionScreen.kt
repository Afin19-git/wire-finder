package com.afin19.wirefinder.ui

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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afin19.wirefinder.i18n.LocalAppLanguage
import com.afin19.wirefinder.i18n.LocalAppStrings
import com.afin19.wirefinder.ui.theme.AmberPrimary
import com.afin19.wirefinder.ui.theme.DarkBackground
import com.afin19.wirefinder.ui.theme.DarkBorder
import com.afin19.wirefinder.ui.theme.DarkSurface
import com.afin19.wirefinder.ui.theme.DarkSurfaceVariant
import com.afin19.wirefinder.ui.theme.SignalDanger
import com.afin19.wirefinder.ui.theme.TextMuted
import com.afin19.wirefinder.ui.theme.TextPrimary
import com.afin19.wirefinder.ui.theme.TextSecondary

@Composable
fun InstructionScreen(
    onStartScanning: () -> Unit,
    onOpenLanguageSelection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Language Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurface,
                modifier = Modifier
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .clickable { onOpenLanguageSelection() }
                    .testTag("instruction_language_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = strings.languageTooltip,
                        tint = AmberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentLang.nativeName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App Header Badge & Title
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AmberPrimary.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                .border(1.5.dp, AmberPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = AmberPrimary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.appName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = TextPrimary
        )

        Text(
            text = strings.appSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = AmberPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Core instructions list
        InstructionCard(
            number = "1",
            icon = Icons.Default.Power,
            iconTint = AmberPrimary,
            title = strings.instPowerTitle,
            description = strings.instPowerDesc
        )

        Spacer(modifier = Modifier.height(12.dp))

        InstructionCard(
            number = "2",
            icon = Icons.Default.Speed,
            iconTint = Color(0xFF00E5FF),
            title = strings.instMoveTitle,
            description = strings.instMoveDesc
        )

        Spacer(modifier = Modifier.height(12.dp))

        InstructionCard(
            number = "3",
            icon = Icons.Default.Tune,
            iconTint = Color(0xFF00E676),
            title = strings.instCalibTitle,
            description = strings.instCalibDesc
        )

        Spacer(modifier = Modifier.height(12.dp))

        InstructionCard(
            number = "4",
            icon = Icons.Default.Info,
            iconTint = Color(0xFFB0BEC5),
            title = strings.instDepthTitle,
            description = strings.instDepthDesc
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Safety disclaimer card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SignalDanger.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SignalDanger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = SignalDanger,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = strings.safetyTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFF8A80)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.safetyDesc,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start button
        Button(
            onClick = onStartScanning,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_scanning_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.understandStart,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InstructionCard(
    number: String,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = DarkSurfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, iconTint.copy(alpha = 0.5f), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$number. ",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AmberPrimary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
