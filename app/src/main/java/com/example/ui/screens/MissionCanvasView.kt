package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import com.example.game.GameEngine
import com.example.game.InteractiveTarget
import com.example.game.InteractiveType
import com.example.game.Vector2
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.MutedForestGreen
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SandMuted
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WarmSand
import com.example.ui.theme.WeatheredIvory
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MissionCanvasView(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().testTag("mission_canvas_view")) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val camX = engine.cameraPos.x
            val camY = engine.cameraPos.y

            // 1. TERRAIN BACKGROUND & GRID
            drawRect(color = DeepGraphite)

            // Coastal sand terrain layer
            val worldScreenOffset = Offset(-camX, -camY)
            drawRect(
                color = WarmSand,
                topLeft = worldScreenOffset,
                size = Size(engine.worldWidth, engine.worldHeight)
            )

            // Tactical Grid lines across terrain
            val gridSpacing = 80f
            var gx = 0f
            while (gx <= engine.worldWidth) {
                val screenX = gx - camX
                drawLine(
                    color = SandMuted.copy(alpha = 0.4f),
                    start = Offset(screenX, -camY),
                    end = Offset(screenX, engine.worldHeight - camY),
                    strokeWidth = 1.2f
                )
                gx += gridSpacing
            }
            var gy = 0f
            while (gy <= engine.worldHeight) {
                val screenY = gy - camY
                drawLine(
                    color = SandMuted.copy(alpha = 0.4f),
                    start = Offset(-camX, screenY),
                    end = Offset(engine.worldWidth - camX, screenY),
                    strokeWidth = 1.2f
                )
                gy += gridSpacing
            }

            // Patrol road / gravel path from Outpost to Relay and Extraction
            val pathRoad = Path().apply {
                moveTo(160f - camX, 320f - camY)
                lineTo(380f - camX, 300f - camY)
                lineTo(600f - camX, 240f - camY)
                lineTo(850f - camX, 490f - camY)
                lineTo(980f - camX, 560f - camY)
            }
            drawPath(
                path = pathRoad,
                color = FieldOlive.copy(alpha = 0.35f),
                style = Stroke(width = 38f)
            )

            // 2. OBSTACLES & STRUCTURES WITH DROP SHADOWS
            for (obs in engine.obstacles) {
                val obsLeft = obs.left - camX
                val obsTop = obs.top - camY
                val obsWidth = obs.right - obs.left
                val obsHeight = obs.bottom - obs.top

                // Drop Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.25f),
                    topLeft = Offset(obsLeft + 8f, obsTop + 8f),
                    size = Size(obsWidth, obsHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Structure Body
                val obsColor = when (obs.label) {
                    "Relay Array Alpha" -> DeepGraphite
                    "Aux Generator Vault" -> AlertRed.copy(alpha = 0.85f)
                    "Civilian Shelter" -> MutedForestGreen
                    "Sandbag Emplacement" -> SandMuted
                    "Coastal Cliff Ridge" -> DarkGreenSurface
                    else -> FieldOlive
                }

                drawRoundRect(
                    color = obsColor,
                    topLeft = Offset(obsLeft, obsTop),
                    size = Size(obsWidth, obsHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Structural Border
                drawRoundRect(
                    color = CoolSteel.copy(alpha = 0.6f),
                    topLeft = Offset(obsLeft, obsTop),
                    size = Size(obsWidth, obsHeight),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 2.5f)
                )
            }

            // 3. SPECIAL STRUCTURE DETAILS
            // Relay Tower Alpha Antenna Beacon & Truss
            val relayCenterX = 600f - camX
            val relayCenterY = 160f - camY
            drawCircle(
                color = DeepGraphite,
                radius = 28f,
                center = Offset(relayCenterX, relayCenterY)
            )
            drawCircle(
                color = SignalAmber,
                radius = 28f,
                center = Offset(relayCenterX, relayCenterY),
                style = Stroke(width = 3f)
            )
            // Blinking antenna light
            val blinkAlpha = if ((engine.timeElapsedSeconds * 3).toInt() % 2 == 0) 1f else 0.2f
            drawCircle(
                color = AlertRed.copy(alpha = blinkAlpha),
                radius = 8f,
                center = Offset(relayCenterX, relayCenterY)
            )

            // Extraction Pad LZ Falcon Circle
            val lzX = 220f - camX
            val lzY = 660f - camY
            drawCircle(
                color = RadarCyan.copy(alpha = 0.15f),
                radius = 54f,
                center = Offset(lzX, lzY)
            )
            drawCircle(
                color = RadarCyan,
                radius = 54f,
                center = Offset(lzX, lzY),
                style = Stroke(width = 3f)
            )
            // Crosshairs on LZ
            drawLine(
                color = RadarCyan,
                start = Offset(lzX - 25f, lzY),
                end = Offset(lzX + 25f, lzY),
                strokeWidth = 2.5f
            )
            drawLine(
                color = RadarCyan,
                start = Offset(lzX, lzY - 25f),
                end = Offset(lzX, lzY + 25f),
                strokeWidth = 2.5f
            )

            // 4. SQUAD MEMBERS & RECON NPC
            // Tomas Vale (Recon)
            val tomasX = 380f - camX
            val tomasY = 300f - camY
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = 16f,
                center = Offset(tomasX + 3f, tomasY + 3f)
            )
            drawCircle(
                color = FieldOlive,
                radius = 16f,
                center = Offset(tomasX, tomasY)
            )
            drawCircle(
                color = WeatheredIvory,
                radius = 16f,
                center = Offset(tomasX, tomasY),
                style = Stroke(width = 2.5f)
            )

            // 5. INTERACTIVE TARGET HALOS
            for (target in engine.interactiveTargets) {
                if (target.isEnabled && !target.isInteracted) {
                    val tx = target.position.x - camX
                    val ty = target.position.y - camY
                    val isNearest = engine.nearestTarget?.id == target.id

                    // Halo Ring
                    val haloRadius = target.interactionRadius
                    val haloAlpha = if (isNearest) 0.8f else 0.35f
                    val haloColor = if (isNearest) SignalAmber else CoolSteel

                    drawCircle(
                        color = haloColor.copy(alpha = haloAlpha),
                        radius = haloRadius,
                        center = Offset(tx, ty),
                        style = Stroke(width = if (isNearest) 2.5f else 1.5f)
                    )

                    // Target Core Icon
                    drawCircle(
                        color = haloColor,
                        radius = 8f,
                        center = Offset(tx, ty)
                    )
                }
            }

            // 6. DYNAMIC PARTICLES (Dust, Sparks, Smoke)
            for (p in engine.particles) {
                val px = p.x - camX
                val py = p.y - camY
                val alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(p.color).copy(alpha = alpha),
                    radius = p.size,
                    center = Offset(px, py)
                )
            }

            // 7. SCANNER WAVES (Expanding Radar Rings)
            for (wave in engine.scannerWaves) {
                val wx = wave.origin.x - camX
                val wy = wave.origin.y - camY
                val waveAlpha = (1f - (wave.currentRadius / wave.maxRadius)).coerceIn(0f, 1f)
                drawCircle(
                    color = RadarCyan.copy(alpha = waveAlpha * 0.7f),
                    radius = wave.currentRadius,
                    center = Offset(wx, wy),
                    style = Stroke(width = 3.5f)
                )
            }

            // 8. PLAYER CHARACTER & FLASHLIGHT CONE
            val playerScreenX = engine.playerPos.x - camX
            val playerScreenY = engine.playerPos.y - camY
            val playerCenter = Offset(playerScreenX, playerScreenY)
            val angle = engine.playerFacingAngleRad

            // Field-of-View Cone (Wedge in front of player)
            val coneDistance = 140f
            val coneSpreadAngle = 0.55f // ~32 degrees each side
            val leftAngle = angle - coneSpreadAngle
            val rightAngle = angle + coneSpreadAngle

            val conePath = Path().apply {
                moveTo(playerScreenX, playerScreenY)
                lineTo(
                    playerScreenX + cos(leftAngle) * coneDistance,
                    playerScreenY + sin(leftAngle) * coneDistance
                )
                lineTo(
                    playerScreenX + cos(rightAngle) * coneDistance,
                    playerScreenY + sin(rightAngle) * coneDistance
                )
                close()
            }
            drawPath(
                path = conePath,
                color = SignalAmber.copy(alpha = 0.12f)
            )

            // Player Drop Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = engine.playerRadius,
                center = playerCenter + Offset(4f, 4f)
            )

            // Player Body Base
            drawCircle(
                color = DeepGraphite,
                radius = engine.playerRadius,
                center = playerCenter
            )

            // Player Tactical Armor (Signal Amber / Field Olive)
            drawCircle(
                color = SignalAmber,
                radius = engine.playerRadius,
                center = playerCenter,
                style = Stroke(width = 3f)
            )

            // Directional Facing Nose/Arrow
            val noseX = playerScreenX + cos(angle) * (engine.playerRadius + 6f)
            val noseY = playerScreenY + sin(angle) * (engine.playerRadius + 6f)
            drawLine(
                color = SignalAmber,
                start = playerCenter,
                end = Offset(noseX, noseY),
                strokeWidth = 3.5f
            )
        }
    }
}
