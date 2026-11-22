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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.AcademyProgressEntity
import com.example.game.academy.DrillId
import com.example.game.combat.RankLadder
import com.example.ui.AcademyController
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/** Academy hub: the four drill modules from the training rotation. */
@Composable
fun AcademyScreen(
    controller: AcademyController,
    onBack: () -> Unit,
    onStartDrill: (DrillId) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by controller.progress.collectAsStateWithLifecycle()
    val rankIndex = progress?.rankIndex ?: 0
    val merit = progress?.meritPoints ?: 0
    val tier = RankLadder.tierAt(rankIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("academy_screen")
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                Text("BACK", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACADEMY DRILL ROTATION",
                    color = WeatheredIvory,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${tier.display}  |  $merit MP  |  ${progress?.drillRuns ?: 0} runs",
                    color = SignalAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(DrillId.values().toList()) { drill ->
                val unlocked = rankIndex >= drill.requiredRankIndex
                DrillCard(
                    drill = drill,
                    progress = progress,
                    unlocked = unlocked,
                    clearance = RankLadder.tierAt(drill.requiredRankIndex).display,
                    onStart = { onStartDrill(drill) }
                )
            }
        }
    }
}

@Composable
private fun DrillCard(
    drill: DrillId,
    progress: AcademyProgressEntity?,
    unlocked: Boolean,
    clearance: String,
    onStart: () -> Unit
) {
    val best = when (drill) {
        DrillId.RANGE -> "${progress?.rangeBestScore ?: 0} pts"
        DrillId.OBSTACLE -> if ((progress?.obstacleBestSeconds ?: 0) > 0) {
            "${progress?.obstacleBestSeconds} s"
        } else {
            "--"
        }
        DrillId.KILLHOUSE -> "${progress?.killhouseBestScore ?: 0} pts"
        DrillId.DRONE -> "${progress?.droneBestScore ?: 0} pts"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeepGraphiteElevated)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = drill.title,
                color = WeatheredIvory,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = if (unlocked) "MAX ${drill.maxMerit} MP" else "LOCKED",
                color = if (unlocked) RadarCyan else SignalAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
        }
        Text(
            text = drill.subtitle,
            color = CoolSteel,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "BEST: $best   |   LIMIT: ${drill.timeLimitSeconds.toInt()} s   |   ${clearance}",
            color = CoolSteel,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onStart,
            enabled = unlocked,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (unlocked) SignalAmber else DeepGraphite,
                contentColor = if (unlocked) DeepGraphite else CoolSteel
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (unlocked) "START DRILL" else "REQUIRES ${clearance.uppercase()}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
