package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampaignProfileEntity
import com.example.game.MissionCatalog
import com.example.game.SquadMemberDossier
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun RosterScreen(
    profile: CampaignProfileEntity?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("roster_screen")
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
                    .testTag("roster_back_button")
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
                    text = "SQUAD DOSSIERS & INTELLIGENCE",
                    style = MaterialTheme.typography.titleMedium,
                    color = WeatheredIvory,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "ASTERRA DEFENSE FORCE // EMBERWATCH CONTINGENT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SignalAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FIELD RECORD — drives the standing meters below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RecordChip(
                label = "RANK",
                value = (profile?.rankTitle ?: "Junior Cadet").uppercase(),
                modifier = Modifier.weight(1.4f)
            )
            RecordChip(
                label = "XP",
                value = "${profile?.experiencePoints ?: 0}",
                modifier = Modifier.weight(1f)
            )
            RecordChip(
                label = "MORALE",
                value = "${profile?.morale ?: 100}%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LIST OF SQUAD MEMBERS
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (member in MissionCatalog.SQUAD_DOSSIERS) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar Silhouette
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(member.uniformColor).copy(alpha = 0.35f))
                                .border(1.5.dp, Color(member.uniformColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(member.uniformColor),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = member.name.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = WeatheredIvory
                                )
                                Text(
                                    text = member.callsign,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SignalAmber
                                )
                            }

                            Text(
                                text = member.role,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CoolSteel
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = member.bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = WeatheredIvory.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "SPECIALTY: ${member.specialty}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(member.uniformColor)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            StandingMeter(
                                standing = standingFor(member, profile),
                                modifier = Modifier.testTag("roster_standing_${member.key}")
                            )
                        }
                    }
                }
            }
        }
    }
}

/** One squad relationship value read from the campaign profile. */
private data class Standing(
    val label: String,
    val value: Int,
    val color: Color,
    val isRivalry: Boolean
)

private fun standingFor(member: SquadMemberDossier, profile: CampaignProfileEntity?): Standing {
    val raw = when (member.key) {
        "voss" -> profile?.vossTrust
        "tomas" -> profile?.tomasSync
        "leena" -> profile?.leenaAffinity
        "idris" -> profile?.idrisRespect
        "kade" -> profile?.kadeRivalry
        else -> null
    } ?: 50
    val value = raw.coerceIn(0, 100)

    // Kade's column tracks rivalry, so a low number is his good outcome.
    if (member.key == "kade") {
        val label = when {
            value <= 25 -> "ALLIED"
            value <= 50 -> "COMPETITIVE"
            value <= 75 -> "HOSTILE"
            else -> "HUNTING YOU"
        }
        return Standing(label, value, if (value <= 50) SignalAmber else AlertRed, true)
    }

    val label = when {
        value >= 80 -> "TRUSTED"
        value >= 60 -> "RELIABLE"
        value >= 40 -> "NEUTRAL"
        value >= 20 -> "WARY"
        else -> "DISTRUSTFUL"
    }
    return Standing(label, value, if (value >= 60) SignalAmber else CoolSteel, false)
}

@Composable
private fun StandingMeter(standing: Standing, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (standing.isRivalry) "RIVALRY" else "STANDING",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = CoolSteel
            )
            Text(
                text = standing.label,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = standing.color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DeepGraphiteBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(standing.value / 100f)
                    .height(4.dp)
                    .background(standing.color)
            )
        }
    }
}

@Composable
private fun RecordChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DeepGraphiteElevated)
            .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = CoolSteel
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = WeatheredIvory,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
