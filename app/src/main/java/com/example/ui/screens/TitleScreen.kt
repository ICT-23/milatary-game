package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampaignProfileEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TitleScreen(
    profile: CampaignProfileEntity?,
    onStartCampaign: () -> Unit,
    onOpenRoster: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExportSpec: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Infinite transition for live background radar sweep
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .testTag("title_screen")
    ) {
        // ATMOSPHERIC LIVE RADAR SWEEP BACKGROUND
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.width.coerceAtLeast(size.height) * 0.7f

            // Concentric tactical rings
            for (r in listOf(0.2f, 0.4f, 0.6f, 0.8f)) {
                drawCircle(
                    color = CoolSteel.copy(alpha = 0.15f),
                    radius = maxR * r,
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }

            // Crosshair lines
            drawLine(
                color = CoolSteel.copy(alpha = 0.15f),
                start = Offset(center.x - maxR, center.y),
                end = Offset(center.x + maxR, center.y),
                strokeWidth = 1.2f
            )
            drawLine(
                color = CoolSteel.copy(alpha = 0.15f),
                start = Offset(center.x, center.y - maxR),
                end = Offset(center.x, center.y + maxR),
                strokeWidth = 1.2f
            )

            // Live rotating sweep line
            val rad = Math.toRadians(sweepAngle.toDouble()).toFloat()
            val sweepEnd = Offset(
                center.x + cos(rad) * maxR,
                center.y + sin(rad) * maxR
            )
            drawLine(
                color = SignalAmber.copy(alpha = 0.5f),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.5f
            )

            // Fictional coastal blips
            val blip1 = Offset(center.x + maxR * 0.35f, center.y - maxR * 0.25f)
            drawCircle(color = RadarCyan.copy(alpha = 0.7f), radius = 4f, center = blip1)
            val blip2 = Offset(center.x - maxR * 0.4f, center.y + maxR * 0.3f)
            drawCircle(color = AlertRed.copy(alpha = 0.7f), radius = 4f, center = blip2)
        }

        // FOREGROUND TITLE & NAVIGATION UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: System status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RadarCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SECURE LINK ACTIVE // AST-4",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = RadarCyan,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "REPUBLIC OF ASTERRA",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CoolSteel,
                    letterSpacing = 1.sp
                )
            }

            // Central Branding & Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 500.dp)
            ) {
                // Emblem Icon Box
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepGraphiteElevated)
                        .border(2.dp, SignalAmber, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(44.dp)) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = SignalAmber,
                            radius = 18f,
                            center = c,
                            style = Stroke(width = 2.5f)
                        )
                        drawLine(
                            color = SignalAmber,
                            start = Offset(c.x, c.y - 18f),
                            end = Offset(c.x, c.y - 6f),
                            strokeWidth = 2.5f
                        )
                        drawLine(
                            color = SignalAmber,
                            start = Offset(c.x, c.y + 6f),
                            end = Offset(c.x, c.y + 18f),
                            strokeWidth = 2.5f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OPERATION: EMBERWATCH",
                    style = MaterialTheme.typography.headlineLarge,
                    color = WeatheredIvory,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "TACTICAL MILITARY ACTION & RECRUIT PROGRESSION",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalAmber,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.2.sp
                )

                if (profile != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "OPERATIVE: ${profile.callsign}  |  RANK: ${profile.rankTitle}  |  XP: ${profile.experiencePoints}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CoolSteel
                    )
                }
            }

            // Primary Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartCampaign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_mission_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignalAmber,
                        contentColor = DeepGraphite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if ((profile?.experiencePoints ?: 0) > 0) "CONTINUE CAMPAIGN" else "START MISSION 1: SIGNAL IN THE DUST",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenRoster,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("roster_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DeepGraphiteElevated.copy(alpha = 0.8f),
                            contentColor = WeatheredIvory
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SQUAD ROSTER",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("settings_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DeepGraphiteElevated.copy(alpha = 0.8f),
                            contentColor = WeatheredIvory
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SETTINGS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenExportSpec,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("export_spec_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepGraphite.copy(alpha = 0.7f),
                        contentColor = CoolSteel
                    )
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COMPANION REACT NATIVE EXPORT SPEC",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
