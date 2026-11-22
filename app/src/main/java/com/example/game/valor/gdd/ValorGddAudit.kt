package com.example.game.valor.gdd

import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.ContractCase

/**
 * The Valor Academy Game Design contract gate.
 *
 * Every group mirrors one section of `Valor_Academy_Game_Design.md` and runs the
 * shipping tables in-process, so a transcribed number that drifts fails on the
 * next launch instead of on a device.
 */
object ValorGddAudit {

    const val GROUP_STORY = "VALOR_GDD_STORY"
    const val GROUP_RANKS = "VALOR_GDD_RANKS"
    const val GROUP_MEDALS = "VALOR_GDD_MEDALS"
    const val GROUP_ARSENAL = "VALOR_GDD_ARSENAL"
    const val GROUP_SUPPORT = "VALOR_GDD_SUPPORT"
    const val GROUP_MODES = "VALOR_GDD_MODES"
    const val GROUP_AI = "VALOR_GDD_AI"
    const val GROUP_CONTROLS = "VALOR_GDD_CONTROLS"
    const val GROUP_ECONOMY = "VALOR_GDD_ECONOMY"
    const val GROUP_WORLD = "VALOR_GDD_WORLD"
    const val GROUP_PRODUCTION = "VALOR_GDD_PRODUCTION"
    const val GROUP_SCREENS = "VALOR_GDD_SCREENS"
    const val GROUP_ROADMAP = "VALOR_GDD_ROADMAP"

    val GROUP_ORDER: List<String> = listOf(
        GROUP_STORY,
        GROUP_RANKS,
        GROUP_MEDALS,
        GROUP_ARSENAL,
        GROUP_SUPPORT,
        GROUP_MODES,
        GROUP_AI,
        GROUP_CONTROLS,
        GROUP_ECONOMY,
        GROUP_WORLD,
        GROUP_PRODUCTION,
        GROUP_SCREENS,
        GROUP_ROADMAP
    )

    /** Which sections of the document each group enforces. */
    val GROUP_SECTIONS: Map<String, String> = mapOf(
        GROUP_STORY to "3",
        GROUP_RANKS to "4",
        GROUP_MEDALS to "4.1",
        GROUP_ARSENAL to "5",
        GROUP_SUPPORT to "6",
        GROUP_MODES to "7.1-7.3, 7.5",
        GROUP_AI to "7.4",
        GROUP_CONTROLS to "8",
        GROUP_ECONOMY to "9",
        GROUP_WORLD to "10",
        GROUP_PRODUCTION to "11.3, 12.4, 12.6",
        GROUP_SCREENS to "13",
        GROUP_ROADMAP to "14, 18"
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        GddStoryCases.run() +
            GddRankCases.run() +
            GddMedalCases.run() +
            GddArsenalCases.run() +
            GddSupportCases.run() +
            GddModeCases.run() +
            GddAiCases.run() +
            GddControlCases.run() +
            GddEconomyCases.run() +
            GddWorldCases.run() +
            GddProductionCases.run() +
            GddScreenCases.run() +
            GddRoadmapCases.run()
}
