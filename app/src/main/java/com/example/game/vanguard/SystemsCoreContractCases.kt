package com.example.game.vanguard

import com.example.game.vanguard.core.DependencyContainer
import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.GameClock
import com.example.game.vanguard.core.ModuleRegistry
import com.example.game.vanguard.core.ModuleStatus
import com.example.game.vanguard.core.register
import com.example.game.vanguard.core.resolve

/**
 * Section 2 Core coverage.
 *
 * GameClock, DependencyContainer and ModuleRegistry are the three services every
 * other module calls, so they are audited before any gameplay contract.
 */
object SystemsCoreContractCases {

    private class Probe(val id: String)

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_SYSTEMS)
        checkClock(cases)
        checkContainer(cases)
        checkRegistry(cases)
        return cases.result()
    }

    private fun checkClock(cases: CaseRecorder) {
        val clock = GameClock(timeScale = 2f)
        clock.advance(2f)
        cases.check(
            title = "GameClock scales animation time",
            expectation = "2 s unscaled at 2x reads 4 s scaled",
            passed = approxEquals(clock.scaledSeconds, 4f) && approxEquals(clock.unscaledSeconds, 2f),
            detail = observed("${clock.scaledSeconds}/${clock.unscaledSeconds}", "4.0/2.0")
        )
        clock.pause()
        clock.advance(1f)
        cases.check(
            title = "GameClock keeps unscaled time running while paused",
            expectation = "scaled stays 4.0, unscaled reaches 3.0",
            passed = approxEquals(clock.scaledSeconds, 4f) && approxEquals(clock.unscaledSeconds, 3f),
            detail = observed("${clock.scaledSeconds}/${clock.unscaledSeconds}", "4.0/3.0")
        )
        cases.check(
            title = "Watchdog reads the unscaled source",
            expectation = GameClock.UNSCALED_WATCHDOG_SOURCE,
            passed = GameClock.UNSCALED_WATCHDOG_SOURCE == "GameClock.unscaledSeconds",
            detail = GameClock.UNSCALED_WATCHDOG_SOURCE
        )
    }

    private fun checkContainer(cases: CaseRecorder) {
        val container = DependencyContainer()
        container.register { Probe("A") }
        val first = container.resolve<Probe>()
        val second = container.resolve<Probe>()
        cases.check(
            title = "DependencyContainer resolves one instance per type",
            expectation = "both lookups return the same object",
            passed = first != null && first === second && first.id == "A",
            detail = observed(second?.id, "A")
        )
        cases.check(
            title = "DependencyContainer rejects duplicate registration",
            expectation = "second registration of Probe returns false",
            passed = !container.register { Probe("B") },
            detail = observed(container.registeredCount, 1)
        )
        container.freeze()
        cases.check(
            title = "Frozen container refuses new services",
            expectation = "EventBus cannot be registered after freeze",
            passed = !container.register { EventBus() } && container.resolve<EventBus>() == null,
            detail = observed(container.isFrozen, true)
        )
    }

    private fun checkRegistry(cases: CaseRecorder) {
        val registry = ModuleRegistry.canonical()
        cases.check(
            title = "Registry mirrors the six Section 4 keys",
            expectation = "size 6 starting at Core_EventBus_Result",
            passed = registry.size == 6 &&
                registry.entries().first().key == "Core_EventBus_Result" &&
                registry.entries().last().key == "Meta_ShipVehicle_PresidentialSim",
            detail = observed(registry.size, 6)
        )
        cases.check(
            title = "Nothing is locked before the audit runs",
            expectation = "lockedCount 0 and spec keys editable",
            passed = registry.lockedCount == 0 && registry.mayEdit("Combat_DualVectorBallistics_Hitboxes"),
            detail = observed(registry.lockedCount, 0)
        )
        val locked = registry.lock("Core_EventBus_Result")
        cases.check(
            title = "Locking blocks further edits",
            expectation = "lock succeeds, mayEdit false, unknown locks fail",
            passed = locked &&
                !registry.mayEdit("Core_EventBus_Result") &&
                !registry.markImplemented("Core_EventBus_Result") &&
                !registry.lock("NOT_A_KEY") &&
                registry.mayEdit("NOT_A_KEY"),
            detail = observed(registry.lockedKeys(), listOf("Core_EventBus_Result"))
        )
        val parsed = ModuleRegistry.fromPairs(
            mapOf("Core_EventBus_Result" to " locked_verified ", "Mystery" to "WHAT")
        )
        cases.check(
            title = "Registry parsing normalises status labels",
            expectation = "case-insensitive lock, unknown status falls back to pending",
            passed = parsed.statusOf("Core_EventBus_Result") == ModuleStatus.LOCKED_VERIFIED &&
                parsed.statusOf("Mystery") == ModuleStatus.PENDING_IMPLEMENTATION,
            detail = observed(
                parsed.entries().map { it.status.label },
                "LOCKED_VERIFIED/PENDING_IMPLEMENTATION"
            )
        )
    }
}
