package com.example.game.tactical

import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.ContractCase

/**
 * The Tactical Evolution contract gate.
 *
 * Mirrors `VanguardContractAudit`: every case runs the shipping domain code
 * in-process, so a mechanic that drifts from the specification fails here on
 * the next launch instead of on the device.
 */
object TacticalContractAudit {

    const val GROUP_RANK = "TACTICAL_RANK"
    const val GROUP_MEDAL = "TACTICAL_MEDALS"
    const val GROUP_STATE = "TACTICAL_STATE"
    const val GROUP_ACADEMY = "TACTICAL_ACADEMY"
    const val GROUP_COMBAT = "TACTICAL_COMBAT"
    const val GROUP_MOVEMENT = "TACTICAL_MOVEMENT"
    const val GROUP_AI = "TACTICAL_AI"
    const val GROUP_GUNSMITH = "TACTICAL_GUNSMITH"
    const val GROUP_CAMPAIGN = "TACTICAL_CAMPAIGN"
    const val GROUP_PRESENTATION = "TACTICAL_PRESENTATION"

    val GROUP_ORDER: List<String> = listOf(
        GROUP_RANK,
        GROUP_MEDAL,
        GROUP_STATE,
        GROUP_ACADEMY,
        GROUP_COMBAT,
        GROUP_MOVEMENT,
        GROUP_AI,
        GROUP_GUNSMITH,
        GROUP_CAMPAIGN,
        GROUP_PRESENTATION
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        TacticalCareerContractCases.run() +
            TacticalCombatContractCases.run() +
            TacticalMovementContractCases.run() +
            TacticalAiContractCases.run() +
            TacticalGunsmithContractCases.run() +
            TacticalWorldContractCases.run()
}
