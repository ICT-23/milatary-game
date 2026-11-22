package com.example.game.valor.plan

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 7 to 10 and 12: the test plan, the budgets, the edge cases and the DoD. */
object PlanProcessCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_PROCESS)
        qualityCases(r)
        budgetCases(r)
        edgeCaseCases(r)
        debugCases(r)
        doneCases(r)
        return r.result()
    }

    private fun qualityCases(r: CaseRecorder) {
        r.check(
            "Eight unit-test areas", "every pure-logic system carries named checks",
            PlanQuality.UNIT_AREAS.size == 8 && PlanQuality.allAreasNameTests() &&
                PlanQuality.areaNames().contains("DamageSystem"),
            observed(PlanQuality.UNIT_AREAS.size, 8)
        )
        r.check(
            "PlayMode and manual passes", "five scripted runs and eight hand checks",
            PlanQuality.PLAY_MODE_TESTS.size == 5 && PlanQuality.MANUAL_QA.size == 8 &&
                PlanQuality.MANUAL_QA.any { it.contains("No forced ads") },
            observed(PlanQuality.PLAY_MODE_TESTS.size, 5)
        )
        val tournament = PlanQuality.regressionFor("First tournament impossible")
        val ads = PlanQuality.regressionFor("Ads during gameplay")
        r.check(
            "Five regressions", "each original bug keeps a guard",
            PlanQuality.REGRESSIONS.size == 5 && tournament != null && ads != null &&
                ads.ourTest.contains("no ad call"),
            observed(PlanQuality.REGRESSIONS.size, 5)
        )
    }

    private fun budgetCases(r: CaseRecorder) {
        r.check(
            "Three quality tiers", "Low at 30 FPS, Medium and High at 60",
            PerformancePlan.BUDGETS.size == 3 &&
                PerformancePlan.BUDGETS.map { it.fps } == listOf(30, 60, 60),
            observed(PerformancePlan.BUDGETS.map { it.fps }, listOf(30, 60, 60))
        )
        r.check(
            "Draw calls and triangles", "100/150/250 calls, 250k/400k/800k triangles",
            PerformancePlan.BUDGETS.map { it.drawCallsMax } == listOf(100, 150, 250) &&
                PerformancePlan.BUDGETS.map { it.trianglesMax } == listOf(250_000, 400_000, 800_000),
            observed(PerformancePlan.BUDGETS.map { it.trianglesMax }, "250k/400k/800k")
        )
        r.check(
            "AI caps agree with 5.10", "the budget table and the AI table are the same numbers",
            PerformancePlan.BUDGETS.map { it.activeAi } == EnemyAiRules.ACTIVE_AI_BY_TIER,
            observed(PerformancePlan.BUDGETS.map { it.activeAi }, EnemyAiRules.ACTIVE_AI_BY_TIER)
        )
        r.check(
            "Render scale", "0.7, 0.85 and 1.0 from Low to High",
            PerformancePlan.BUDGETS.map { it.renderScale }
                .zip(listOf(0.7f, 0.85f, 1f))
                .all { (a, b) -> approxEquals(a, b) },
            observed(PerformancePlan.BUDGETS.map { it.renderScale }, listOf(0.7f, 0.85f, 1f))
        )
        r.check(
            "Eleven techniques", "pooling through async scene loading",
            PerformancePlan.techniqueCount() == 11 &&
                PerformancePlan.TECHNIQUES.contains("Async scene loading") &&
                PerformancePlan.TECHNIQUES.contains("No GC allocations in Update"),
            observed(PerformancePlan.techniqueCount(), 11)
        )
        val low = PerformancePlan.LOW
        r.check(
            "Budget verdict", "one column over the ceiling fails the tier",
            PerformancePlan.withinBudget(low, 30, 99, 249_000, 4, 399) &&
                !PerformancePlan.withinBudget(low, 29, 99, 249_000, 4, 399) &&
                !PerformancePlan.withinBudget(low, 30, 100, 249_000, 4, 399) &&
                !PerformancePlan.withinBudget(low, 30, 99, 249_000, 5, 399),
            observed(PerformancePlan.withinBudget(low, 30, 100, 249_000, 4, 399), false)
        )
    }

    private fun edgeCaseCases(r: CaseRecorder) {
        r.check(
            "Sixteen edge cases", "each known bug has a stated behavior",
            EdgeCases.ROWS.size == 16 && EdgeCases.behaviors().all { it.isNotBlank() },
            observed(EdgeCases.ROWS.size, 16)
        )
        r.check(
            "Kill-plane guards the round", "a body lost off the map still unblocks the objective",
            EdgeCases.forSituation("Enemy falls through map")?.behavior?.contains("Kill-plane") == true,
            observed(EdgeCases.hasSituation("Enemy falls through map"), true)
        )
        r.check(
            "Save during death", "only a consistent state is ever written",
            EdgeCases.forSituation("Save during death")?.behavior?.contains("consistent") == true &&
                SaveSystem.SAVE_EXCLUDES_TRANSIENT_COMBAT_STATE,
            observed(SaveSystem.SAVE_EXCLUDES_TRANSIENT_COMBAT_STATE, true)
        )
        r.check(
            "Rank-up waits for Results", "the popup row and the HUD queue agree",
            EdgeCases.forSituation("Rank-up mid-combat")?.behavior?.contains("Results") == true &&
                MissionState.RESULTS in HudRules.SAFE_POPUP_STATES,
            observed(HudRules.SAFE_POPUP_STATES.contains(MissionState.PLAYING), false)
        )
        r.check(
            "Closed ad grants nothing", "the edge case and the reward rule say the same thing",
            EdgeCases.forSituation("Reward ad closes early")?.behavior == "No reward" &&
                !RewardedAdRules.grant(completed = false) { Unit },
            observed(RewardedAdRules.grant(false) { Unit }, false)
        )
    }

    private fun debugCases(r: CaseRecorder) {
        r.check(
            "Panel readouts", "five numbers on the debug overlay",
            DebugTools.PANEL_READOUTS.size == 5 &&
                DebugTools.PANEL_READOUTS.contains("Current mission state"),
            observed(DebugTools.PANEL_READOUTS.size, 5)
        )
        r.check(
            "Nine cheats", "god mode marks its kills as debug and grants no XP",
            DebugTools.CHEATS.size == 9 &&
                DebugTools.cheat("God mode")?.contains("debug") == true,
            observed(DebugTools.CHEATS.size, 9)
        )
        r.check(
            "Six gizmos", "rays, cones, hearing, cover, paths and hitboxes",
            DebugTools.GIZMOS.size == 6 && DebugTools.GIZMOS.contains("Hitbox zones"),
            observed(DebugTools.GIZMOS.size, 6)
        )
        r.check(
            "Nine log categories", "the debug log covers every category the rules name",
            DebugTools.LOG_CATEGORIES.size == 9 &&
                DebugTools.LOG_CATEGORIES.size == LogCategory.entries.size &&
                DebugTools.everyCategoryLogs(DebugLog()),
            observed(DebugTools.LOG_CATEGORIES.size, 9)
        )
        r.check(
            "Debug only", "the panel is compiled out of a release build",
            DebugTools.DEBUG_BUILD_ONLY && DebugTools.STRIPPED_FROM_RELEASE &&
                DebugTools.isActive(debugBuild = true) && !DebugTools.isActive(debugBuild = false),
            observed(DebugTools.isActive(false), false)
        )
    }

    private fun doneCases(r: CaseRecorder) {
        val all = DefinitionOfDone.ITEMS.indices.toSet()
        r.check(
            "Nine conditions of done", "a task is finished only when all nine hold",
            DefinitionOfDone.ITEMS.size == 9 && DefinitionOfDone.isDone(all),
            observed(DefinitionOfDone.ITEMS.size, 9)
        )
        r.check(
            "One missing condition blocks", "the gate is not satisfied by a subset",
            !DefinitionOfDone.isDone(all - 3) &&
                DefinitionOfDone.pending(all - 3) == listOf(DefinitionOfDone.ITEMS[3]),
            observed(DefinitionOfDone.pending(all - 3).size, 1)
        )
        r.check(
            "No rewrite, one service, no magic numbers", "the three rules that guard the codebase",
            DefinitionOfDone.indexOf("Existing working features were not replaced or broken") >= 0 &&
                DefinitionOfDone.indexOf("Damage, XP and currency go through their single services only") >= 0 &&
                DefinitionOfDone.indexOf("No hard-coded balance numbers (data-driven)") >= 0,
            observed(DefinitionOfDone.indexOf("No hard-coded balance numbers (data-driven)"), 3)
        )
        r.check(
            "Status and changelog are part of done", "the documentation gate is a DoD item",
            DefinitionOfDone.indexOf("BUILD_STATUS.md and CHANGELOG.md updated") >= 0 &&
                DefinitionOfDone.pending(emptySet()).size == DefinitionOfDone.ITEMS.size,
            observed(DefinitionOfDone.pending(emptySet()).size, 9)
        )
    }
}
