package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.combat.RankLadder
import com.example.ui.AcademyController
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/** Phase 5 of the roadmap: merit ladder, clearances and promotion ceremonies. */
@Composable
fun RankScreen(
    controller: AcademyController,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by controller.progress.collectAsStateWithLifecycle()

    val rankIndex = progress?.rankIndex ?: 0
    val merit = progress?.meritPoints ?: 0
    val current = RankLadder.tierAt(rankIndex)
    val next = RankLadder.nextTier(rankIndex)
    val ratio = RankLadder.progress(rankIndex, merit)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("rank_screen")
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                Text("BACK", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ACADEMY RANK LADDER",
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepGraphiteElevated)
                .padding(14.dp)
        ) {
            Text(
                text = current.insignia,
                color = SignalAmber,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            Text(
                text = "${current.code} · ${current.title}",
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$merit MERIT POINTS",
                color = CoolSteel,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SignalAmber,
                trackColor = DeepGraphite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (next == null) {
                    "Supreme command reached."
                } else {
                    "${next.meritRequired - merit} MP to ${next.insignia} · ${next.title}"
                },
                color = RadarCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(RankLadder.TIERS) { tier ->
                val reached = rankIndex >= tier.index
                val isCurrent = rankIndex == tier.index
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrent) FieldOlive.copy(alpha = 0.55f) else DeepGraphiteElevated)
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "${tier.insignia} · ${tier.title}",
                            color = if (reached) WeatheredIvory else CoolSteel,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (tier.meritRequired == 0) "0 MP" else "${tier.meritRequired} MP",
                            color = if (reached) SignalAmber else AlertRed,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = tier.unlocks.joinToString(" · "),
                        color = CoolSteel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
