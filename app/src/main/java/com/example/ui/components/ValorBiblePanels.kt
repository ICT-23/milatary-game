package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.valor.art.ArtAct
import com.example.game.valor.art.SplitTone
import com.example.game.valor.art.VisualPillar
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/**
 * Section 2 presentation: the palette strip and the post-process grade, read
 * straight off [ArtAct] so the swatches can never disagree with the direction.
 */
@Composable
internal fun ValorPaletteStrip(act: ArtAct) {
    Row(modifier = Modifier.fillMaxWidth().height(12.dp)) {
        act.palette.forEach { swatch ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(swatch.argb))
            )
        }
    }
}

/** One act: number, title, setting, palette and the numeric grade summary. */
@Composable
internal fun ValorActBlock(act: ArtAct) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonoLabel(text = "ACT ${act.number}", color = SignalAmber, weight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            MonoLabel(text = act.title.uppercase(), color = WeatheredIvory, weight = FontWeight.Bold)
        }
        MonoLabel(text = "${act.setting} / ${act.light}", size = 8)
        Spacer(modifier = Modifier.height(5.dp))
        ValorPaletteStrip(act)
        Spacer(modifier = Modifier.height(4.dp))
        MonoLabel(text = act.palette.joinToString(" + ") { it.name }, size = 8)
        MonoLabel(text = valorGradeSummary(act), size = 8, color = RadarCyan)
    }
}

/** Section 1: pillar name plus the one-line promise every asset is graded against. */
@Composable
internal fun ValorPillarRow(pillar: VisualPillar) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        MonoLabel(text = pillar.displayName, color = WeatheredIvory, weight = FontWeight.Bold)
        MonoLabel(text = pillar.statement, size = 8)
    }
}

/** Percentages from the bible are stored as factors, so the label shows both. */
internal fun valorGradeSummary(act: ArtAct): String {
    val grade = act.grade
    val parts = mutableListOf<String>()
    if (grade.saturation != 1f) parts += "SAT ${grade.saturation}"
    if (grade.contrast != 1f) parts += "CON ${grade.contrast}"
    if (grade.sepia > 0f) parts += "SEPIA ${grade.sepia}"
    if (grade.grain > 0f) parts += "GRAIN ${grade.grain}"
    if (grade.bloom > 0f) parts += "BLOOM ${grade.bloom}"
    if (grade.warmShift > 0f) parts += "WARM ${grade.warmShift}"
    if (grade.chromaticAberration > 0f) parts += "CA ${grade.chromaticAberration}"
    if (grade.bleachBypass) parts += "BLEACH"
    if (grade.splitTone != SplitTone.NONE) parts += grade.splitTone.name
    return if (parts.isEmpty()) "NEUTRAL" else parts.joinToString(" / ")
}
