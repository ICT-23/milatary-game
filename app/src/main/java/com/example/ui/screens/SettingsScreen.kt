package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TacticalSettingsEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun SettingsScreen(
    settings: TacticalSettingsEntity,
    onSaveSettings: (TacticalSettingsEntity) -> Unit,
    onResetCampaign: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sfxEnabled by remember { mutableStateOf(settings.soundEffectsVolume > 0.05f) }
    var hapticEnabled by remember { mutableStateOf(settings.hapticEnabled) }
    var sensitivity by remember { mutableStateOf(settings.joystickSensitivity) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("settings_screen")
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WeatheredIvory
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "TACTICAL SETTINGS & PROTOCOLS",
                    style = MaterialTheme.typography.titleMedium,
                    color = WeatheredIvory,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "LOCAL PERSISTENCE & HARDWARE CONTROLS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SignalAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // AUDIO & HAPTICS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIO & HAPTIC FEEDBACK",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // SFX Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CoolSteel)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Procedural Audio Synthesizer", style = MaterialTheme.typography.bodyMedium, color = WeatheredIvory)
                        }
                        Switch(
                            checked = sfxEnabled,
                            onCheckedChange = {
                                sfxEnabled = it
                                onSaveSettings(settings.copy(soundEffectsVolume = if (it) 0.8f else 0f))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SignalAmber,
                                checkedTrackColor = SignalAmber.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Haptic Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = CoolSteel)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Tactile Haptic Recoil", style = MaterialTheme.typography.bodyMedium, color = WeatheredIvory)
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = {
                                hapticEnabled = it
                                onSaveSettings(settings.copy(hapticEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SignalAmber,
                                checkedTrackColor = SignalAmber.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // CONTROLS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INPUT RESPONSIVENESS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Virtual Joystick Sensitivity: ${(sensitivity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = WeatheredIvory
                    )

                    Slider(
                        value = sensitivity,
                        onValueChange = {
                            sensitivity = it
                            onSaveSettings(settings.copy(joystickSensitivity = it))
                        },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = SignalAmber,
                            activeTrackColor = SignalAmber
                        )
                    )
                }
            }

            // RESET CAMPAIGN DANGER ZONE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CAMPAIGN DATA RECOVERY",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AlertRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Clear all local SQLite campaign progress, dialogue flags, and operative promotions to start fresh from Academy Day 1.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolSteel
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = WeatheredIvory
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reset_campaign_button")
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESET CAMPAIGN PROGRESS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // CONFIRMATION DIALOG
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = {
                    Text(
                        text = "CONFIRM CAMPAIGN PURGE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AlertRed
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete local mission records and start over as an unranked cadet?",
                        color = WeatheredIvory
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirm = false
                            onResetCampaign()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("CONFIRM RESET")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showResetConfirm = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGraphiteBorder)
                    ) {
                        Text("CANCEL")
                    }
                },
                containerColor = DeepGraphiteElevated
            )
        }
    }
}
