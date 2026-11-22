package com.example.game.vanguard

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult
import com.example.game.vanguard.core.MissionStateChanged
import com.example.game.vanguard.core.RankChanged
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.errorOrNull
import com.example.game.vanguard.core.isOk
import com.example.game.vanguard.core.map
import com.example.game.vanguard.core.subscribeTo
import com.example.game.vanguard.core.then
import com.example.game.vanguard.core.valueOrNull

/**
 * Section 1.2 step 3 / Section 2 Core.
 *
 * The EventBus and the Result type are the two primitives every other module
 * builds on, so they are checked before any gameplay contract is worth running.
 */
object CoreCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_CORE)
        checkBus(cases)
        checkResult(cases)
        return cases.result()
    }

    private fun checkBus(cases: CaseRecorder) {
        val bus = EventBus()
        var delivered = 0
        val slot = bus.subscribeTo<RankChanged> { delivered += 1 }

        bus.publish(RankChanged(oldRankIndex = 0, newRankIndex = 1, rankCode = "E4_CPL", rankTitle = "Corporal"))
        cases.check(
            title = "EventBus delivers a matching event",
            expectation = "one RankChanged reaches the subscriber",
            passed = delivered == 1,
            detail = observed(delivered, 1)
        )

        bus.publish(MissionStateChanged(missionId = "MISSION_01", state = "ACTIVE_MISSION"))
        cases.check(
            title = "EventBus does not leak events across subscriber types",
            expectation = "delivery count stays at one",
            passed = delivered == 1,
            detail = observed(delivered, 1)
        )

        slot.unsubscribe()
        bus.publish(RankChanged(1, 2, "E6_SSG", "Staff Sergeant"))
        cases.check(
            title = "Unsubscribing detaches the handler",
            expectation = "no further delivery and an empty subscriber list",
            passed = delivered == 1 && bus.subscriberCount == 0,
            detail = "delivered=$delivered subscribers=${bus.subscriberCount}"
        )

        cases.check(
            title = "Every publish is counted for diagnostics",
            expectation = "publishedCount tracks all three publishes",
            passed = bus.publishedCount == 3,
            detail = observed(bus.publishedCount, 3)
        )
    }

    private fun checkResult(cases: CaseRecorder) {
        val blocked: GameResult<Int> =
            GameResult.fail(GameErrorCategory.SAFETY, "SAFETY_BLOCKED", "Safety blocks the trigger")

        var nextStepRan = false
        val chained = blocked.then { value ->
            nextStepRan = true
            GameResult.Ok(value + 1)
        }
        cases.check(
            title = "then() short-circuits a failure and keeps its code",
            expectation = "SAFETY_BLOCKED survives and the next step never runs",
            passed = !nextStepRan && chained.errorCodeOrNull() == "SAFETY_BLOCKED",
            detail = "code=${chained.errorCodeOrNull()} nextStepRan=$nextStepRan"
        )

        val doubled = GameResult.Ok(7).then { value -> GameResult.Ok(value * 2) }
        val doubledValue = doubled.valueOrNull()
        cases.check(
            title = "then() carries the success value forward",
            expectation = "7 chained through *2 yields 14",
            passed = doubled.isOk && doubledValue == 14,
            detail = observed(doubledValue, 14)
        )

        val mapped = blocked.map { it * 3 }
        cases.check(
            title = "map() preserves the original failure",
            expectation = "SAFETY_BLOCKED, no substituted value",
            passed = mapped.errorCodeOrNull() == "SAFETY_BLOCKED",
            detail = observed(mapped.errorCodeOrNull(), "SAFETY_BLOCKED")
        )

        cases.check(
            title = "A failure keeps its category for the UI to branch on",
            expectation = "SAFETY category, not a re-parsed string",
            passed = blocked.errorOrNull()?.category == GameErrorCategory.SAFETY,
            detail = observed(blocked.errorOrNull()?.category, GameErrorCategory.SAFETY)
        )
    }
}
