package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.InteractiveTarget
import com.example.game.TacticalObjective
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun TacticalHud(
    callsign: String,
    health: Int,
    morale: Int,
    currentObjective: TacticalObjective?,
    nearestTarget: InteractiveTarget?,
    scannerCooldownSec: Float,
    timeElapsedSeconds: Float,
    onPauseClick: () -> Unit,
    onScanClick: () -> Unit,
    onInteractClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // TOP STATUS BAR CLUSTER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left: Pause & Active Objective Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepGraphiteElevated.copy(alpha = 0.9f))
                        .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = WeatheredIvory
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepGraphite.copy(alpha = 0.88f))
                        .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(max = 280.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SignalAmber)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRIMARY OBJECTIVE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SignalAmber,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = currentObjective?.title ?: "Secure Sector 4",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 13.sp,
                        color = WeatheredIvory,
                        maxLines = 1
                    )
                    Text(
                        text = currentObjective?.detail ?: "Locate squad liaison",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = CoolSteel,
                        maxLines = 1
                    )
                }
            }

            // Right: Operative Telemetry & Morale
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphite.copy(alpha = 0.88f))
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = callsign,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WeatheredIvory
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val minutes = (timeElapsedSeconds / 60).toInt()
                    val seconds = (timeElapsedSeconds % 60).toInt()
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SignalAmber
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Condition / Health Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VITAL",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CoolSteel
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { (health / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (health > 35) FieldOlive else AlertRed,
                        trackColor = DeepGraphiteElevated
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Morale Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SYNC",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CoolSteel
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { (morale / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SignalAmber,
                        trackColor = DeepGraphiteElevated
                    )
                }
            }
        }

        // BOTTOM-RIGHT ACTION CONTROLS CLUSTER
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contextual Interaction Button
            AnimatedVisibility(
                visible = nearestTarget != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                nearestTarget?.let { target ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SignalAmber)
                            .clickable { onInteractClick() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("interact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Interact",
                            tint = DeepGraphite,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = target.prompt,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DeepGraphite,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Radar Field Scanner Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        if (scannerCooldownSec <= 0f) DeepGraphiteElevated.copy(alpha = 0.95f)
                        else DeepGraphite.copy(alpha = 0.6f)
                    )
                    .border(
                        2.dp,
                        if (scannerCooldownSec <= 0f) SignalAmber else CoolSteel.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .clickable(enabled = scannerCooldownSec <= 0f) { onScanClick() }
                    .padding(8.dp)
                    .testTag("scan_button"),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Radar Scanner",
                    tint = if (scannerCooldownSec <= 0f) SignalAmber else CoolSteel.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = if (scannerCooldownSec <= 0f) "SCAN" else String.format("%.1fs", scannerCooldownSec),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (scannerCooldownSec <= 0f) SignalAmber else CoolSteel.copy(alpha = 0.5f)
                )
            }
        }
    }
}
