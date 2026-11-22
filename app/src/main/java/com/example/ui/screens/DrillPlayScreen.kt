package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.academy.DrillId
import com.example.game.academy.DrillMarker
import com.example.game.academy.DrillMarkerState
import com.example.game.academy.DrillResult
import com.example.game.academy.DrillSnapshot
import com.example.ui.AcademyController
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.CoolSteelDark
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WarmSand
import com.example.ui.theme.WeatheredIvory

/** Runs one live drill from the shared [DrillSnapshot] contract. */
@Composable
fun DrillPlayScreen(
    controller: AcademyController,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snapshot by controller.drillSnapshot.collectAsStateWithLifecycle()
    val result by controller.lastDrillResult.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize().background(DeepGraphite)) {
        val state = snapshot
        if (state == null) {
            LaunchedEffect(Unit) { onExit() }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            controller.dismissDrill()
                            onExit()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ABORT", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.drill.title,
                            color = WeatheredIvory,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = state.statusLine,
                            color = SignalAmber,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = "${"%.0f".format(state.timeRemainingSeconds)}s",
                        color = if (state.timeRemainingSeconds < 15f) AlertRed else WeatheredIvory,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { state.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = SignalAmber,
                    trackColor = DeepGraphiteElevated
                )

                Spacer(modifier = Modifier.height(8.dp))
                DrillCanvas(
                    snapshot = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onPoint = controller::drillPoint
                )

                if (state.drill == DrillId.OBSTACLE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (state.stamina / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (state.stamina < 25f) AlertRed else RadarCyan,
                        trackColor = DeepGraphiteElevated
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listItems(state.metrics) { metric ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepGraphiteElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = metric.label,
                                color = CoolSteel,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp
                            )
                            Text(
                                text = metric.value,
                                color = WeatheredIvory,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.actions) { action ->
                        Button(
                            onClick = { controller.drillAction(action.id) },
                            enabled = action.enabled,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepGraphiteElevated,
                                contentColor = WeatheredIvory,
                                disabledContainerColor = DeepGraphite,
                                disabledContentColor = CoolSteelDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = action.label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (state.alerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    state.alerts.take(2).forEach { alert ->
                        Text(
                            text = "> $alert",
                            color = CoolSteel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        if (result != null) {
            DrillResultOverlay(
                result = result!!,
                onDone = {
                    controller.dismissDrill()
                    onExit()
                }
            )
        }
    }
}

@Composable
private fun DrillCanvas(
    snapshot: DrillSnapshot,
    modifier: Modifier,
    onPoint: (Float, Float) -> Unit
) {
    val labelPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(255, 240, 233, 219)
            textSize = 22f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF151B20))
            .pointerInput(snapshot.drill) {
                detectTapGestures { offset ->
                    onPoint(
                        (offset.x / size.width.toFloat()).coerceIn(0f, 1f),
                        (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                    )
                }
            }
    ) {
        val gridColor = Color(0xFF25303A)
        for (step in 1..9) {
            val x = size.width * step / 10f
            val y = size.height * step / 10f
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }

        if (snapshot.drill == DrillId.DRONE) {
            for (row in 0..4) {
                val y = size.height * row / 4f
                drawLine(DeepGraphiteBorder, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
            }
            for (column in 0..6) {
                val x = size.width * column / 6f
                drawLine(DeepGraphiteBorder, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
            }
        }

        snapshot.markers.forEach { marker ->
            val center = Offset(marker.x * size.width, marker.y * size.height)
            val color = markerColor(marker.state)
            drawCircle(color.copy(alpha = 0.18f), radius = 26f, center = center)
            drawCircle(color, radius = 13f, center = center, style = Stroke(width = 2.5f))
            if (marker.state == DrillMarkerState.ACTIVE || marker.state == DrillMarkerState.CONVOY) {
                drawCircle(color, radius = 5f, center = center)
            }
            if (marker.label.isNotBlank()) {
                drawContext.canvas.nativeCanvas.drawText(
                    marker.label,
                    center.x,
                    center.y - 20f,
                    labelPaint
                )
            }
            if (marker.sublabel.isNotBlank()) {
                drawContext.canvas.nativeCanvas.drawText(
                    marker.sublabel,
                    center.x,
                    center.y + 34f,
                    labelPaint
                )
            }
        }
    }
}

private fun markerColor(state: DrillMarkerState): Color = when (state) {
    DrillMarkerState.ACTIVE -> SignalAmber
    DrillMarkerState.TARGET -> CoolSteel
    DrillMarkerState.HOSTILE -> AlertRed
    DrillMarkerState.FRIENDLY -> RadarCyan
    DrillMarkerState.DOWN -> CoolSteelDark
    DrillMarkerState.STUNNED -> ShieldBlue
    DrillMarkerState.SEALED -> DeepGraphiteBorder
    DrillMarkerState.CONVOY -> AlertRed
    DrillMarkerState.IMPACT -> WarmSand
    DrillMarkerState.PLAYER -> RadarCyan
}

@Composable
private fun DrillResultOverlay(result: DrillResult, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC101418)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(14.dp))
                .background(DeepGraphiteElevated)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (result.passed) "QUALIFICATION PASSED" else "QUALIFICATION FAILED",
                color = if (result.passed) RadarCyan else AlertRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = result.score.toString(),
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp
            )
            Text(
                text = "+${result.meritAwarded} MP   |   ${result.accuracyPercent}% accuracy",
                color = SignalAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Text(
                text = "Field time ${"%.1f".format(result.durationSeconds)} s  |  infractions ${result.infractions}",
                color = CoolSteel,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            result.notes.forEach { note ->
                Text(
                    text = "· $note",
                    color = CoolSteel,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SignalAmber, contentColor = DeepGraphite),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "RETURN TO ACADEMY",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
