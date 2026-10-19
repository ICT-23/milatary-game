package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.Vector2
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.SignalAmber
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    sizeDp: Int = 120,
    onMove: (Vector2) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val radiusPx = size.width / 2f
                val maxDragRadius = radiusPx * 0.75f

                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(radiusPx, radiusPx)
                        val delta = offset - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val angle = atan2(delta.y, delta.x)
                        val clampedDist = dist.coerceAtMost(maxDragRadius)

                        val clampedX = cos(angle) * clampedDist
                        val clampedY = sin(angle) * clampedDist
                        thumbOffset = Offset(clampedX, clampedY)

                        val normX = clampedX / maxDragRadius
                        val normY = clampedY / maxDragRadius
                        onMove(Vector2(normX, normY))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val angle = atan2(newOffset.y, newOffset.x)
                        val clampedDist = dist.coerceAtMost(maxDragRadius)

                        val clampedX = cos(angle) * clampedDist
                        val clampedY = sin(angle) * clampedDist
                        thumbOffset = Offset(clampedX, clampedY)

                        val normX = clampedX / maxDragRadius
                        val normY = clampedY / maxDragRadius
                        onMove(Vector2(normX, normY))
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(Vector2.Zero)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(Vector2.Zero)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4f
            val thumbRadius = size.width / 5f

            // Outer Base Ring
            drawCircle(
                color = DeepGraphite.copy(alpha = 0.65f),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = CoolSteel.copy(alpha = 0.45f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Cardinal Ticks
            val tickLen = 6.dp.toPx()
            drawLine(
                color = CoolSteel.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - outerRadius),
                end = Offset(center.x, center.y - outerRadius + tickLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = CoolSteel.copy(alpha = 0.6f),
                start = Offset(center.x, center.y + outerRadius),
                end = Offset(center.x, center.y + outerRadius - tickLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = CoolSteel.copy(alpha = 0.6f),
                start = Offset(center.x - outerRadius, center.y),
                end = Offset(center.x - outerRadius + tickLen, center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = CoolSteel.copy(alpha = 0.6f),
                start = Offset(center.x + outerRadius, center.y),
                end = Offset(center.x + outerRadius - tickLen, center.y),
                strokeWidth = 2.dp.toPx()
            )

            // Dynamic Thumb Knob
            val currentThumbPos = center + thumbOffset
            drawCircle(
                color = DeepGraphite.copy(alpha = 0.85f),
                radius = thumbRadius,
                center = currentThumbPos
            )
            drawCircle(
                color = if (thumbOffset != Offset.Zero) SignalAmber else CoolSteel,
                radius = thumbRadius,
                center = currentThumbPos,
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawCircle(
                color = if (thumbOffset != Offset.Zero) SignalAmber.copy(alpha = 0.7f) else CoolSteel.copy(alpha = 0.3f),
                radius = thumbRadius * 0.4f,
                center = currentThumbPos
            )
        }
    }
}
