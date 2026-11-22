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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampaignProfileEntity
import com.example.data.MissionProgressEntity
import com.example.game.MissionCatalog
import com.example.game.MissionDossier
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/**
 * Campaign board. Lists the seven chapters in order, shows the best rating the
 * player has earned on each and gates the ones still locked behind progression.
 */
@Composable
fun MissionSelectScreen(
    missions: List<MissionProgressEntity>,
    profile: CampaignProfileEntity?,
    onSelectMission: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressById = missions.associateBy { it.missionId }
    val totalStars = MissionCatalog.ALL_MISSIONS.sumOf { dossier ->
        progressById[dossier.id]?.stars ?: 0
    }
    val cleared = MissionCatalog.ALL_MISSIONS.count { progressById[it.id]?.isCompleted == true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("mission_select_screen")
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("mission_select_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WeatheredIvory
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OPERATIONS BOARD",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SignalAmber,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${profile?.rankTitle?.uppercase() ?: "JUNIOR CADET"} // ${profile?.experiencePoints ?: 0} XP",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = CoolSteel
                )
            }

            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CAMPAIGN SUMMARY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DeepGraphiteElevated)
                .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHAPTERS CLEARED  $cleared / ${MissionCatalog.ALL_MISSIONS.size}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = WeatheredIvory
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = SignalAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$totalStars / ${MissionCatalog.ALL_MISSIONS.size * 3}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = SignalAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(MissionCatalog.ALL_MISSIONS, key = { it.id }) { dossier ->
                val progress = progressById[dossier.id]
                // Chapter 1 is always available, even before the board finishes loading.
                val unlocked = progress?.isUnlocked == true || dossier.chapterNumber == 1
                MissionRow(
                    dossier = dossier,
                    progress = progress,
                    unlocked = unlocked,
                    onClick = { if (unlocked) onSelectMission(dossier.id) }
                )
            }

            item {
                Text(
                    text = "Additional chapters unlock as each operation is cleared. Replay any unlocked chapter to improve its rating.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = CoolSteel,
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MissionRow(
    dossier: MissionDossier,
    progress: MissionProgressEntity?,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    val completed = progress?.isCompleted == true
    val stars = progress?.stars ?: 0
    val accent = when {
        !unlocked -> DeepGraphiteBorder
        completed -> FieldOlive
        else -> SignalAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("mission_card_${dossier.id}"),
        colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accent.copy(alpha = if (unlocked) 0.55f else 0.25f), RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chapter index
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (unlocked) accent.copy(alpha = 0.18f) else DeepGraphite)
                    .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d".format(dossier.chapterNumber),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (unlocked) accent else CoolSteel
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dossier.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (unlocked) WeatheredIvory else CoolSteel,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = dossier.subtitle,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (unlocked) CoolSteel else CoolSteel.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        !unlocked -> "LOCKED // CLEAR CHAPTER ${dossier.chapterNumber - 1}"
                        completed -> "BEST ${progress?.score ?: 0} PTS  •  ${progress?.completionTimeSeconds ?: 0L}s"
                        else -> "READY FOR DEPLOYMENT"
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    color = when {
                        !unlocked -> CoolSteel.copy(alpha = 0.55f)
                        completed -> RadarCyan
                        else -> SignalAmber
                    }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                !unlocked -> Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = CoolSteel.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                completed -> Column(horizontalAlignment = Alignment.End) {
                    Row {
                        repeat(3) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < stars) SignalAmber else CoolSteel.copy(alpha = 0.25f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FieldOlive,
                        modifier = Modifier.size(15.dp)
                    )
                }
                else -> Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Deploy",
                    tint = SignalAmber,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
