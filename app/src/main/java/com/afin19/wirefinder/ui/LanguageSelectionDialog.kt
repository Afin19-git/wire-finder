package com.afin19.wirefinder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.afin19.wirefinder.i18n.AppLanguage
import com.afin19.wirefinder.i18n.getStrings
import com.afin19.wirefinder.ui.theme.AmberPrimary
import com.afin19.wirefinder.ui.theme.DarkBorder
import com.afin19.wirefinder.ui.theme.DarkSurface
import com.afin19.wirefinder.ui.theme.DarkSurfaceVariant
import com.afin19.wirefinder.ui.theme.TextPrimary
import com.afin19.wirefinder.ui.theme.TextSecondary

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: (() -> Unit)? = null,
    isFirstLaunch: Boolean = false
) {
    var selectedLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }
    val strings = getStrings(selectedLanguage)

    Dialog(
        onDismissRequest = {
            if (!isFirstLaunch) {
                onDismiss?.invoke()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isFirstLaunch,
            dismissOnClickOutside = !isFirstLaunch
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, AmberPrimary, RoundedCornerShape(20.dp))
                .testTag("language_selection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(1.dp, AmberPrimary.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.chooseLanguageTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.chooseLanguageDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val languages = listOf(
                        AppLanguage.ENGLISH to "🇺🇸",
                        AppLanguage.RUSSIAN to "🇷🇺",
                        AppLanguage.UKRAINIAN to "🇺🇦"
                    )

                    languages.forEach { (lang, flag) ->
                        val isSelected = selectedLanguage == lang
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AmberPrimary else DarkBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedLanguage = lang }
                                .testTag("language_option_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = flag,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) AmberPrimary else TextPrimary
                                    )
                                    if (lang.nativeName != lang.displayName) {
                                        Text(
                                            text = lang.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedLanguage = lang },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AmberPrimary,
                                        unselectedColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        onLanguageSelected(selectedLanguage)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_language_button")
                ) {
                    Text(
                        text = strings.confirmLanguage,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
