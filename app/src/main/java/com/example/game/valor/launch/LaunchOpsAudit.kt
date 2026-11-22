package com.example.game.valor.launch

import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.ContractCase

/**
 * The Setup, Launch & Live-Ops contract gate.
 *
 * Each group mirrors one slice of `Valor_Academy_Launch_and_LiveOps.md` and runs
 * the shipping tables in-process, so a transcribed number that drifts fails on
 * the next launch instead of on upload day.
 */
object LaunchOpsAudit {

    const val GROUP_DEV = "LAUNCH_DEV"
    const val GROUP_BUILD = "LAUNCH_BUILD"
    const val GROUP_ANALYTICS = "LAUNCH_ANALYTICS"
    const val GROUP_MONETIZATION = "LAUNCH_MONETIZATION"
    const val GROUP_PRIVACY = "LAUNCH_PRIVACY"
    const val GROUP_FTUE = "LAUNCH_FTUE"
    const val GROUP_STORE = "LAUNCH_STORE"
    const val GROUP_QA = "LAUNCH_QA"
    const val GROUP_LIVEOPS = "LAUNCH_LIVEOPS"
    const val GROUP_RISK = "LAUNCH_RISK"

    /** Display order used by the read-out screen, so groups never reshuffle. */
    val GROUP_ORDER: List<String> = listOf(
        GROUP_DEV,
        GROUP_BUILD,
        GROUP_ANALYTICS,
        GROUP_MONETIZATION,
        GROUP_PRIVACY,
        GROUP_FTUE,
        GROUP_STORE,
        GROUP_QA,
        GROUP_LIVEOPS,
        GROUP_RISK
    )

    /** Which sections of the document each group enforces. */
    val GROUP_SECTIONS: Map<String, String> = mapOf(
        GROUP_DEV to "1",
        GROUP_BUILD to "2",
        GROUP_ANALYTICS to "3",
        GROUP_MONETIZATION to "4",
        GROUP_PRIVACY to "5",
        GROUP_FTUE to "6",
        GROUP_STORE to "7",
        GROUP_QA to "8",
        GROUP_LIVEOPS to "9-10",
        GROUP_RISK to "11-13"
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        LaunchFoundationCases.devCases() +
            LaunchFoundationCases.buildCases() +
            LaunchFoundationCases.analyticsCases() +
            LaunchMonetizationCases.run() +
            LaunchPrivacyCases.run() +
            LaunchFtueCases.run() +
            LaunchStoreCases.run() +
            LaunchQaCases.run() +
            LaunchLiveOpsCases.run() +
            LaunchRiskCases.run()

    fun groups(): List<String> = GROUP_ORDER

    fun sectionFor(group: String): String = GROUP_SECTIONS[group] ?: "-"
}
