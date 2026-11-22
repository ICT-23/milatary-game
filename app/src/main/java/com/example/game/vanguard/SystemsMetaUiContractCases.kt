package com.example.game.vanguard

import com.example.game.vanguard.combat.MechanicalState
import com.example.game.vanguard.combat.SafetyMode
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.meta.ArmorPackage
import com.example.game.vanguard.meta.BaseIdleManager
import com.example.game.vanguard.meta.BattleMapController
import com.example.game.vanguard.meta.BattleMapZone
import com.example.game.vanguard.meta.CombatState
import com.example.game.vanguard.meta.PowertrainPackage
import com.example.game.vanguard.meta.VehicleBase
import com.example.game.vanguard.meta.VehicleConfig
import com.example.game.vanguard.meta.VehicleCustomizationBay
import com.example.game.vanguard.meta.ZoneControl
import com.example.game.vanguard.narrative.ChapterGate
import com.example.game.vanguard.narrative.StoryAct
import com.example.game.vanguard.narrative.StoryArcManager
import com.example.game.vanguard.progression.VanguardRank
import com.example.game.vanguard.ui.CheckpointAdBoundary
import com.example.game.vanguard.ui.CheckpointSurface
import com.example.game.vanguard.ui.HudController
import com.example.game.vanguard.ui.PauseSensitivityPanel
import com.example.game.vanguard.ui.SensitivityPreset
import com.example.game.vanguard.ui.SensitivityProfile

/**
 * Section 2 Meta / UI coverage.
 *
 * Story gating, idle accrual, theater control, vehicle fitting and the three UI
 * boundaries that must never invent their own rules.
 */
object SystemsMetaUiContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_SYSTEMS)
        checkStory(cases)
        checkIdle(cases)
        checkBattleMap(cases)
        checkVehicles(cases)
        checkUi(cases)
        return cases.result()
    }

    private fun checkStory(cases: CaseRecorder) {
        val arcs = StoryArcManager()
        cases.check(
            title = "Story arc maps nine chapters onto five acts",
            expectation = "chapter 1 in Act I, chapter 9 in Act V, chapter 10 unknown",
            passed = arcs.actForChapter(1) == StoryAct.ACT_I &&
                arcs.actForChapter(9) == StoryAct.ACT_V &&
                arcs.actForChapter(10) == null &&
                arcs.chapterCount == 9,
            detail = observed(arcs.chapterCount, 9)
        )
        val gate = ChapterGate(5, "CH05", "Command", requiresChapters = listOf(3))
        cases.check(
            title = "Chapter unlocks need prerequisites and rank",
            expectation = "blocked at O1_LT, open at O3_CPT, prerequisite still enforced",
            passed = !arcs.isUnlocked(gate, setOf(1, 3), VanguardRank.O1_LT) &&
                arcs.isUnlocked(gate, setOf(1, 3), VanguardRank.O3_CPT) &&
                !arcs.isUnlocked(gate, setOf(1), VanguardRank.O5_LTC),
            detail = observed(arcs.blockerFor(gate, setOf(1), VanguardRank.O5_LTC), "Requires chapter 3")
        )
        cases.check(
            title = "Blocker text names the gate that failed",
            expectation = "rank blocker mentions O3_CPT, open chapter has no blocker",
            passed = arcs.blockerFor(gate, setOf(1, 3), VanguardRank.O1_LT)?.contains("O3_CPT") == true &&
                arcs.nextUnlockedChapter(listOf(gate), setOf(1, 3), VanguardRank.O3_CPT) == gate &&
                arcs.blockerFor(gate, setOf(1, 3), VanguardRank.O3_CPT) == null,
            detail = observed(
                arcs.blockerFor(gate, setOf(1, 3), VanguardRank.O1_LT),
                "Requires O3_CPT to enter Act III - Command"
            )
        )
    }

    private fun checkIdle(cases: CaseRecorder) {
        val short = BaseIdleManager.accrue(0L, 3_600L * 3)
        val long = BaseIdleManager.accrue(0L, 3_600L * 20)
        val backwards = BaseIdleManager.accrue(7_200L, 3_600L)
        cases.check(
            title = "Idle accrual credits elapsed hours",
            expectation = "3 h -> 90 intel, 135 supplies, not capped",
            passed = approxEquals(short.elapsedHours, 3f) &&
                short.intel == 90 &&
                short.supplies == 135 &&
                !short.wasCapped,
            detail = observed("${short.intel}/${short.supplies}", "90/135")
        )
        cases.check(
            title = "Idle accrual caps at twelve hours",
            expectation = "20 h elapsed credits 12 h, a backwards clock credits nothing",
            passed = long.wasCapped &&
                approxEquals(long.creditedHours, 12f) &&
                long.intel == 360 &&
                backwards.elapsedHours == 0f &&
                backwards.intel == 0,
            detail = observed("${long.creditedHours}/${long.intel}", "12.0/360")
        )
    }

    private fun checkBattleMap(cases: CaseRecorder) {
        val zones = listOf(
            BattleMapZone("Z1", "Ridge", "NORTH", ZoneControl.HOSTILE, garrisonStrength = 40),
            BattleMapZone("Z2", "Bridge", "NORTH", ZoneControl.HOSTILE, garrisonStrength = 12),
            BattleMapZone("Z3", "Depot", "SOUTH", ZoneControl.SECURED, garrisonStrength = 8)
        )
        cases.check(
            title = "Battle map names the weakest next objective",
            expectation = "Bridge at 12 troops while secured zones are skipped",
            passed = BattleMapController.nextObjective(zones)?.zoneId == "Z2" &&
                !BattleMapController.isTheaterSecured(zones),
            detail = observed(BattleMapController.nextObjective(zones)?.zoneId, "Z2")
        )
        val push = BattleMapController.advance(zones, listOf("Z1"), BattleMapController.MISSION_PRESSURE)
            .valueOrNull()
        cases.check(
            title = "Mission pressure flips a hostile zone",
            expectation = "Z1 becomes FRIENDLY and is reported as changed",
            passed = push?.zones?.first()?.control == ZoneControl.FRIENDLY && push.changed == listOf("Z1"),
            detail = observed(push?.zones?.first()?.control, ZoneControl.FRIENDLY)
        )
        val attrition = BattleMapController.shift(zones[0], BattleMapController.ATTRITION_PRESSURE)
        cases.check(
            title = "Attrition clamps control at zero",
            expectation = "0 - 20 stays HOSTILE on the same zone",
            passed = attrition.control == ZoneControl.HOSTILE && attrition.zoneId == "Z1",
            detail = observed(attrition.control, ZoneControl.HOSTILE)
        )
        cases.check(
            title = "Unknown zones fail instead of silently moving",
            expectation = "UNKNOWN_ZONE, and one secured of three zones is a third of the map",
            passed = BattleMapController.advance(zones, listOf("Z9"), 10).errorCodeOrNull() == "UNKNOWN_ZONE" &&
                approxEquals(BattleMapController.overallControl(zones), 1f / 3f),
            detail = observed(BattleMapController.overallControl(zones), 0.3333f)
        )
    }

    private fun checkVehicles(cases: CaseRecorder) {
        val base = VehicleBase("CHASSIS_MBT", "Vanguard MBT", 42_000f, 72f, 4)
        val config = VehicleConfig(
            chassisId = "CHASSIS_MBT",
            paintId = "PAINT_OLIVE",
            armor = ArmorPackage.ARMOR_MEDIUM,
            powertrain = PowertrainPackage.POWERTRAIN_UPGRADED,
            weaponStationId = "WEAPON_AUTOCANNON"
        )
        val fitted = VehicleCustomizationBay.fit(base, config).valueOrNull()
        cases.check(
            title = "Vehicle bay assembles mass, speed and ratings",
            expectation = "42640 kg, 74 kph, armour 9, firepower 9",
            passed = fitted != null &&
                approxEquals(fitted.massKg, 42_640f) &&
                approxEquals(fitted.topSpeedKph, 74f) &&
                fitted.armorRating == 9 &&
                fitted.firepowerRating == 9,
            detail = observed("${fitted?.massKg}/${fitted?.topSpeedKph}", "42640.0/74.0")
        )
        cases.check(
            title = "Vehicle bay rejects incomplete or mismatched fits",
            expectation = "CHASSIS_MISMATCH, INCOMPLETE_SLOTS, UNKNOWN_PAINT, UNKNOWN_WEAPON_STATION",
            passed = VehicleCustomizationBay.fit(base, config.copy(chassisId = "CHASSIS_IFV"))
                .errorCodeOrNull() == "CHASSIS_MISMATCH" &&
                VehicleCustomizationBay.fit(base, config.copy(paintId = " "))
                    .errorCodeOrNull() == "INCOMPLETE_SLOTS" &&
                VehicleCustomizationBay.fit(base, config.copy(paintId = "PAINT_PINK"))
                    .errorCodeOrNull() == "UNKNOWN_PAINT" &&
                VehicleCustomizationBay.fit(base, config.copy(weaponStationId = "WEAPON_LASER"))
                    .errorCodeOrNull() == "UNKNOWN_WEAPON_STATION",
            detail = observed(VehicleCustomizationBay.PAINTS.size, 4)
        )
    }

    private fun checkUi(cases: CaseRecorder) {
        val empty = MechanicalState(truePhysicsRpm = 780f, hasChamberedRound = false, chamberedRounds = 0)
        val hud = HudController.readout(empty, magazineCapacity = 10)
        cases.check(
            title = "HUD reads the physics RPM and the empty magazine",
            expectation = "780 RPM, 0 / 10, reload prompt on, trigger not ready",
            passed = hud.rpmText == "780 RPM" &&
                hud.ammoText == "0 / 10" &&
                hud.reloadPromptVisible &&
                !hud.triggerReady,
            detail = observed("${hud.rpmText}/${hud.ammoText}", "780 RPM/0 / 10")
        )
        val obstructed = empty.copy(
            hasChamberedRound = true,
            chamberedRounds = 5,
            isMuzzleObstructedByWall = true
        )
        val blocked = HudController.readout(obstructed, magazineCapacity = 10)
        cases.check(
            title = "HUD surfaces the obstruction warning",
            expectation = "warning visible, trigger blocked, reload allowed only with room",
            passed = blocked.obstructionWarningVisible &&
                !blocked.triggerReady &&
                !blocked.reloadPromptVisible &&
                HudController.reloadIsAvailable(obstructed, 10) &&
                !HudController.reloadIsAvailable(obstructed.copy(chamberedRounds = 10), 10) &&
                !HudController.reloadIsAvailable(obstructed.copy(safety = SafetyMode.POS3_FULL_LOCK), 10),
            detail = observed(blocked.ammoText, "5 / 10")
        )
        cases.check(
            title = "Safety position gates the bolt control",
            expectation = "Pos 3 blocks, Pos 1 allows",
            passed = !HudController.boltIsAvailable(MechanicalState(safety = SafetyMode.POS3_FULL_LOCK)) &&
                HudController.boltIsAvailable(MechanicalState(safety = SafetyMode.POS1_FIRE_AND_RELOAD)),
            detail = observed(SafetyMode.POS3_FULL_LOCK.displayName, "Pos 3 - Full Lock")
        )

        cases.check(
            title = "Pause panel exposes three bounded presets",
            expectation = "HIGH hip 1.35, ADS 1.05, deadzone 0.05",
            passed = PauseSensitivityPanel.PRESETS.size == 3 &&
                approxEquals(PauseSensitivityPanel.profileFor(SensitivityPreset.HIGH).hipMultiplier, 1.35f) &&
                approxEquals(PauseSensitivityPanel.profileFor(SensitivityPreset.HIGH).adsMultiplier, 1.05f) &&
                approxEquals(PauseSensitivityPanel.profileFor(SensitivityPreset.HIGH).deadzone, 0.05f),
            detail = observed(PauseSensitivityPanel.PRESETS.size, 3)
        )
        val medium = SensitivityProfile(1f, 0.8f, 0.08f)
        cases.check(
            title = "Sensitivity rejects out-of-range values",
            expectation = "SENSITIVITY_OUT_OF_RANGE and DEADZONE_OUT_OF_RANGE",
            passed = PauseSensitivityPanel.apply(medium).valueOrNull() == medium &&
                PauseSensitivityPanel.apply(medium.copy(hipMultiplier = 4f))
                    .errorCodeOrNull() == "SENSITIVITY_OUT_OF_RANGE" &&
                PauseSensitivityPanel.apply(medium.copy(deadzone = 0.4f))
                    .errorCodeOrNull() == "DEADZONE_OUT_OF_RANGE" &&
                approxEquals(PauseSensitivityPanel.effectiveLookSpeed(medium, 100f, true), 80f) &&
                approxEquals(PauseSensitivityPanel.effectiveLookSpeed(medium, 100f, false), 100f),
            detail = observed(PauseSensitivityPanel.effectiveLookSpeed(medium, 100f, true), 80f)
        )

        val missionAd = CheckpointAdBoundary.decide(CheckpointSurface.INTERSTITIAL, CombatState.ACTIVE_MISSION)
        cases.check(
            title = "Checkpoint ads cannot interrupt a mission",
            expectation = "blocked during ACTIVE_MISSION and IDLE, allowed after completion",
            passed = !CheckpointAdBoundary.mayPresent(CombatState.ACTIVE_MISSION) &&
                CheckpointAdBoundary.mayPresent(CombatState.MISSION_COMPLETED) &&
                missionAd.reasonCode == "BLOCKED_DURING_ACTIVE_MISSION" &&
                CheckpointAdBoundary.decide(CheckpointSurface.STORE, CombatState.IDLE)
                    .reasonCode == "BLOCKED_NO_BOUNDARY" &&
                CheckpointAdBoundary.decide(CheckpointSurface.SAVE, CombatState.MISSION_FAILED).allowed,
            detail = observed(missionAd.reasonCode, "BLOCKED_DURING_ACTIVE_MISSION")
        )
        cases.check(
            title = "Mission HUD states why interstitials are locked",
            expectation = "banner mentions locked ads during the mission",
            passed = CheckpointAdBoundary.statusLine(CombatState.ACTIVE_MISSION).contains("locked") &&
                CheckpointAdBoundary.statusLine(CombatState.MISSION_FAILED).contains("retry"),
            detail = CheckpointAdBoundary.statusLine(CombatState.ACTIVE_MISSION)
        )
    }
}
