package com.example.game.valor.plan

import com.example.game.vanguard.ContractAudit
import com.example.game.vanguard.ContractCase

/**
 * The Valor Academy Implementation Plan contract gate.
 *
 * Every group mirrors one slice of `Valor_Academy_Implementation_Plan.md` and
 * runs the shipping rules in-process, so a transcribed number that drifts fails
 * on the next launch instead of on a device.
 */
object ValorPlanAudit {

    const val GROUP_CORE = "PLAN_CORE"
    const val GROUP_COMBAT = "PLAN_COMBAT"
    const val GROUP_FIELD = "PLAN_FIELD"
    const val GROUP_OPS = "PLAN_OPS"
    const val GROUP_SAVE = "PLAN_SAVE"
    const val GROUP_STORY = "PLAN_STORY"
    const val GROUP_HUD = "PLAN_HUD"
    const val GROUP_ADS = "PLAN_ADS"
    const val GROUP_META = "PLAN_META"
    const val GROUP_PROCESS = "PLAN_PROCESS"

    /** Display order used by the read-out screen, so groups never reshuffle. */
    val GROUP_ORDER: List<String> = listOf(
        GROUP_CORE,
        GROUP_COMBAT,
        GROUP_FIELD,
        GROUP_OPS,
        GROUP_SAVE,
        GROUP_STORY,
        GROUP_HUD,
        GROUP_ADS,
        GROUP_META,
        GROUP_PROCESS
    )

    /** Which sections of the document each group enforces. */
    val GROUP_SECTIONS: Map<String, String> = mapOf(
        GROUP_CORE to "4",
        GROUP_COMBAT to "5.1-5.6",
        GROUP_FIELD to "5.7-5.12",
        GROUP_OPS to "5.13-5.18",
        GROUP_SAVE to "5.19",
        GROUP_STORY to "5.20",
        GROUP_HUD to "5.21",
        GROUP_ADS to "5.22",
        GROUP_META to "2, 3, 6, 11",
        GROUP_PROCESS to "7-10, 12"
    )

    fun audit(): ContractAudit = ContractAudit(run())

    fun run(): List<ContractCase> =
        PlanCoreCases.run() +
            PlanCombatCases.run() +
            PlanDamageCases.run() +
            PlanFieldCases.run() +
            PlanStealthCases.run() +
            PlanOpsCases.run() +
            PlanCareerCases.run() +
            PlanSaveCases.run() +
            PlanStoryCases.run() +
            PlanHudCases.run() +
            PlanAdCases.run() +
            PlanMetaCases.run() +
            PlanProcessCases.run()

    /** Groups in display order, including any group that produced no case. */
    fun groups(): List<String> = GROUP_ORDER

    fun sectionFor(group: String): String = GROUP_SECTIONS[group] ?: "-"
}
