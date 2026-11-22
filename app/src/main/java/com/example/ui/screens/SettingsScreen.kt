package com.example.ui.screens

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
import com.example.game.settings.QualityPreset
import com.example.game.valor.launch.AgeBand
import com.example.game.valor.launch.BuildPipeline
import com.example.game.valor.launch.ConsentGate
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
    var quality by remember { mutableStateOf(QualityPreset.fromId(settings.qualityPreset)) }
    var masterVolume by remember { mutableStateOf(settings.masterVolume) }
    var dialogueVolume by remember { mutableStateOf(settings.dialogueVolume) }
    var subtitlesEnabled by remember { mutableStateOf(settings.subtitlesEnabled) }

    // Section 5.3: the persistent "Privacy settings" entry. The age band is stated
    // here rather than guessed, and the two switches are clamped by the same rule
    // the first-launch dialog uses.
    var consent by remember { mutableStateOf(ConsentGate.undecided()) }

    // Section 21.3: auto-detection may recommend a preset, it never applies one.
    val recommended = remember {
        QualityPreset.recommend(
            heapMegabytes = (Runtime.getRuntime().maxMemory() / (1024L * 1024L)).toInt(),
            cores = Runtime.getRuntime().availableProcessors()
        )
    }

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
                    Spacer(modifier = Modifier.height(14.dp))

                    // Section 15.2: master and dialogue are separate buses, so a loud
                    // firefight can never bury a briefing line.
                    Text(
                        text = "Master Bus: ${(masterVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = WeatheredIvory
                    )
                    Slider(
                        value = masterVolume,
                        onValueChange = {
                            masterVolume = it
                            onSaveSettings(settings.copy(masterVolume = it))
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = SignalAmber,
                            activeTrackColor = SignalAmber
                        ),
                        modifier = Modifier.testTag("master_volume_slider")
                    )

                    Text(
                        text = "Dialogue Bus: ${(dialogueVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = WeatheredIvory
                    )
                    Slider(
                        value = dialogueVolume,
                        onValueChange = {
                            dialogueVolume = it
                            onSaveSettings(settings.copy(dialogueVolume = it))
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = SignalAmber,
                            activeTrackColor = SignalAmber
                        ),
                        modifier = Modifier.testTag("dialogue_volume_slider")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CoolSteel)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Audio-Cue Captions", style = MaterialTheme.typography.bodyMedium, color = WeatheredIvory)
                                Text(
                                    text = "Pickup and resupply calls stay readable",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = CoolSteel
                                )
                            }
                        }
                        Switch(
                            checked = subtitlesEnabled,
                            onCheckedChange = {
                                subtitlesEnabled = it
                                onSaveSettings(settings.copy(subtitlesEnabled = it))
                            },
                            modifier = Modifier.testTag("subtitles_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SignalAmber,
                                checkedTrackColor = SignalAmber.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // VISUAL FIDELITY CARD (Section 21.3)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VISUAL FIDELITY",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = QualityPreset.recommendationNote(recommended),
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolSteel
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QualityPreset.entries.forEach { preset ->
                            val selected = preset == quality
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) SignalAmber.copy(alpha = 0.18f) else DeepGraphite)
                                    .border(
                                        1.dp,
                                        if (selected) SignalAmber else DeepGraphiteBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        quality = preset
                                        onSaveSettings(settings.copy(qualityPreset = preset.id))
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("quality_${preset.id.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = preset.label,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (selected) SignalAmber else WeatheredIvory
                                    )
                                    Text(
                                        text = if (preset == recommended) "ADVISED" else " ",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = CoolSteel
                                    )
                                }
                            }
                        }
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

            // PRIVACY SETTINGS CARD (Section 5.3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_settings_entry"),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRIVACY SETTINGS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Under 13 or a stated age of \"prefer not to say\" keeps ads non-personalized and analytics essential-only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolSteel
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AgeBand.entries.forEach { band ->
                            val selected = band == consent.ageBand
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) SignalAmber.copy(alpha = 0.18f) else DeepGraphite)
                                    .border(
                                        1.dp,
                                        if (selected) SignalAmber else DeepGraphiteBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        consent = ConsentGate.stateFor(
                                            ageBand = band,
                                            personalizedAds = consent.personalizedAdsAllowed,
                                            analytics = consent.analyticsAllowed
                                        )
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("age_band_${band.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = band.displayName,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = if (selected) SignalAmber else WeatheredIvory
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personalized ads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WeatheredIvory
                        )
                        Switch(
                            checked = consent.personalizedAdsAllowed,
                            onCheckedChange = {
                                consent = ConsentGate.stateFor(
                                    ageBand = consent.ageBand,
                                    personalizedAds = it,
                                    analytics = consent.analyticsAllowed
                                )
                            },
                            enabled = !ConsentGate.requiresNonPersonalizedAds(consent.ageBand),
                            modifier = Modifier.testTag("personalized_ads_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SignalAmber,
                                checkedTrackColor = SignalAmber.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Analytics and crash reports",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WeatheredIvory
                        )
                        Switch(
                            checked = consent.analyticsAllowed,
                            onCheckedChange = {
                                consent = ConsentGate.stateFor(
                                    ageBand = consent.ageBand,
                                    personalizedAds = consent.personalizedAdsAllowed,
                                    analytics = it
                                )
                            },
                            enabled = !ConsentGate.requiresNonPersonalizedAds(consent.ageBand),
                            modifier = Modifier.testTag("analytics_consent_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SignalAmber,
                                checkedTrackColor = SignalAmber.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = consent.describe(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CoolSteel
                    )
                }
            }

            // ABOUT CARD (Section 2.2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ABOUT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = BuildPipeline.aboutLine(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WeatheredIvory,
                        modifier = Modifier.testTag("about_version_line")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${BuildPipeline.SIGNING_MODE} - keystore held outside the repository",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolSteel
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
