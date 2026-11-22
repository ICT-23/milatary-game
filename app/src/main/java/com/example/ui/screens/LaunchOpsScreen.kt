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
import com.example.game.valor.launch.LaunchOpsAudit
import com.example.game.vanguard.ContractAudit
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.WeatheredIvory

/**
 * Sections 1 - 13 of the Setup, Launch & Live-Ops document as a live read-out.
 *
 * Every figure is read back from `com.example.game.valor.launch`, and the footer
 * runs the ten-group contract gate that guards those tables.
 */
@Composable
fun LaunchOpsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { LaunchOpsAudit.audit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("launch_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("launch_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "SETUP, LAUNCH & LIVE-OPS",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                MonoLabel(text = "VALOR ACADEMY - SHIP PLAN AND OPERATIONS", size = 10)
            }
        }

        LaunchEnvironmentSection()
        LaunchRepositorySection()
        LaunchBuildSection()
        LaunchAnalyticsSection()
        LaunchMonetizationSection()
        LaunchPrivacySection()
        LaunchFtueSection()
        LaunchStoreSection()
        LaunchQaSection()
        LaunchLiveOpsSection()
        LaunchProgramSection()
        LaunchGateSection(audit)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** The ten-group gate over every table above. */
@Composable
private fun LaunchGateSection(audit: ContractAudit) {
    GddSection(
        "CONTRACT GATE",
        if (audit.allPassed) "ALL CONTRACTS HOLD" else "${audit.failures.size} FAILURES",
        if (audit.allPassed) RadarCyan else AlertRed
    ) {
        TacticalDetailRow("CASES", "${audit.passed} / ${audit.total}")
        val grouped = audit.byGroup()
        LaunchOpsAudit.groups().forEach { group ->
            val cases = grouped[group] ?: return@forEach
            TacticalDetailRow(
                group,
                "${cases.count { it.passed }} / ${cases.size} - section " +
                    LaunchOpsAudit.sectionFor(group),
                if (cases.all { it.passed }) RadarCyan else AlertRed
            )
        }
        audit.failures.forEach { failure ->
            TacticalDetailRow(failure.title, failure.detail, AlertRed)
        }
        MonoLabel(text = "EVERY CASE RUNS THE SHIPPING TABLE IN-PROCESS", size = 8)
    }
}
