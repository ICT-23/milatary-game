package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.tactical.TacticalContractAudit
import com.example.game.tactical.campaign.IronVanguardCampaign
import com.example.game.tactical.integration.LegacyBridge
import com.example.game.tactical.progression.CampaignAct
import com.example.game.tactical.progression.MedalEvaluator
import com.example.game.tactical.progression.MissionApproach
import com.example.game.tactical.progression.MissionRecord
import com.example.game.tactical.progression.RankLadder
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.components.TacticalSectionCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/** One decorated Act IV clearance, reused by the ribbon wall sample. */
private val SEED_RECORD = MissionRecord(
    missionId = "A4-03",
    act = CampaignAct.ACT_IV,
    survived = true,
    accuracyPercent = 91f,
    headshots = 7,
    lowestHealthPercent = 26,
    traumaKitsApplied = 0,
    approach = MissionApproach.STEALTH,
    alarmTriggered = false,
    squadmatesSaved = 4,
    squadmatesTotal = 4
)

/**
 * Phases 1 - 6 presentation layer: the career ladder, the honours wall, the
 * four-act campaign board and the live contract gate. The screen computes no
 * rule of its own - every figure is read back from the tactical domain packages.
 */
@Composable
fun TacticalEvolutionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { TacticalContractAudit.audit() }
    val candidate = remember { LegacyBridge.seed("VANCE", 12_000, 4, "SGT") }
    val nextRung = remember { RankLadder.eligibility(candidate.xp, candidate.satisfied) }
    val progress = remember { RankLadder.progressToNext(candidate.xp, candidate.rankIndex) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("tactical_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("tactical_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "TACTICAL EVOLUTION",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                MonoLabel(text = "FORT VALOR / OPERATION IRON VANGUARD", size = 10)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TacticalSectionCard("CAREER PROJECTION", "PHASE 1 - GAMESTATE MANAGER", RadarCyan) {
            TacticalDetailRow("OPERATOR", candidate.callsign)
            TacticalDetailRow("HELD RUNG", candidate.rank.display, SignalAmber)
            TacticalDetailRow("EXPERIENCE", "${candidate.xp} XP")
            TacticalDetailRow("CREDITS", candidate.credits.toString())
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DeepGraphiteBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .background(SignalAmber)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            TacticalDetailRow("NEXT RUNG", nextRung.tier.display)
            TacticalDetailRow(
                "BOARD",
                nextRung.missingRequirement?.label ?: "ALL BOARDS CLEARED",
                if (nextRung.missingRequirement == null) RadarCyan else AlertRed
            )
            TacticalDetailRow("XP STILL OWED", nextRung.xpMissing.toString(), CoolSteel)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("RANK LADDER", "SECTION 3 - XP AND QUALIFICATION", SignalAmber) {
            RankLadder.TIERS.forEach { tier ->
                val held = tier.index == candidate.rankIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonoLabel(
                        text = tier.code,
                        modifier = Modifier.width(40.dp),
                        size = 10,
                        color = if (held) SignalAmber else CoolSteel,
                        weight = FontWeight.Bold
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tier.title,
                            fontSize = 11.sp,
                            color = if (held) WeatheredIvory else CoolSteel
                        )
                        MonoLabel(text = "${tier.requirement.label} / ${tier.xpRequired} XP", size = 8)
                    }
                    MonoLabel(
                        text = if (tier.requirement in candidate.satisfied) "CLEARED" else "PENDING",
                        size = 8,
                        color = if (tier.requirement in candidate.satisfied) RadarCyan else AlertRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("HONOURS WALL", "SECTION 3 - RIBBON PREDICATES", RadarCyan) {
            MedalEvaluator.ribbonWall(SEED_RECORD).forEach { (medal, earned) ->
                Column(modifier = Modifier.padding(vertical = 3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = medal.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (earned) WeatheredIvory else CoolSteel,
                            modifier = Modifier.weight(1f)
                        )
                        MonoLabel(
                            text = if (earned) "AWARDED" else "OWED",
                            size = 8,
                            color = if (earned) SignalAmber else CoolSteel
                        )
                    }
                    MonoLabel(text = medal.citation, size = 8)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard(
            "CAMPAIGN BOARD",
            "SECTION 2 - ${IronVanguardCampaign.CODENAME.uppercase()}",
            SignalAmber
        ) {
            CampaignAct.entries.forEach { act ->
                val missions = IronVanguardCampaign.forAct(act)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonoLabel(
                        text = "ACT ${act.number} / ${act.title}",
                        modifier = Modifier.weight(1f),
                        color = SignalAmber,
                        weight = FontWeight.Bold
                    )
                    MonoLabel(text = "${missions.sumOf { it.xpReward }} XP")
                }
                missions.forEach { brief ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MonoLabel(text = brief.id, modifier = Modifier.width(46.dp))
                        Text(
                            text = brief.title,
                            fontSize = 10.sp,
                            color = WeatheredIvory,
                            modifier = Modifier.weight(1f)
                        )
                        MonoLabel(
                            text = if (brief.requiresRankIndex > 0) {
                                RankLadder.tierAt(brief.requiresRankIndex).code
                            } else {
                                "OPEN"
                            },
                            size = 8
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            TacticalDetailRow("ENEMY", IronVanguardCampaign.ENEMY_FACTION, AlertRed)
            TacticalDetailRow("ACADEMY", IronVanguardCampaign.ACADEMY)
            TacticalDetailRow(
                "TOTAL REWARD",
                "${IronVanguardCampaign.MISSIONS.sumOf { it.xpReward }} XP",
                SignalAmber
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard("LEGACY BRIDGE", "PHASE 1 - NON-DESTRUCTIVE MIGRATION", CoolSteel) {
            TacticalDetailRow(
                "LEGACY SGT",
                RankLadder.tierAt(LegacyBridge.toTacticalRankIndex(4, "SGT")).display
            )
            TacticalDetailRow(
                "MERIT CEILING",
                "${LegacyBridge.LEGACY_MAX_MERIT} MP = ${LegacyBridge.TACTICAL_MAX_XP} XP"
            )
            TacticalDetailRow(
                "CLEARANCE KEPT",
                "${LegacyBridge.toLegacyClearance(4)} legacy rungs"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        TacticalSectionCard(
            "CONTRACT GATE",
            if (audit.allPassed) "ALL CONTRACTS HOLD" else "${audit.failures.size} FAILURES",
            if (audit.allPassed) RadarCyan else AlertRed
        ) {
            TacticalDetailRow("CASES", "${audit.passed} / ${audit.total}")
            val grouped = audit.byGroup()
            TacticalContractAudit.GROUP_ORDER.forEach { group ->
                val cases = grouped[group] ?: return@forEach
                TacticalDetailRow(
                    group,
                    "${cases.count { it.passed }} / ${cases.size}",
                    if (cases.all { it.passed }) RadarCyan else AlertRed
                )
            }
            audit.failures.forEach { failure ->
                TacticalDetailRow(failure.title, failure.detail, AlertRed)
            }
        }
    }
}
