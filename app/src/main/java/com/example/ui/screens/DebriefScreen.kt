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
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.MissionRating
import com.example.ui.MissionResult
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.FieldOlive
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun DebriefScreen(
    result: MissionResult,
    onContinue: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(24.dp)
            .testTag("debrief_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BADGE
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SignalAmber.copy(alpha = 0.2f))
                    .border(2.dp, SignalAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = SignalAmber,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MISSION ACCOMPLISHED",
                style = MaterialTheme.typography.headlineMedium,
                color = WeatheredIvory,
                letterSpacing = 2.sp
            )

            Text(
                text = "AFTER-ACTION REPORT // CHAPTER ${result.chapterNumber}: ${result.missionTitle.uppercase()}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = SignalAmber,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // The AAR is step six of the six-stage core loop.
            Text(
                text = "CORE LOOP 6/6 · AFTER-ACTION REPORT & OPERATIVE PROMOTION",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = CoolSteel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CENTER STATS & REWARDS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Star Rating
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) { index ->
                            val earned = index < result.stars
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (earned) SignalAmber else CoolSteel.copy(alpha = 0.3f),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val mins = result.elapsedSeconds / 60
                    val secs = result.elapsedSeconds % 60
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCell(label = "TIME", value = String.format("%02d:%02d", mins, secs), color = WeatheredIvory)
                        StatCell(label = "SCORE", value = result.score.toString(), color = RadarCyan)
                        StatCell(label = "HEALTH", value = "${result.healthPercent}%", color = SignalAmber)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCell(label = "RATING", value = MissionRating.titleFor(result.stars), color = RadarCyan)
                        StatCell(label = "EXPERIENCE", value = "+${result.awardedXp} XP", color = SignalAmber)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Section 6.4: transparent grade with the reason it was awarded.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MISSION GRADE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CoolSteel
                            )
                            Text(
                                text = result.grade.letter,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                color = SignalAmber
                            )
                            Text(
                                text = result.grade.label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = CoolSteel
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            StatCell(label = "CREDITS", value = "+${result.serviceCreditsGained}", color = RadarCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            StatCell(label = "TOKENS", value = "+${result.commendationTokensGained}", color = RadarCyan)
                        }
                    }

                    if (result.gradeReasons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "GRADE BREAKDOWN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = CoolSteel
                            )
                            result.gradeReasons.forEach { reason ->
                                Text(
                                    text = "\u00b7 $reason",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = WeatheredIvory
                                )
                            }
                        }
                    }

                    if (result.awardsEarned.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SignalAmber.copy(alpha = 0.12f))
                                .border(1.dp, SignalAmber, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .testTag("awards_earned_block")
                        ) {
                            Text(
                                text = "COMMENDATIONS EARNED",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = SignalAmber
                            )
                            result.awardsEarned.forEach { award ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\u2605 ${award.title}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = WeatheredIvory
                                )
                                Text(
                                    text = award.condition,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp,
                                    color = CoolSteel
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tactical Choice Outcome
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepGraphite)
                            .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "COMMAND DISPATCH RECORD:",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = SignalAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val choice = result.choice
                            if (choice == null) {
                                Text(
                                    text = "No field decision was logged for this operation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoolSteel,
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    text = choice.label,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = WeatheredIvory
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = choice.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WeatheredIvory,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${choice.speaker}: \"${choice.outcome}\"",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp,
                                    color = CoolSteel
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rank / next chapter banner
                    val promoted = result.promotedRank
                    if (promoted != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SignalAmber.copy(alpha = 0.16f))
                                .border(1.dp, SignalAmber, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("promotion_banner"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = SignalAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PROMOTION AWARDED",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = SignalAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${result.previousRankTitle.uppercase()}  ->  ${promoted.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = WeatheredIvory,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FieldOlive.copy(alpha = 0.25f))
                                .border(1.dp, FieldOlive, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = SignalAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RANK HELD: ${result.rankTitle.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = WeatheredIvory
                            )
                        }
                    }

                    if (result.intelGained > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+${result.intelGained} INTEL SECURED // CIPHER CACHE LOGGED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = RadarCyan
                        )
                    }

                    if (result.promotionOutstanding.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "NEXT RANK REQUIREMENTS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = CoolSteel
                            )
                            result.promotionOutstanding.forEach { requirement ->
                                Text(
                                    text = "\u00b7 $requirement",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    lineHeight = 13.sp,
                                    color = WeatheredIvory
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (result.nextMissionTitle != null) {
                        Text(
                            text = "NEXT OPERATION: ${result.nextMissionTitle.uppercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = RadarCyan
                        )
                    } else {
                        // Final chapter: the campaign closes on its own banner.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SignalAmber.copy(alpha = 0.14f))
                                .border(1.dp, SignalAmber, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("campaign_complete_banner"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "OPERATION EMBERWATCH // COMPLETE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.5.sp,
                                color = SignalAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Section 3.7: the ending the campaign actually earned.
                            val ending = result.ending
                            if (ending != null) {
                                Text(
                                    text = "ENDING // ${ending.title.uppercase()}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = RadarCyan,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ending.description,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp,
                                    color = WeatheredIvory,
                                    textAlign = TextAlign.Center
                                )
                                if (ending.hidden) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "HIDDEN OUTCOME UNLOCKED",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = SignalAmber
                                    )
                                }
                            } else {
                                Text(
                                    text = "ALL SEVEN CHAPTERS CLEARED. SPECTRE-7 STANDS WATCH.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp,
                                    color = WeatheredIvory,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BOTTOM ACTIONS
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("debrief_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SignalAmber,
                    contentColor = DeepGraphite
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "PROCEED TO OPERATIONS BOARD",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }

            OutlinedButton(
                onClick = onReplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("debrief_replay_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DeepGraphiteElevated.copy(alpha = 0.8f),
                    contentColor = CoolSteel
                )
            ) {
                Text(
                    text = "REPLAY CHAPTER FOR A BETTER RATING",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CoolSteel)
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
