package com.example.game.vanguard

import com.example.game.combat.BallisticBody
import com.example.game.combat.Vec3
import com.example.game.vanguard.combat.AimRequest
import com.example.game.vanguard.combat.AnatomicalDamage
import com.example.game.vanguard.combat.AnatomicalHitbox
import com.example.game.vanguard.combat.BoundingBoxGuard
import com.example.game.vanguard.combat.ClearanceProbe
import com.example.game.vanguard.combat.DualVectorBallistics
import com.example.game.vanguard.combat.FireControl
import com.example.game.vanguard.combat.MechanicalState
import com.example.game.vanguard.combat.MozambiqueTracker
import com.example.game.vanguard.combat.PoseAction
import com.example.game.vanguard.combat.Posture
import com.example.game.vanguard.combat.RecoilModel
import com.example.game.vanguard.combat.RecoilState
import com.example.game.vanguard.combat.SafetyMode
import com.example.game.vanguard.combat.Stance
import com.example.game.vanguard.combat.TargetArmor
import com.example.game.vanguard.core.errorCodeOrNull
import com.example.game.vanguard.core.errorOrNull
import com.example.game.vanguard.core.isOk
import com.example.game.vanguard.core.valueOrNull
import com.example.game.vanguard.gunsmith.KickOffBuffer

/**
 * Section 3.1: the six gates in the order the specification lists them. Each
 * gate is checked with numbers, never with a probability roll, so the audit
 * result is identical on every device.
 */
object CombatContractCases {

    fun run(): List<ContractCase> {
        val cases = CaseRecorder(VanguardContractAudit.GROUP_COMBAT)
        checkMechanicalReadiness(cases)
        checkSightAndImmediateCover(cases)
        checkAnatomy(cases)
        checkStanceAndRecoil(cases)
        checkBoundingBox(cases)
        return cases.result()
    }

    // ------------------------------------------------ gate 1: readiness
    private fun checkMechanicalReadiness(cases: CaseRecorder) {
        cases.check(
            title = "Gate 1: charged, safety-off weapon authorises the trigger",
            expectation = "Ok",
            passed = FireControl.evaluate(0f, MechanicalState()).isOk,
            detail = "state=${MechanicalState()}"
        )

        val pos2 = FireControl.evaluate(0f, MechanicalState(safety = SafetyMode.POS2_SAFE_LOAD_UNLOAD))
        cases.check(
            title = "Gate 1: Pos 2 blocks the trigger but leaves the bolt free",
            expectation = "SAFETY_BLOCKED",
            passed = pos2.errorCodeOrNull() == "SAFETY_BLOCKED" && SafetyMode.POS2_SAFE_LOAD_UNLOAD.allowsBoltCycle,
            detail = "code=${pos2.errorCodeOrNull()} boltCycle=${SafetyMode.POS2_SAFE_LOAD_UNLOAD.allowsBoltCycle}"
        )

        val pos3 = FireControl.chamberRound(MechanicalState(safety = SafetyMode.POS3_FULL_LOCK))
        cases.check(
            title = "Gate 1: Pos 3 locks the bolt carrier",
            expectation = "BOLT_LOCKED",
            passed = pos3.errorCodeOrNull() == "BOLT_LOCKED",
            detail = observed(pos3.errorCodeOrNull(), "BOLT_LOCKED")
        )

        val firstShot = FireControl.discharge(0f, MechanicalState(truePhysicsRpm = 780f)).valueOrNull()
        cases.check(
            title = "Gate 1: cyclic rate is 60.0 / TruePhysicsRPM and the HUD reads it",
            expectation = "0.0769 s interval, HUD 780 rpm",
            passed = firstShot != null &&
                approxEquals(firstShot.state.fireIntervalSeconds, 60f / 780f, 0.0001f) &&
                firstShot.state.hudRpm == 780,
            detail = "interval=${firstShot?.state?.fireIntervalSeconds} hudRpm=${firstShot?.state?.hudRpm}"
        )

        val rechambered = firstShot?.state?.copy(chamberedRounds = 1, hasChamberedRound = true)
        val early = rechambered?.let { FireControl.discharge(0.01f, it) }
        cases.check(
            title = "Gate 1: a second pull inside the interval is rate limited",
            expectation = "RATE_LIMITED even with a round chambered",
            passed = early?.errorCodeOrNull() == "RATE_LIMITED",
            detail = observed(early?.errorCodeOrNull(), "RATE_LIMITED")
        )

        val noRound = FireControl.evaluate(0f, MechanicalState(hasChamberedRound = false, chamberedRounds = 0))
        cases.check(
            title = "Gate 1: an empty chamber refuses the shot",
            expectation = "NO_CHAMBERED_ROUND",
            passed = noRound.errorCodeOrNull() == "NO_CHAMBERED_ROUND",
            detail = observed(noRound.errorCodeOrNull(), "NO_CHAMBERED_ROUND")
        )
    }

    // -------------------------------------- gates 2 and 3: sight + cover
    private fun checkSightAndImmediateCover(cases: CaseRecorder) {
        val ads = DualVectorBallistics.alignDownSights(Vec3(0f, 0f, 1f), cameraFovDegrees = 55f)
        val adsWide = DualVectorBallistics.alignDownSights(Vec3(0f, 0f, 1f), cameraFovDegrees = 12f)
        cases.check(
            title = "Gate 2: the ADS camera follows the optic socket, not the FOV",
            expectation = "cameraForward equals opticSightForward at 12x and 55x FOV",
            passed = ads.isFovDecoupled && adsWide.cameraForward == ads.cameraForward && ads.cameraFovDegrees == 55f,
            detail = "decoupled=${ads.isFovDecoupled} fovIndependent=${adsWide.cameraForward == ads.cameraForward}"
        )

        val offAxis = DualVectorBallistics.resolve(
            AimRequest(
                opticSightOrigin = Vec3.Zero,
                opticSightDirection = Vec3(0f, 0f, 1f),
                muzzleOrigin = Vec3(0.2f, 0f, 0f),
                hitPoint = Vec3(10f, 0f, 100f),
                angularToleranceDegrees = 1f,
                mechanicalDispersionMoa = 1f
            )
        )
        cases.check(
            title = "Gate 3: the sight vector must land inside the aiming cone",
            expectation = "NOT_AIMED_AT_TARGET at 5.7 deg of miss",
            passed = offAxis.errorCodeOrNull() == "NOT_AIMED_AT_TARGET",
            detail = observed(offAxis.errorCodeOrNull(), "NOT_AIMED_AT_TARGET")
        )

        val blockedShot = DualVectorBallistics.resolve(
            AimRequest(
                opticSightOrigin = Vec3.Zero,
                opticSightDirection = Vec3(0f, 0f, 1f),
                muzzleOrigin = Vec3.Zero,
                hitPoint = Vec3(0f, 0f, 50f),
                coverBodies = listOf(BallisticBody.wall("WALL_TEST", Vec3(0f, 0f, 20f), 2f))
            )
        )
        cases.check(
            title = "Gate 3: cover between muzzle and impact voids the shot",
            expectation = "TRAJECTORY_BLOCKED naming the wall, damage multiplier 0.0",
            passed = blockedShot.errorCodeOrNull() == "TRAJECTORY_BLOCKED" &&
                blockedShot.errorOrNull()?.detail?.get("blockingBodyId") == "WALL_TEST" &&
                DualVectorBallistics.damageMultiplierFor(blockedShot) == 0f,
            detail = "code=${blockedShot.errorCodeOrNull()} multiplier=${DualVectorBallistics.damageMultiplierFor(blockedShot)}"
        )

        val clean = DualVectorBallistics.resolve(
            AimRequest(
                opticSightOrigin = Vec3.Zero,
                opticSightDirection = Vec3(0f, 0f, 1f),
                muzzleOrigin = Vec3(0.2f, 0f, 0.4f),
                hitPoint = Vec3(0f, 0f, 50f)
            )
        )
        cases.check(
            title = "Gate 3: an aligned, unobstructed line returns a full damage multiplier",
            expectation = "Ok with multiplier 1.0",
            passed = clean.isOk && DualVectorBallistics.damageMultiplierFor(clean) == 1f,
            detail = "code=${clean.errorCodeOrNull()} multiplier=${DualVectorBallistics.damageMultiplierFor(clean)}"
        )
    }

    // --------------------------------------------- gate 4: anatomy
    private fun checkAnatomy(cases: CaseRecorder) {
        val plate = TargetArmor(plateRating = 4, ammoPenetrationRating = 2)

        val skull = AnatomicalDamage.resolve(AnatomicalHitbox.SKULL, baseDamage = 100f, armor = plate).valueOrNull()
        cases.check(
            title = "Gate 4: the skull bypasses the plate at 3.5x",
            expectation = "350 damage, not absorbed",
            passed = skull != null && approxEquals(skull.damage, 350f) && !skull.absorbedByPlate,
            detail = "damage=${skull?.damage} absorbed=${skull?.absorbedByPlate}"
        )

        val torso = AnatomicalDamage.resolve(AnatomicalHitbox.TORSO, baseDamage = 100f, armor = plate).valueOrNull()
        cases.check(
            title = "Gate 4: the torso plate stops an under-rated round",
            expectation = "0 damage, absorbed by plate",
            passed = torso != null && torso.damage == 0f && torso.absorbedByPlate,
            detail = "damage=${torso?.damage} absorbed=${torso?.absorbedByPlate}"
        )

        val pelvis = AnatomicalDamage.resolve(AnatomicalHitbox.PELVIS, baseDamage = 100f, armor = plate).valueOrNull()
        cases.check(
            title = "Gate 4: the pelvis bypasses the plate at 1.1x and collapses mobility",
            expectation = "110 damage, mobility collapsed",
            passed = pelvis != null && approxEquals(pelvis.damage, 110f) && pelvis.mobilityCollapsed,
            detail = "damage=${pelvis?.damage} mobilityCollapsed=${pelvis?.mobilityCollapsed}"
        )

        val limb = AnatomicalDamage.resolve(AnatomicalHitbox.LIMBS, baseDamage = 100f).valueOrNull()
        cases.check(
            title = "Gate 4: limbs take 0.45x and split their penalties",
            expectation = "45 damage, 0.25 arm sway, 0.35 leg slow",
            passed = limb != null &&
                approxEquals(limb.damage, 45f) &&
                approxEquals(AnatomicalDamage.limbPenalty(AnatomicalHitbox.LIMBS, isArm = true), 0.25f) &&
                approxEquals(AnatomicalDamage.limbPenalty(AnatomicalHitbox.LIMBS, isArm = false), 0.35f),
            detail = "damage=${limb?.damage} arm=${AnatomicalDamage.limbPenalty(AnatomicalHitbox.LIMBS, true)} leg=${AnatomicalDamage.limbPenalty(AnatomicalHitbox.LIMBS, false)}"
        )

        cases.check(
            title = "Gate 4: a penetrating hit produces entry and exit wounds",
            expectation = "both decals, 562.5 J residual",
            passed = limb != null && limb.entryWoundDecal && limb.exitWoundDecal &&
                approxEquals(limb.residualEnergyJoules, 562.5f),
            detail = "entry=${limb?.entryWoundDecal} exit=${limb?.exitWoundDecal} residual=${limb?.residualEnergyJoules}"
        )

        val drill = MozambiqueTracker()
        drill.record(AnatomicalHitbox.TORSO)
        val afterTwo = drill.record(AnatomicalHitbox.TORSO)
        val afterSkull = drill.record(AnatomicalHitbox.SKULL)
        cases.check(
            title = "Gate 4: Mozambique drill - two sternum hits, then a cranial finisher",
            expectation = "staggered after 2 torso hits, finisher on the skull shot",
            passed = afterTwo.torsoHits == 2 && afterTwo.staggered && afterSkull.finisher,
            detail = "torsoHits=${afterTwo.torsoHits} staggered=${afterTwo.staggered} finisher=${afterSkull.finisher}"
        )
    }

    // -------------------------------------- gate 5: stance, posture, recoil
    private fun checkStanceAndRecoil(cases: CaseRecorder) {
        val impulse = RecoilModel.impulsePerShot(1.9f, Stance.ISOSCELES, Posture.STANDING, 0f)
        var recoil = RecoilState()
        repeat(5) { recoil = RecoilModel.apply(recoil, impulse) }
        cases.check(
            title = "Gate 5: recoil accumulates over consecutive shots",
            expectation = "cumulativeImpulse equals five shots, consecutiveShots 5",
            passed = approxEquals(recoil.cumulativeImpulse, impulse * 5f, 0.001f) && recoil.consecutiveShots == 5,
            detail = "cumulative=${recoil.cumulativeImpulse} shots=${recoil.consecutiveShots}"
        )

        val mega = RecoilModel.impulsePerShot(2f, Stance.ISOSCELES, Posture.STANDING, KickOffBuffer.KICK_OFF_MEGA.reduction)
        cases.check(
            title = "Gate 5: Kick-Off Mega removes up to 70% of the impulse",
            expectation = "2.0 base becomes 0.6",
            passed = approxEquals(KickOffBuffer.KICK_OFF_MEGA.reduction, 0.70f) && approxEquals(mega, 0.6f),
            detail = observed(mega, 0.6f)
        )

        val prone = RecoilModel.impulsePerShot(2f, Stance.ISOSCELES, Posture.PRONE, 0f)
        cases.check(
            title = "Gate 5: prone posture halves the delivered impulse",
            expectation = "2.0 base becomes 1.1 at 0.55",
            passed = approxEquals(prone, 1.1f),
            detail = observed(prone, 1.1f)
        )

        val darkHarries = RecoilModel.darknessPenaltyDegrees(Stance.HARRIES, isDark = true, insideBeamCone = true)
        val darkIsosceles = RecoilModel.darknessPenaltyDegrees(Stance.ISOSCELES, isDark = true, insideBeamCone = true)
        cases.check(
            title = "Gate 5: the Harries flashlight removes the darkness penalty inside its beam",
            expectation = "0 deg for Harries, 2.5 deg otherwise",
            passed = darkHarries == 0f && approxEquals(darkIsosceles, 2.5f),
            detail = "harries=$darkHarries isosceles=$darkIsosceles"
        )
    }

    // --------------------------------- gate 6: bounding box and pose guards
    private fun checkBoundingBox(cases: CaseRecorder) {
        val tight = BoundingBoxGuard.evaluate(
            ClearanceProbe(obstacleDistancesMeters = listOf(0.30f), weaponOverallLengthMeters = 1.0f)
        )
        val tightShot = FireControl.evaluate(0f, BoundingBoxGuard.applyTo(MechanicalState(), tight))
        cases.check(
            title = "Gate 6: a barrel inside the envelope enters compressed ready",
            expectation = "canFire false, blend > 0, MUZZLE_OBSTRUCTED on the trigger",
            passed = tight.weaponCompressed && !tight.canFire && tight.compressedReadyBlend > 0f &&
                tightShot.errorCodeOrNull() == "MUZZLE_OBSTRUCTED",
            detail = "compressed=${tight.weaponCompressed} blend=${tight.compressedReadyBlend} code=${tightShot.errorCodeOrNull()}"
        )

        val roomy = BoundingBoxGuard.evaluate(
            ClearanceProbe(obstacleDistancesMeters = listOf(3f), weaponOverallLengthMeters = 1.0f)
        )
        cases.check(
            title = "Gate 6: clear space restores the full firing pose",
            expectation = "canFire true, no compression",
            passed = roomy.canFire && !roomy.weaponCompressed && roomy.muzzleClearanceMeters > 0f,
            detail = "clearance=${roomy.muzzleClearanceMeters} compressed=${roomy.weaponCompressed}"
        )

        cases.check(
            title = "Gate 6: pose transitions never overlap prone with melee, lean or sprint",
            expectation = "prone-to-melee and prone-to-sprint rejected, idle-to-sprint allowed",
            passed = !BoundingBoxGuard.transitionAllowed(PoseAction.PRONE_TRANSITION, PoseAction.MELEE_ATTACK) &&
                !BoundingBoxGuard.transitionAllowed(PoseAction.PRONE_TRANSITION, PoseAction.SPRINT) &&
                BoundingBoxGuard.transitionAllowed(PoseAction.IDLE, PoseAction.SPRINT),
            detail = "proneMelee=${BoundingBoxGuard.transitionAllowed(PoseAction.PRONE_TRANSITION, PoseAction.MELEE_ATTACK)} idleSprint=${BoundingBoxGuard.transitionAllowed(PoseAction.IDLE, PoseAction.SPRINT)}"
        )
    }
}
