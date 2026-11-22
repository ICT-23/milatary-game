package com.example.game.vanguard

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.isOk
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.meta.BudgetAllocation
import com.example.game.vanguard.meta.BudgetSector
import com.example.game.vanguard.meta.CombatState
import com.example.game.vanguard.meta.MetaCameraController
import com.example.game.vanguard.meta.MetaZone
import com.example.game.vanguard.meta.PostMissionHook
import com.example.game.vanguard.meta.PresidentialGovernance
import com.example.game.vanguard.meta.ProductionKind
import com.example.game.vanguard.meta.ProductionOrder
import com.example.game.vanguard.meta.ProductionQueue
import com.example.game.vanguard.meta.SessionFlowGuard
import com.example.game.vanguard.narrative.Dilemma
import com.example.game.vanguard.narrative.DilemmaEngine
import com.example.game.vanguard.narrative.DilemmaLibrary
import com.example.game.vanguard.narrative.DilemmaOption
import com.example.game.vanguard.narrative.OfficerAttributes

/**
 * Section 3.4 item 1: three-way dilemmas that move three signed attributes.
 * A choice that is trivially correct is rejected by the engine, not by the
 * writer, because "obvious" is a property the data can be checked for.
 */
object NarrativeContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_NARRATIVE)
        checkValidation(cases)
        checkResolution(cases)
        return cases.result()
    }

    private fun checkValidation(cases: CaseRecorder) {
        val library = DilemmaLibrary.ACADEMY_BOARD_LIVE_FIRE

        cases.check(
            title = "Both shipped dilemmas satisfy the three-option rule",
            expectation = "Ok for every entry in the library",
            passed = DilemmaLibrary.ALL.all { DilemmaEngine.validate(it).isOk },
            detail = observed(DilemmaLibrary.ALL.map { it.options.size }, 3)
        )

        val short = library.copy(options = library.options.take(2))
        cases.check(
            title = "A dilemma with fewer than three options is rejected",
            expectation = "WRONG_OPTION_COUNT",
            passed = DilemmaEngine.validate(short).errorCodeOrNull() == "WRONG_OPTION_COUNT",
            detail = observed(DilemmaEngine.validate(short).errorCodeOrNull(), "WRONG_OPTION_COUNT")
        )

        val duplicate = library.copy(
            options = library.options.mapIndexed { index, option ->
                if (index == 1) option.copy(id = library.options[0].id) else option
            }
        )
        cases.check(
            title = "Duplicate option identifiers are rejected",
            expectation = "DUPLICATE_OPTION_ID",
            passed = DilemmaEngine.validate(duplicate).errorCodeOrNull() == "DUPLICATE_OPTION_ID",
            detail = observed(DilemmaEngine.validate(duplicate).errorCodeOrNull(), "DUPLICATE_OPTION_ID")
        )

        val rigged: Dilemma = library.copy(
            id = "DIL_TEST_RIGGED",
            options = listOf(
                DilemmaOption("BEST", "Best on every axis", 1, 1, 1, "no trade-off"),
                DilemmaOption("MIDDLE", "Neutral", 0, 0, 0, "no trade-off"),
                DilemmaOption("WORST", "Worst on every axis", -1, -1, -1, "no trade-off")
            )
        )
        cases.check(
            title = "A dominating option makes the dilemma trivial and is rejected",
            expectation = "TRIVIAL_DILEMMA naming the dominating id",
            passed = DilemmaEngine.validate(rigged).errorCodeOrNull() == "TRIVIAL_DILEMMA",
            detail = observed(DilemmaEngine.dominatingOption(rigged), "a dominating id")
        )
    }

    private fun checkResolution(cases: CaseRecorder) {
        val dilemma = DilemmaLibrary.ACADEMY_BOARD_LIVE_FIRE

        val endorse = DilemmaEngine.choose(dilemma, "ENDORSE", OfficerAttributes()).valueOrNull()
        cases.check(
            title = "Choosing an option applies all three signed deltas",
            expectation = "doctrine -8, morale +14, capital -5",
            passed = endorse != null && endorse.attributes == OfficerAttributes(-8, 14, -5),
            detail = observed(endorse?.attributes, OfficerAttributes(-8, 14, -5))
        )

        val clamped = DilemmaEngine.choose(dilemma, "ENDORSE", OfficerAttributes(95, 95, 95)).valueOrNull()
        cases.check(
            title = "Attributes are clamped to the -100..+100 range",
            expectation = "87 / 100 / 90 after the clamp",
            passed = clamped != null && clamped.attributes == OfficerAttributes(87, 100, 90),
            detail = observed(clamped?.attributes, OfficerAttributes(87, 100, 90))
        )

        cases.check(
            title = "Unit morale feeds squad responsiveness",
            expectation = "morale 14 yields 1.028",
            passed = endorse != null && approxEquals(endorse.squadResponsiveness, 1.028f, 0.0001f),
            detail = observed(endorse?.squadResponsiveness, 1.028f)
        )

        cases.check(
            title = "Political capital feeds the budget multiplier",
            expectation = "capital -5 yields 0.9875",
            passed = endorse != null && approxEquals(endorse.budgetMultiplier, 0.9875f, 0.0001f),
            detail = observed(endorse?.budgetMultiplier, 0.9875f)
        )

        val unknown = DilemmaEngine.choose(dilemma, "NO_SUCH_OPTION", OfficerAttributes())
        cases.check(
            title = "An unknown option id is rejected",
            expectation = "UNKNOWN_OPTION",
            passed = unknown.errorCodeOrNull() == "UNKNOWN_OPTION",
            detail = observed(unknown.errorCodeOrNull(), "UNKNOWN_OPTION")
        )
    }
}

/**
 * Section 3.4 items 2-4: deterministic production timestamps, the presidential
 * budget loop, the post-mission checkpoint guard and the meta camera.
 */
object MetaContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_META)
        checkProduction(cases)
        checkGovernance(cases)
        checkSessionFlow(cases)
        checkCamera(cases)
        return cases.result()
    }

    private fun checkProduction(cases: CaseRecorder) {
        val order = ProductionOrder(
            id = "ORDER_DESTROYER",
            name = "Destroyer",
            kind = ProductionKind.NAVAL_SHIP,
            stationId = "DOCK_01",
            startUtcSeconds = 1_000L,
            durationSeconds = 3_600L
        )

        val half = ProductionQueue.progress(order, nowUtcSeconds = 2_800L)
        cases.check(
            title = "Production progress is now minus startUtc",
            expectation = "1800 s elapsed, 0.5 complete, 1800 s left",
            passed = half.elapsedSeconds == 1_800L &&
                approxEquals(half.progress01, 0.5f) &&
                half.remainingSeconds == 1_800L,
            detail = "elapsed=${half.elapsedSeconds} progress=${half.progress01} remaining=${half.remainingSeconds}"
        )

        val finished = ProductionQueue.progress(order, nowUtcSeconds = 10_000L)
        cases.check(
            title = "An order past its duration completes without overflowing",
            expectation = "progress 1.0, zero remaining",
            passed = finished.isComplete && approxEquals(finished.progress01, 1f) && finished.remainingSeconds == 0L,
            detail = "progress=${finished.progress01} remaining=${finished.remainingSeconds}"
        )

        val clockBehind = ProductionQueue.progress(order, nowUtcSeconds = 900L)
        cases.check(
            title = "A clock that predates the order clamps to zero progress",
            expectation = "0.0 progress, never negative",
            passed = clockBehind.elapsedSeconds == 0L && approxEquals(clockBehind.progress01, 0f),
            detail = "elapsed=${clockBehind.elapsedSeconds} progress=${clockBehind.progress01}"
        )

        val second = ProductionOrder("ORDER_TANK", "Tank", ProductionKind.GROUND_VEHICLE, "DEPOT_02", 4_500L, 1_000L)
        val (done, pending) = ProductionQueue.catchUp(listOf(order, second), nowUtcSeconds = 5_000L)
        cases.check(
            title = "Offline catch-up separates finished work from running work",
            expectation = "one completed order, one still in production",
            passed = done.size == 1 && pending.size == 1 && done[0].order.id == "ORDER_DESTROYER",
            detail = "completed=${done.map { it.order.id }} pending=${pending.map { it.id }}"
        )
    }

    private fun checkGovernance(cases: CaseRecorder) {
        val incomplete = PresidentialGovernance.allocate(1_000f, BudgetAllocation(34, 33, 30))
        cases.check(
            title = "A budget that does not total 100 percent is rejected",
            expectation = "BUDGET_NOT_ALLOCATED",
            passed = incomplete.errorCodeOrNull() == "BUDGET_NOT_ALLOCATED",
            detail = observed(incomplete.errorCodeOrNull(), "BUDGET_NOT_ALLOCATED")
        )

        val negative = PresidentialGovernance.allocate(1_000f, BudgetAllocation(-1, 50, 51))
        cases.check(
            title = "A negative sector allocation is rejected",
            expectation = "NEGATIVE_BUDGET_LINE",
            passed = negative.errorCodeOrNull() == "NEGATIVE_BUDGET_LINE",
            detail = observed(negative.errorCodeOrNull(), "NEGATIVE_BUDGET_LINE")
        )

        val enacted = PresidentialGovernance.allocate(1_000f, BudgetAllocation(34, 33, 33)).valueOrNull()
        cases.check(
            title = "GDP is split across the three sectors",
            expectation = "340 / 330 / 330 billion",
            passed = enacted != null &&
                approxEquals(enacted.civilInfrastructureBillions, 340f) &&
                approxEquals(enacted.defenseRndBillions, 330f) &&
                approxEquals(enacted.fleetBillions, 330f),
            detail = "civil=${enacted?.civilInfrastructureBillions} defense=${enacted?.defenseRndBillions} fleets=${enacted?.fleetBillions}"
        )

        cases.check(
            title = "The development index rewards a balanced budget",
            expectation = "98.67 for 34 / 33 / 33",
            passed = enacted != null && approxEquals(enacted.nationalDevelopmentIndex, 98.6667f, 0.01f),
            detail = observed(enacted?.nationalDevelopmentIndex, 98.6667f)
        )

        val rebalanced = PresidentialGovernance.rebalance(BudgetSector.DEFENSE_RND, newValue = 50).valueOrNull()
        cases.check(
            title = "The policy slider always returns a complete budget",
            expectation = "total 100 with Defense R&D at 50",
            passed = rebalanced != null && rebalanced.total == 100 && rebalanced.defenseRnd == 50,
            detail = "allocation=$rebalanced total=${rebalanced?.total}"
        )
    }

    private fun checkSessionFlow(cases: CaseRecorder) {
        val blocked = SessionFlowGuard.evaluate(PostMissionHook.INTERSTITIAL_AD, CombatState.ACTIVE_MISSION)
        cases.check(
            title = "An interstitial ad cannot run during an active mission",
            expectation = "BLOCKED_DURING_ACTIVE_MISSION",
            passed = blocked.errorCodeOrNull() == "BLOCKED_DURING_ACTIVE_MISSION",
            detail = observed(blocked.errorCodeOrNull(), "BLOCKED_DURING_ACTIVE_MISSION")
        )

        cases.check(
            title = "Post-mission hooks run once the mission reaches a terminal state",
            expectation = "Ok on MISSION_COMPLETED and MISSION_FAILED, blocked while idle",
            passed = SessionFlowGuard.evaluate(PostMissionHook.REWARD_SUMMARY, CombatState.MISSION_COMPLETED).isOk &&
                SessionFlowGuard.evaluate(PostMissionHook.REWARD_SUMMARY, CombatState.MISSION_FAILED).isOk &&
                SessionFlowGuard.evaluate(PostMissionHook.REWARD_SUMMARY, CombatState.IDLE).errorCodeOrNull() == "BLOCKED_NO_BOUNDARY",
            detail = "completed=${SessionFlowGuard.evaluate(PostMissionHook.REWARD_SUMMARY, CombatState.MISSION_COMPLETED)}"
        )

        val bus = EventBus()
        SessionFlowGuard.evaluate(PostMissionHook.BATTLE_PASS_POPUP, CombatState.ACTIVE_MISSION, bus)
        cases.check(
            title = "A blocked hook raises the diagnostic event",
            expectation = "one SessionFlowBlocked published",
            passed = bus.publishedCount == 1,
            detail = observed(bus.publishedCount, 1)
        )
    }

    private fun checkCamera(cases: CaseRecorder) {
        val blended = MetaCameraController.zoom(MetaZone.ACADEMY_GROUNDS, MetaZone.BATTLE_MAP, 0.5f)
        cases.check(
            title = "The meta camera blends height between zones",
            expectation = "87 m at the halfway point of 24 m to 150 m",
            passed = approxEquals(blended.cameraHeightMeters, 87f) && !blended.isSettled,
            detail = observed(blended.cameraHeightMeters, 87f)
        )

        val chain = MetaCameraController.nextZone(
            MetaCameraController.nextZone(MetaCameraController.nextZone(MetaZone.ACADEMY_GROUNDS))
        )
        cases.check(
            title = "The zoom chain runs grounds, bay, dockyard, battle map",
            expectation = "three steps reach BATTLE_MAP and stop there",
            passed = chain == MetaZone.BATTLE_MAP && MetaCameraController.nextZone(chain) == MetaZone.BATTLE_MAP &&
                MetaCameraController.previousZone(MetaZone.BATTLE_MAP) == MetaZone.NAVAL_DOCKYARD,
            detail = observed(chain, MetaZone.BATTLE_MAP)
        )
    }
}
