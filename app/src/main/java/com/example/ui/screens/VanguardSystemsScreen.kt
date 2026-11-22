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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.VanguardContractAudit
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.gunsmith.BallisticStatAggregator
import com.example.game.vanguard.gunsmith.VanguardLoadouts
import com.example.game.vanguard.meta.BudgetAllocation
import com.example.game.vanguard.meta.PresidentialGovernance
import com.example.game.vanguard.narrative.DilemmaLibrary
import com.example.game.vanguard.progression.RankEntry
import com.example.game.vanguard.progression.RankLedger
import com.example.game.vanguard.progression.UniformTable
import com.example.game.vanguard.progression.VanguardRank
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/**
 * Section 1.2 step 5 / Section 2 src/UI.
 *
 * The presentation layer only reads domain values. Nothing on this screen
 * computes a rule of its own - the audit and the snapshots come straight from
 * the vanguard domain packages.
 */
@Composable
fun VanguardSystemsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { VanguardContractAudit.audit() }
    val career = remember { RankLedger.initial().record(RankEntry(VanguardRank.O5_LTC, 1_000L, "CAREER")) }
    val precision = remember { BallisticStatAggregator.assemble(VanguardLoadouts.PRECISION_308).valueOrNull() }
    val budget = remember { PresidentialGovernance.allocate(1_240f, BudgetAllocation()).valueOrNull() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("vanguard_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("vanguard_back_button")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "VANGUARD SYSTEMS",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "SECTION 1.2 / 3.1 - 3.4 CONTRACT GATE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = CoolSteel,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AuditGateCard(audit)
        Spacer(modifier = Modifier.height(12.dp))
        GroupBreakdownCard(audit)

        if (audit.failures.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FailureCard(audit)
        }

        Spacer(modifier = Modifier.height(12.dp))
        FieldDataCard(
            rank = career.current,
            rankTitle = UniformTable.forRank(career.current).detail,
            weaponLine = precision?.let {
                "%.2f kg  |  %.0f m/s  |  %.2f MOA  |  %d rpm".format(
                    it.massKg, it.muzzleVelocityMps, it.mechanicalMoa, it.hudRpm
                )
            } ?: "unavailable",
            dilemmaLine = "3-way dilemma  |  ${DilemmaLibrary.ALL.size} authored",
            budgetLine = budget?.let {
                "index %.1f  |  civil %.0f bn  |  R&D %.0f bn  |  fleets %.0f bn".format(
                    it.nationalDevelopmentIndex,
                    it.civilInfrastructureBillions,
                    it.defenseRndBillions,
                    it.fleetBillions
                )
            } ?: "unavailable"
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AuditGateCard(audit: ContractAudit) {
    val accent = if (audit.allPassed) RadarCyan else SignalAmber
    Card(
        modifier = Modifier.fillMaxWidth().testTag("vanguard_audit_gate"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (audit.allPassed) "CONTRACT GATE - CLEARED" else "CONTRACT GATE - FAILURES PRESENT",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${audit.passed} / ${audit.total}",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = WeatheredIvory
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "deterministic cases executed in-process on this device",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = CoolSteel
            )
        }
    }
}

@Composable
private fun GroupBreakdownCard(audit: ContractAudit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            audit.byGroup()
                .toList()
                .sortedBy { (group, _) ->
                    VanguardContractAudit.GROUP_ORDER.indexOf(group).takeIf { it >= 0 } ?: Int.MAX_VALUE
                }
                .forEach { (group, cases) ->
                val passed = cases.count { it.passed }
                val total = cases.size
                val ok = passed == total
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WeatheredIvory,
                        modifier = Modifier.width(96.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DeepGraphite)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (total == 0) 0f else passed.toFloat() / total)
                                .height(6.dp)
                                .background(if (ok) RadarCyan else SignalAmber)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$passed/$total",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (ok) RadarCyan else SignalAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun FailureCard(audit: ContractAudit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("vanguard_audit_failures"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "FAILED CASES",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SignalAmber
            )
            audit.failures.forEach { case ->
                Column {
                    Text(text = "${case.group}-${case.index} ${case.title}", fontSize = 13.sp, color = WeatheredIvory)
                    Text(
                        text = "expected ${case.expectation} | ${case.detail}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CoolSteel
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldDataCard(rank: VanguardRank, rankTitle: String, weaponLine: String, dilemmaLine: String, budgetLine: String) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("vanguard_field_data"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "FIELD DATA",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RadarCyan,
                letterSpacing = 1.2.sp
            )
            FieldRow("RANK", rank.display)
            FieldRow("GARRISON", rankTitle)
            FieldRow("PRECISION .308", weaponLine)
            FieldRow("NARRATIVE", dilemmaLine)
            FieldRow("PRESIDENCY", budgetLine)
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String) {
    Column {
        Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = CoolSteel)
        Text(text = value, fontSize = 12.sp, color = WeatheredIvory)
    }
}
