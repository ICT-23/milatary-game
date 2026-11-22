package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.game.valor.balance.BalanceAudit
import com.example.game.valor.balance.CrateCatalog
import com.example.game.valor.balance.FieldChecklists
import com.example.game.valor.balance.LootRarity
import com.example.game.valor.balance.MedalEconomy
import com.example.game.valor.balance.MedalSource
import com.example.game.valor.balance.MissionCatalog
import com.example.game.valor.balance.MissionIncome
import com.example.game.valor.balance.MissionType
import com.example.game.valor.balance.NpcCatalog
import com.example.game.valor.balance.RankTable
import com.example.game.valor.balance.Sink
import com.example.game.valor.balance.SkillBranch
import com.example.game.valor.balance.SkillTreeData
import com.example.game.vanguard.ContractAudit
import com.example.ui.components.BalanceContractLine
import com.example.ui.components.BalanceMissionLine
import com.example.ui.components.BalanceNpcLine
import com.example.ui.components.BalanceRankLine
import com.example.ui.components.BalanceSkillLine
import com.example.ui.components.MonoLabel
import com.example.ui.components.TacticalDetailRow
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber

/** Section 5: the v2 rank curve and its act bands. */
@Composable
internal fun BalanceRankSection() {
    BalanceSection("RANKS", "SECTION 5 - V2 CURVE", RadarCyan) {
        TacticalDetailRow(
            "RUNGS",
            "${RankTable.RANKS.size} to ${RankTable.byIndex(19)?.cumulativeXp} XP"
        )
        TacticalDetailRow("SKILL POINTS", RankTable.RANKS.sumOf { it.skillPoints }.toString())
        (1..6).forEach { act ->
            val band = RankTable.forAct(act)
            TacticalDetailRow(
                "ACT $act",
                "${RankTable.actXpSpan(act)} XP / ranks ${band.first().index}-${band.last().index}"
            )
        }
        RankTable.RANKS.forEach { BalanceRankLine(it) }
    }
}

/** Section 5.1: four branches, sixteen skills. */
@Composable
internal fun BalanceSkillSection() {
    BalanceSection("SKILL TREE", "SECTION 5.1 - 16 SKILLS", CoolSteel) {
        TacticalDetailRow(
            "TREE",
            "${SkillTreeData.SKILLS.size} skills / ${SkillTreeData.MAX_TOTAL_LEVELS} levels / " +
                "${SkillTreeData.TOTAL_POINTS_AWARDED} points"
        )
        SkillBranch.entries.forEach { branch ->
            TacticalDetailRow(branch.displayName.uppercase(), SkillTreeData.ofBranch(branch).size.toString())
            SkillTreeData.ofBranch(branch).forEach { BalanceSkillLine(it) }
        }
    }
}

/** Section 6: income bands, medals, sinks and crates. */
@Composable
internal fun BalanceEconomySection() {
    BalanceSection("ECONOMY", "SECTION 6 - INCOME, MEDALS, CRATES", SignalAmber) {
        MissionIncome.BANDS.forEach { band ->
            TacticalDetailRow(
                "ACT ${band.act}",
                "${band.cash.first}-${band.cash.last} cash / ${band.steel.first}-${band.steel.last} steel / " +
                    "${band.parts.first}-${band.parts.last} parts"
            )
        }
        TacticalDetailRow("BONUSES", "+20% objective, +25% at three stars, then the difficulty multiplier")
        TacticalDetailRow(
            "MEDALS",
            "${MedalSource.entries.size} sources / revive ${MedalEconomy.REVIVE_TOKEN_PRICE} / " +
                "crate ${MedalEconomy.CRATE_PRICE}"
        )
        TacticalDetailRow("SINKS", "${Sink.entries.size} always open")
        CrateCatalog.CRATES.forEach { crate ->
            TacticalDetailRow(crate.name.uppercase(), crate.bonusItems.joinToString(", "))
        }
        TacticalDetailRow(
            "ODDS",
            LootRarity.entries.joinToString(" / ") { "${it.displayName} ${it.chancePercent}%" }
        )
        TacticalDetailRow("PITY", "epic within ${CrateCatalog.PITY_TIMER} crates")
    }
}

/** Section 7: all sixty missions, act by act, plus the side content. */
@Composable
internal fun BalanceMissionSection() {
    BalanceSection("MISSIONS", "SECTION 7 - 60 MISSIONS", RadarCyan) {
        TacticalDetailRow("TOTAL", "${MissionCatalog.MISSIONS.size} across ${MissionCatalog.ACT_COUNT} acts")
        TacticalDetailRow("TYPES", MissionType.entries.joinToString(" ") { it.code })
        (1..6).forEach { act ->
            TacticalDetailRow(
                "ACT $act",
                "${MissionCatalog.baseXpTotal(act)} base XP / ${MissionCatalog.cashTotal(act)} cash"
            )
            MissionCatalog.forAct(act).forEach { BalanceMissionLine(it) }
        }
        MissionCatalog.SIDE_CONTENT.forEach { side ->
            TacticalDetailRow(side.name.uppercase(), side.rewards)
        }
    }
}

/** Section 8: the six story characters. */
@Composable
internal fun BalanceNpcSection() {
    BalanceSection("CHARACTERS", "SECTION 8 - NPC COMBAT STATS", CoolSteel) {
        NpcCatalog.NPCS.forEach { BalanceNpcLine(it) }
    }
}

/** Section 9: the four schema checklists and what is still unauthored. */
@Composable
internal fun BalanceContractSection() {
    BalanceSection("DATA CONTRACTS", "SECTION 9 - FIELD CHECKLISTS", SignalAmber) {
        FieldChecklists.ALL.forEach { BalanceContractLine(it) }
    }
}

/** The twelve-group gate over every table above. */
@Composable
internal fun BalanceGateSection(audit: ContractAudit) {
    BalanceSection(
        "CONTRACT GATE",
        if (audit.allPassed) "ALL CONTRACTS HOLD" else "${audit.failures.size} FAILURES",
        if (audit.allPassed) RadarCyan else AlertRed
    ) {
        TacticalDetailRow("CASES", "${audit.passed} / ${audit.total}")
        val grouped = audit.byGroup()
        BalanceAudit.GROUP_ORDER.forEach { group ->
            val cases = grouped[group] ?: return@forEach
            TacticalDetailRow(
                group,
                "${cases.count { it.passed }} / ${cases.size} - section " +
                    (BalanceAudit.GROUP_SECTIONS[group] ?: "-"),
                if (cases.all { it.passed }) RadarCyan else AlertRed
            )
        }
        audit.failures.forEach { failure ->
            TacticalDetailRow(failure.title, failure.detail, AlertRed)
        }
        MonoLabel(text = "EVERY CASE RUNS THE SHIPPING TABLE IN-PROCESS", size = 8)
    }
}
