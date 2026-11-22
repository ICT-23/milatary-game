package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.vanguard.ContractAudit
import com.example.game.valor.gdd.ValorGddAudit
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.WeatheredIvory

/**
 * Sections 3 - 18 of the Game Design Document as a live read-out.
 *
 * Every figure is read back from `com.example.game.valor.gdd`; the section bodies
 * live in [GddSections.kt], [GddSystemSections.kt] and [GddProductionSections.kt],
 * and the footer runs the thirteen-group contract gate that guards those tables.
 */
@Composable
fun ValorGddScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { ValorGddAudit.audit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("gdd_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("gdd_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "GAME DESIGN DOCUMENT",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                MonoLabel(
                    text = "VALOR ACADEMY - STORY, RANKS, ARSENAL, MODES, ECONOMY",
                    size = 10
                )
            }
        }

        GddStorySection()
        GddRankSection()
        GddMedalSection()
        GddArsenalSection()
        GddSupportSection()
        GddModeSection()
        GddAiSection()
        GddControlSection()
        GddEconomySection()
        GddWorldSection()
        GddProductionSection()
        GddScreenFlowSection()
        GddRoadmapSection()
        GddGateSection(audit)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** The thirteen-group gate over every table above. */
@Composable
private fun GddGateSection(audit: ContractAudit) {
    GddSection(
        "CONTRACT GATE",
        if (audit.allPassed) "ALL CONTRACTS HOLD" else "${audit.failures.size} FAILURES",
        if (audit.allPassed) RadarCyan else AlertRed
    ) {
        TacticalDetailRow("CASES", "${audit.passed} / ${audit.total}")
        val grouped = audit.byGroup()
        ValorGddAudit.GROUP_ORDER.forEach { group ->
            val cases = grouped[group] ?: return@forEach
            TacticalDetailRow(
                group,
                "${cases.count { it.passed }} / ${cases.size} - section " +
                    (ValorGddAudit.GROUP_SECTIONS[group] ?: "-"),
                if (cases.all { it.passed }) RadarCyan else AlertRed
            )
        }
        audit.failures.forEach { failure ->
            TacticalDetailRow(failure.title, failure.detail, AlertRed)
        }
        MonoLabel(text = "EVERY CASE RUNS THE SHIPPING TABLE IN-PROCESS", size = 8)
    }
}
