package com.example.game.valor.balance

import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.ContractCase

/**
 * The Content & Balance Data contract gate.
 *
 * Every group mirrors one section of `Valor_Academy_Content_and_Balance_Data.md`
 * and runs the shipping tables in-process, so a transcribed number that drifts
 * fails on the next launch instead of on a device.
 */
object BalanceAudit {

    const val GROUP_COMBAT = "BAL_COMBAT"
    const val GROUP_WEAPONS = "BAL_WEAPONS"
    const val GROUP_GUNSMITH = "BAL_GUNSMITH"
    const val GROUP_GEAR = "BAL_GEAR"
    const val GROUP_ENEMIES = "BAL_ENEMIES"
    const val GROUP_DIFFICULTY = "BAL_DIFFICULTY"
    const val GROUP_RANKS = "BAL_RANKS"
    const val GROUP_SKILLS = "BAL_SKILLS"
    const val GROUP_ECONOMY = "BAL_ECONOMY"
    const val GROUP_MISSIONS = "BAL_MISSIONS"
    const val GROUP_NPCS = "BAL_NPCS"
    const val GROUP_SCHEMAS = "BAL_SCHEMAS"

    val GROUP_ORDER: List<String> = listOf(
        GROUP_COMBAT,
        GROUP_WEAPONS,
        GROUP_GUNSMITH,
        GROUP_GEAR,
        GROUP_ENEMIES,
        GROUP_DIFFICULTY,
        GROUP_RANKS,
        GROUP_SKILLS,
        GROUP_ECONOMY,
        GROUP_MISSIONS,
        GROUP_NPCS,
        GROUP_SCHEMAS
    )

    /** Which sections of the document each group enforces. */
    val GROUP_SECTIONS: Map<String, String> = mapOf(
        GROUP_COMBAT to "1, 4.3",
        GROUP_WEAPONS to "2.1-2.5",
        GROUP_GUNSMITH to "2.6-2.7",
        GROUP_GEAR to "3",
        GROUP_ENEMIES to "4.1, 4.4",
        GROUP_DIFFICULTY to "4.2",
        GROUP_RANKS to "5",
        GROUP_SKILLS to "5.1",
        GROUP_ECONOMY to "6",
        GROUP_MISSIONS to "7",
        GROUP_NPCS to "8",
        GROUP_SCHEMAS to "9"
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        BalanceCombatCases.run() +
            BalanceWeaponCases.run() +
            BalanceLoadoutCases.run() +
            BalanceEnemyCases.run() +
            BalanceProgressionCases.run() +
            BalanceEconomyCases.run() +
            BalanceContentCases.run()
}
