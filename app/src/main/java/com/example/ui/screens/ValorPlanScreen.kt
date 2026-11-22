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
import com.example.game.valor.plan.AdPlacement
import com.example.game.valor.plan.BuildStatus
import com.example.game.valor.plan.ChangelogSpec
import com.example.game.valor.plan.DebugTools
import com.example.game.valor.plan.DefinitionOfDone
import com.example.game.valor.plan.DroneType
import com.example.game.valor.plan.EconomyService
import com.example.game.valor.plan.EdgeCases
import com.example.game.valor.plan.EndingRules
import com.example.game.valor.plan.EnemyAiRules
import com.example.game.valor.plan.GunsmithRules
import com.example.game.valor.plan.HitZone
import com.example.game.valor.plan.Localization
import com.example.game.valor.plan.LogCategory
import com.example.game.valor.plan.MissionState
import com.example.game.valor.plan.PerformancePlan
import com.example.game.valor.plan.PhysicsLayer
import com.example.game.valor.plan.PlanEventType
import com.example.game.valor.plan.PlanPhases
import com.example.game.valor.plan.ProgressionRules
import com.example.game.valor.plan.SaveFiles
import com.example.game.valor.plan.SaveMigrations
import com.example.game.valor.plan.SquadRules
import com.example.game.valor.plan.StealthRules
import com.example.game.valor.plan.ValorPlanAudit
import com.example.game.vanguard.ContractAudit
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

/**
 * Sections 2 - 12 of the Implementation Plan as a live read-out.
 *
 * Every figure is read back from `com.example.game.valor.plan`, and the footer
 * runs the ten-group contract gate that guards those tables.
 */
@Composable
fun ValorPlanScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audit = remember { ValorPlanAudit.audit() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("plan_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("plan_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WeatheredIvory)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "IMPLEMENTATION PLAN",
                    style = MaterialTheme.typography.titleLarge,
                    color = WeatheredIvory,
                    letterSpacing = 1.5.sp
                )
                MonoLabel(text = "VALOR ACADEMY - SYSTEMS, RULES, PHASES", size = 10)
            }
        }

        PlanStatusSection()
        PlanFoundationSection()
        PlanFieldSection()
        PlanCareerSection()
        PlanSystemSection()
        PlanPhaseSection()
        PlanQualitySection()
        PlanGateSection(audit)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** Section 2 and 3: what exists today, what is protected and what is unresolved. */
@Composable
private fun PlanStatusSection() {
    GddSection("BUILD STATUS", "SECTION 2 - INVENTORY, PROTECTED FILES", SignalAmber) {
        BuildStatus.SYSTEMS.forEach { row ->
            TacticalDetailRow(row.system, "${row.status.displayName} - ${row.files}")
        }
        MonoLabel(text = "PROTECTED", size = 8, color = RadarCyan)
        BuildStatus.PROTECTED.forEach { asset -> MonoLabel(text = asset.path) }
        MonoLabel(text = "OPEN QUESTIONS", size = 8, color = AlertRed)
        BuildStatus.OPEN_QUESTIONS.forEach { question -> MonoLabel(text = question) }
        MonoLabel(text = "NEXT TASK", size = 8, color = RadarCyan)
        BuildStatus.NEXT_TASK.forEach { task -> MonoLabel(text = task) }
        TacticalDetailRow("CHANGELOG", ChangelogSpec.sections().joinToString(" / "))
    }
}

/** Section 4 and 5.1 - 5.6: the shared vocabulary and the bullet rules. */
@Composable
private fun PlanFoundationSection() {
    GddSection("LOGIC FOUNDATION", "SECTION 4 - LAYERS, TEAMS, DAMAGE INFO", RadarCyan) {
        TacticalDetailRow("PHYSICS LAYERS", PhysicsLayer.entries.joinToString { it.displayName })
        TacticalDetailRow("TEAMS", "Player and Ally share one side of the bullet mask")
        HitZone.entries.forEach { zone ->
            TacticalDetailRow(zone.displayName, "x${zone.multiplier} damage")
        }
        TacticalDetailRow("EVENTS", "${PlanEventType.entries.size} bus messages")
        TacticalDetailRow("LOG TAGS", LogCategory.entries.joinToString(" ") { it.tag })
        TacticalDetailRow(
            "AI PERCEPTION",
            "${EnemyAiRules.VISION_CONE_DEGREES.toInt()} deg / " +
                "${EnemyAiRules.VISION_RANGE_METERS.toInt()} m / " +
                "${EnemyAiRules.MEMORY_SECONDS.toInt()} s memory"
        )
        TacticalDetailRow("SQUAD", "${SquadRules.maxSize(14)} allies at the top rank")
        TacticalDetailRow("STEALTH", "meter ${StealthRules.METER_MIN}-${StealthRules.METER_MAX}")
    }
}

/** Section 5.13 - 5.15: drones, the mission machine and tournaments. */
@Composable
private fun PlanFieldSection() {
    GddSection("OPERATIONS", "SECTION 5.13 - 5.15 - DRONES, MISSIONS, TOURNAMENT", RadarCyan) {
        DroneType.entries.forEach { drone ->
            TacticalDetailRow(drone.displayName, "unlocks at rank ${drone.unlockRank}")
        }
        TacticalDetailRow(
            "MISSION STATES",
            MissionState.entries.joinToString { it.displayName }
        )
    }
}

/** Section 5.16 - 5.18: XP, ranks, the economy and the gunsmith. */
@Composable
private fun PlanCareerSection() {
    GddSection("CAREER & ECONOMY", "SECTION 5.16 - 5.18 - XP, CASH, GUNSMITH", SignalAmber) {
        TacticalDetailRow("KILL XP", "${ProgressionRules.KILL_XP_RANGE.first}-${ProgressionRules.KILL_XP_RANGE.last}")
        TacticalDetailRow("BONUS XP", "${ProgressionRules.BONUS_XP_RANGE.first}-${ProgressionRules.BONUS_XP_RANGE.last}")
        TacticalDetailRow("BALANCE CEILING", "${EconomyService.MAX_BALANCE}")
        TacticalDetailRow("GUNSMITH", "levels ${GunsmithRules.MIN_LEVEL}-${GunsmithRules.MAX_LEVEL}, no skipping")
    }
}

/** Section 5.19 - 5.22: save, story, HUD and ads. */
@Composable
private fun PlanSystemSection() {
    GddSection("SYSTEMS", "SECTION 5.19 - 5.22 - SAVE, STORY, HUD, ADS", RadarCyan) {
        TacticalDetailRow(
            "SAVE",
            "v${SaveMigrations.CURRENT_VERSION} in ${SaveFiles.ALL.size} files, atomic write"
        )
        TacticalDetailRow(
            "STORY",
            "${EndingRules.reachable().size} endings reachable from the meter presets"
        )
        TacticalDetailRow("HUD", "${Localization.ENTRIES.size} localized labels")
        TacticalDetailRow("ADS", AdPlacement.entries.joinToString { it.displayName })
    }
}

/** Section 6: the fifteen phases and their gates. */
@Composable
private fun PlanPhaseSection() {
    GddSection("BUILD PHASES", "SECTION 6 - PHASE 0 TO 14", SignalAmber) {
        PlanPhases.PHASES.forEach { phase ->
            TacticalDetailRow(
                "PHASE ${phase.number}" + (phase.milestone?.let { " - $it" } ?: ""),
                phase.acceptance.first()
            )
        }
    }
}

/** Sections 7 - 10 and 12: the tests, the budgets, the edge cases and the DoD. */
@Composable
private fun PlanQualitySection() {
    GddSection("QUALITY GATES", "SECTION 7 - 10 AND 12", RadarCyan) {
        PerformancePlan.BUDGETS.forEach { budget ->
            TacticalDetailRow(
                budget.tier.uppercase(),
                "${budget.fps} FPS, ${budget.drawCallsMax} calls, " +
                    "${budget.trianglesMax} tris, ${budget.activeAi} AI"
            )
        }
        TacticalDetailRow("EDGE CASES", "${EdgeCases.ROWS.size} known bugs guarded")
        TacticalDetailRow(
            "DEBUG",
            "${DebugTools.PANEL_READOUTS.size} readouts, ${DebugTools.CHEATS.size} cheats, " +
                "${DebugTools.GIZMOS.size} gizmos"
        )
        TacticalDetailRow("DEFINITION OF DONE", "${DefinitionOfDone.ITEMS.size} conditions")
    }
}

/** The ten-group gate over every table above. */
@Composable
private fun PlanGateSection(audit: ContractAudit) {
    GddSection(
        "CONTRACT GATE",
        if (audit.allPassed) "ALL CONTRACTS HOLD" else "${audit.failures.size} FAILURES",
        if (audit.allPassed) RadarCyan else AlertRed
    ) {
        TacticalDetailRow("CASES", "${audit.passed} / ${audit.total}")
        val grouped = audit.byGroup()
        ValorPlanAudit.groups().forEach { group ->
            val cases = grouped[group] ?: return@forEach
            TacticalDetailRow(
                group,
                "${cases.count { it.passed }} / ${cases.size} - section " +
                    ValorPlanAudit.sectionFor(group),
                if (cases.all { it.passed }) RadarCyan else AlertRed
            )
        }
        audit.failures.forEach { failure ->
            TacticalDetailRow(failure.title, failure.detail, AlertRed)
        }
        MonoLabel(text = "EVERY CASE RUNS THE SHIPPING TABLE IN-PROCESS", size = 8)
    }
}
