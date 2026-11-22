package com.example.game.vanguard

import com.example.game.vanguard.core.EventBus
import com.example.game.vanguard.core.PromotionAcknowledged
import com.example.game.vanguard.core.RankChanged
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.isOk
import com.example.game.vanguard.core.subscribeTo
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.progression.CharacterCustomization
import com.example.game.vanguard.progression.PromotionWatchdog
import com.example.game.vanguard.progression.RankEntry
import com.example.game.vanguard.progression.RankLedger
import com.example.game.vanguard.progression.RankProgressionService
import com.example.game.vanguard.progression.UniformMeshBinder
import com.example.game.vanguard.progression.UniformTable
import com.example.game.vanguard.progression.VanguardRank

/**
 * Section 3.3: the monotonic rank enumeration, the append-only ledger, the
 * three-second watchdog and the uniform binding that had to stop resetting.
 */
object CareerContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_CAREER)
        checkEnumeration(cases)
        checkLedger(cases)
        checkWatchdog(cases)
        checkUniform(cases)
        return cases.result()
    }

    private fun checkEnumeration(cases: CaseRecorder) {
        val ranks = VanguardRank.entries
        cases.check(
            title = "The career ladder holds exactly nine ranks",
            expectation = "9 ranks",
            passed = ranks.size == 9,
            detail = observed(ranks.size, 9)
        )

        cases.check(
            title = "Rank indices are the strict 0..8 order",
            expectation = "indices 0 to 8 with no gaps",
            passed = ranks.map { it.index } == (0..8).toList(),
            detail = observed(ranks.map { it.index }, (0..8).toList())
        )

        val expectedCodes = listOf(
            "E1_PVT", "E4_CPL", "E6_SSG", "O1_LT", "O3_CPT",
            "O5_LTC", "O7_BG", "O10_GEN", "CIV_PRES"
        )
        cases.check(
            title = "Rank codes follow the documented five-act order",
            expectation = "E1_PVT through CIV_PRES",
            passed = ranks.map { it.code } == expectedCodes && VanguardRank.DEFAULT_FALLBACK == VanguardRank.E1_PVT,
            detail = observed(ranks.map { it.code }, expectedCodes)
        )
    }

    private fun checkLedger(cases: CaseRecorder) {
        val bus = EventBus()
        val service = RankProgressionService(bus)
        var lastEvent: RankChanged? = null
        bus.subscribeTo<RankChanged> { lastEvent = it }

        val promoted = service.promoteTo(RankLedger.initial(atEpochMs = 0L), VanguardRank.E6_SSG, 1_000L)
        cases.check(
            title = "A promotion appends one ledger entry",
            expectation = "Ok, current rank E6_SSG, history length 2",
            passed = promoted.isOk && promoted.valueOrNull()?.current == VanguardRank.E6_SSG &&
                promoted.valueOrNull()?.history?.size == 2,
            detail = "current=${promoted.valueOrNull()?.current} history=${promoted.valueOrNull()?.history?.size}"
        )

        cases.check(
            title = "A promotion publishes RankChanged exactly once",
            expectation = "old 0, new 2, code E6_SSG",
            passed = bus.publishedCount == 1 && lastEvent?.oldRankIndex == 0 &&
                lastEvent?.newRankIndex == 2 && lastEvent?.rankCode == "E6_SSG",
            detail = "published=${bus.publishedCount} event=$lastEvent"
        )

        val regression = service.promoteTo(RankLedger.of(RankEntry(VanguardRank.O5_LTC, 0L, "SAVE")), VanguardRank.O1_LT, 2_000L)
        cases.check(
            title = "A regression promotion is rejected",
            expectation = "RANK_REGRESSION_BLOCKED from O5_LTC to O1_LT",
            passed = regression.errorCodeOrNull() == "RANK_REGRESSION_BLOCKED",
            detail = observed(regression.errorCodeOrNull(), "RANK_REGRESSION_BLOCKED")
        )

        val repeated = service.promoteTo(RankLedger.of(RankEntry(VanguardRank.E6_SSG, 0L, "SAVE")), VanguardRank.E6_SSG, 2_100L)
        cases.check(
            title = "Re-promoting to the current rank is rejected",
            expectation = "RANK_REGRESSION_BLOCKED",
            passed = repeated.errorCodeOrNull() == "RANK_REGRESSION_BLOCKED",
            detail = observed(repeated.errorCodeOrNull(), "RANK_REGRESSION_BLOCKED")
        )

        val savedCaptain = RankLedger.of(
            RankEntry(VanguardRank.E1_PVT, 0L, "ENLISTMENT"),
            RankEntry(VanguardRank.O3_CPT, 5_000L, "PROMOTION")
        )
        cases.check(
            title = "A chapter loader cannot reset the save to E1_PVT",
            expectation = "the saved O3_CPT survives a lower chapter default",
            passed = service.resolveLoadedRank(savedCaptain, VanguardRank.E1_PVT) == VanguardRank.O3_CPT,
            detail = observed(service.resolveLoadedRank(savedCaptain, VanguardRank.E1_PVT), VanguardRank.O3_CPT)
        )

        cases.check(
            title = "A higher chapter default may still promote the save",
            expectation = "O1_LT over a fresh E1_PVT ledger",
            passed = service.resolveLoadedRank(RankLedger.initial(), VanguardRank.O1_LT) == VanguardRank.O1_LT,
            detail = observed(service.resolveLoadedRank(RankLedger.initial(), VanguardRank.O1_LT), VanguardRank.O1_LT)
        )

        cases.check(
            title = "The ledger remembers every rank it ever held",
            expectation = "E1_PVT and O3_CPT both present",
            passed = savedCaptain.everHeld(VanguardRank.E1_PVT) && savedCaptain.everHeld(VanguardRank.O3_CPT),
            detail = "history=${savedCaptain.history.map { it.rank.code }}"
        )
    }

    private fun checkWatchdog(cases: CaseRecorder) {
        val watchdog = PromotionWatchdog()
        watchdog.start()
        val atOneAndHalf = watchdog.update(1.5f)
        val atThree = watchdog.update(1.5f)
        cases.check(
            title = "The Continue button unlocks at 3.0 s without the animation",
            expectation = "locked at 1.5 s, unlocked at 3.0 s, forced release",
            passed = !atOneAndHalf && atThree && watchdog.forced,
            detail = "at1.5=$atOneAndHalf at3.0=$atThree forced=${watchdog.forced}"
        )

        val bus = EventBus()
        val acknowledged = PromotionWatchdog().attach(bus)
        acknowledged.start()
        bus.publish(PromotionAcknowledged(rankIndex = 2, acknowledgedAtSeconds = 0.4f))
        cases.check(
            title = "A working ceremony still releases the button immediately",
            expectation = "enabled before the timeout, not a forced release",
            passed = acknowledged.continueEnabled && !acknowledged.forced,
            detail = "enabled=${acknowledged.continueEnabled} forced=${acknowledged.forced}"
        )

        cases.check(
            title = "The watchdog default is exactly three seconds",
            expectation = "3.0 s",
            passed = approxEquals(PromotionWatchdog.DEFAULT_TIMEOUT_SECONDS, 3f),
            detail = observed(PromotionWatchdog.DEFAULT_TIMEOUT_SECONDS, 3f)
        )
    }

    private fun checkUniform(cases: CaseRecorder) {
        val presets = VanguardRank.entries.map { UniformTable.forRank(it) }
        cases.check(
            title = "Every rank maps to its own uniform preset",
            expectation = "9 distinct presets with a garrison garment",
            passed = presets.map { it.rank }.toSet().size == 9 && presets.all { it.garrisonGarment.isNotBlank() },
            detail = observed(presets.map { it.rank.code }.toSet().size, 9)
        )

        val general = UniformTable.forRank(VanguardRank.O5_LTC)
        val president = UniformTable.forRank(VanguardRank.CIV_PRES)
        cases.check(
            title = "General officers wear a ribbon rack, the Commander-in-Chief a lapel pin",
            expectation = "ribbon rack at O5_LTC, PIN_COMMANDER_IN_CHIEF at CIV_PRES",
            passed = general.ribbonRack && president.collarInsignia == "PIN_COMMANDER_IN_CHIEF" && !president.ribbonRack,
            detail = "generalRack=${general.ribbonRack} presidentPin=${president.collarInsignia}"
        )

        val bus = EventBus()
        val service = RankProgressionService(bus)
        val customization = CharacterCustomization(faceId = "FACE_B", hairId = "HAIR_LONG", gearIds = listOf("GEAR_OPTIC"))
        val savedCaptain = RankLedger.of(RankEntry(VanguardRank.O3_CPT, 1_000L, "PROMOTION"))
        val binder = UniformMeshBinder(bus, customization).attach()
        service.promoteTo(savedCaptain, VanguardRank.O5_LTC, 2_000L)

        cases.check(
            title = "A promotion rebinds the uniform mesh to the new rank",
            expectation = "O5_LTC preset applied after exactly one RankChanged",
            passed = binder.appearance.rank == VanguardRank.O5_LTC && binder.swapCount == 1 &&
                binder.appearance.preset.epaulets == "EPAU_GO_STAR",
            detail = "rank=${binder.appearance.rank} swaps=${binder.swapCount} epaulets=${binder.appearance.preset.epaulets}"
        )

        cases.check(
            title = "The uniform swap preserves face, hair and personal gear",
            expectation = "FACE_B / HAIR_LONG / GEAR_OPTIC carried across",
            passed = binder.appearance.customization.faceId == "FACE_B" &&
                binder.appearance.customization.hairId == "HAIR_LONG" &&
                binder.appearance.customization.gearIds == listOf("GEAR_OPTIC"),
            detail = "customization=${binder.appearance.customization}"
        )
    }
}
