package com.example.game.valor.plan

import com.example.game.vanguard.CaseRecorder
import com.example.game.vanguard.ContractCase
import com.example.game.vanguard.approxEquals
import com.example.game.vanguard.observed

/** Sections 5.10 to 5.12: enemy AI, squad and stealth. */
object PlanStealthCases {

    fun run(): List<ContractCase> {
        val r = CaseRecorder(ValorPlanAudit.GROUP_FIELD)
        aiCases(r)
        squadCases(r)
        stealthCases(r)
        return r.result()
    }

    private fun aiCases(r: CaseRecorder) {
        r.check(
            "Eight AI states", "Idle/Patrol, Suspicious, Alert, Combat, Flank, TakeCover, Retreat/Heal, Dead",
            CombatAiState.entries.map { it.displayName } == listOf(
                "Idle/Patrol", "Suspicious", "Alert", "Combat",
                "Flank", "TakeCover", "Retreat/Heal", "Dead"
            ),
            observed(CombatAiState.entries.size, 8)
        )
        r.check(
            "Transition table", "a noise makes a patroller suspicious, sight makes it alert",
            EnemyAiRules.TRANSITIONS.size == 12 &&
                EnemyAiRules.next(CombatAiState.IDLE_PATROL, AiStimulus.HEARD_NOISE) == CombatAiState.SUSPICIOUS &&
                EnemyAiRules.next(CombatAiState.ALERT, AiStimulus.SAW_TARGET) == CombatAiState.COMBAT,
            observed(EnemyAiRules.TRANSITIONS.size, 12)
        )
        r.check(
            "Death is terminal", "killed from any state lands in Dead and never leaves",
            EnemyAiRules.next(CombatAiState.COMBAT, AiStimulus.KILLED) == CombatAiState.DEAD &&
                EnemyAiRules.next(CombatAiState.DEAD, AiStimulus.SAW_TARGET) == CombatAiState.DEAD &&
                CombatAiState.entries.all { EnemyAiRules.isReachable(it) },
            observed(EnemyAiRules.next(CombatAiState.DEAD, AiStimulus.SAW_TARGET), CombatAiState.DEAD)
        )
        r.check(
            "Perception numbers", "a 90 degree cone, 40 m range and 8 s of memory",
            approxEquals(EnemyAiRules.VISION_CONE_DEGREES, 90f) &&
                approxEquals(EnemyAiRules.VISION_RANGE_METERS, 40f) &&
                approxEquals(EnemyAiRules.MEMORY_SECONDS, 8f),
            observed(EnemyAiRules.VISION_CONE_DEGREES, 90f)
        )
        r.check(
            "Sees requires line of sight", "a wall or smoke hides the player",
            EnemyAiRules.sees(Perception(insideCone = true, losClear = true)) &&
                !EnemyAiRules.sees(Perception(insideCone = true, losClear = false)) &&
                !EnemyAiRules.sees(Perception(insideCone = true, losClear = true, smokeBlocked = true)) &&
                !EnemyAiRules.sees(Perception(insideCone = false, losClear = true)) &&
                !EnemyAiRules.sees(Perception(insideCone = true, losClear = true, targetAlive = false)),
            observed(EnemyAiRules.sees(Perception(insideCone = true, losClear = false)), false)
        )
        r.check(
            "Memory window", "the last known position survives eight seconds",
            EnemyAiRules.remembers(8f) && !EnemyAiRules.remembers(8.1f),
            observed(EnemyAiRules.remembers(8.1f), false)
        )
        r.check(
            "Alert propagation", "20 m of shout, answered after 1 to 2 s",
            EnemyAiRules.alertsAllies(20f) && !EnemyAiRules.alertsAllies(20.1f) &&
                approxEquals(EnemyAiRules.alertDelay(0.2f), 1f) &&
                approxEquals(EnemyAiRules.alertDelay(5f), 2f),
            observed(EnemyAiRules.alertDelay(0.2f), 1f)
        )
        r.check(
            "Reaction delay", "the first shot waits 0.3 to 0.8 s",
            approxEquals(EnemyAiRules.reactionDelay(0.1f), 0.3f) &&
                approxEquals(EnemyAiRules.reactionDelay(2f), 0.8f) &&
                !EnemyAiRules.canFire(0.29f, 0.3f) &&
                EnemyAiRules.canFire(0.31f, 0.3f),
            observed(EnemyAiRules.reactionDelay(0.1f), 0.3f)
        )
        r.check(
            "Aim tightens", "time in sight shrinks the error, movement and cover grow it",
            EnemyAiRules.aimErrorDegrees(
                AimErrorInputs(2f, 1f, targetMoving = false, distanceMeters = 10f, targetInCover = false),
                tighteningPerSecond = 0.5f, movementPenalty = 0.5f,
                distancePenaltyPerMeter = 0f, coverPenalty = 0f
            ) < EnemyAiRules.aimErrorDegrees(
                AimErrorInputs(2f, 0f, targetMoving = false, distanceMeters = 10f, targetInCover = false),
                tighteningPerSecond = 0.5f, movementPenalty = 0.5f,
                distancePenaltyPerMeter = 0f, coverPenalty = 0f
            ),
            observed("tighter after a second in sight", true)
        )
        r.check(
            "Retreat and fallback", "below 30% with a medic, or a squad loss above 50%",
            EnemyAiRules.retreats(0.29f, hasMedic = true) &&
                !EnemyAiRules.retreats(0.29f, hasMedic = false) &&
                !EnemyAiRules.retreats(0.31f, hasMedic = true) &&
                EnemyAiRules.fallsBack(0.51f) && !EnemyAiRules.fallsBack(0.5f),
            observed(EnemyAiRules.retreats(0.29f, false), false)
        )
        r.check(
            "Flanking", "one or two flankers path out once cover is held long enough",
            EnemyAiRules.flankers(11f, 10f) == listOf(1, 2) &&
                EnemyAiRules.flankers(10f, 10f).isEmpty(),
            observed(EnemyAiRules.flankers(11f, 10f), listOf(1, 2))
        )
        r.check(
            "Tick budget", "4/6/8 active AIs by tier, far bodies tick at 10 Hz",
            EnemyAiRules.ACTIVE_AI_BY_TIER == listOf(4, 6, 8) &&
                EnemyAiRules.activeAiCap(0) == 4 &&
                EnemyAiRules.activeAiCap(1) == 6 &&
                EnemyAiRules.activeAiCap(2) == 8 &&
                approxEquals(EnemyAiRules.updateHz(isFar = true), 10f),
            observed(EnemyAiRules.ACTIVE_AI_BY_TIER, listOf(4, 6, 8))
        )
        r.check(
            "Misses are misses", "an AI shot that misses deals nothing, stuck AI recovers",
            !EnemyAiRules.AI_MISSES_DEAL_DAMAGE &&
                EnemyAiRules.shouldRecoverFromStuck(5f, outOfSight = true) &&
                !EnemyAiRules.shouldRecoverFromStuck(5f, outOfSight = false),
            observed(EnemyAiRules.AI_MISSES_DEAL_DAMAGE, false)
        )
    }

    private fun squadCases(r: CaseRecorder) {
        r.check(
            "Five commands", "Follow, Hold, Attack target, Cover me, Regroup",
            SquadCommand.entries.map { it.displayName } ==
                listOf("Follow", "Hold", "Attack target", "Cover me", "Regroup"),
            observed(SquadCommand.entries.size, 5)
        )
        r.check(
            "Downed timer", "twenty seconds of bleed-out, then death",
            approxEquals(SquadRules.DOWNED_SECONDS, 20f) &&
                SquadRules.isDowned(19.9f) &&
                !SquadRules.canBeRevived(20.1f, storyCritical = false, scriptedMoment = false) &&
                SquadRules.canBeRevived(19f, false, false) &&
                SquadRules.bleedsOut(20f, false, false),
            observed(SquadRules.DOWNED_SECONDS, 20f)
        )
        r.check(
            "Story-critical ally", "only a scripted moment may kill a critical ally",
            !SquadRules.bleedsOut(25f, storyCritical = true, scriptedMoment = false) &&
                SquadRules.bleedsOut(25f, storyCritical = true, scriptedMoment = true),
            observed(SquadRules.bleedsOut(25f, true, false), false)
        )
        r.check(
            "Squad size by rank", "one mate at Sergeant, two at Sergeant Major, three at Lt Colonel",
            SquadRules.maxSize(4) == 0 &&
                SquadRules.maxSize(5) == 1 &&
                SquadRules.maxSize(9) == 2 &&
                SquadRules.maxSize(14) == 3,
            observed(
                listOf(SquadRules.maxSize(4), SquadRules.maxSize(5), SquadRules.maxSize(9), SquadRules.maxSize(14)),
                listOf(0, 1, 2, 3)
            )
        )
        r.check(
            "Allies share the ammo rules", "a crate resupplies an ally through AmmoRules",
            SquadRules.resupply(AmmoState(10, 30, 110, 120), 20).second == 10 &&
                SquadRules.sameWeaponAndDamageSystems() &&
                SquadRules.blocksShotOnlyWhenInLine(),
            observed(SquadRules.resupply(AmmoState(10, 30, 110, 120), 20).second, 10)
        )
    }

    private fun stealthCases(r: CaseRecorder) {
        r.check(
            "Meter range", "0 to 100, clamped at both ends",
            StealthRules.METER_MIN == 0 && StealthRules.METER_MAX == 100 &&
                approxEquals(StealthRules.clamp(140f), 100f) &&
                approxEquals(StealthRules.clamp(-5f), 0f),
            observed(StealthRules.clamp(140f), 100f)
        )
        r.check(
            "Two thresholds", "Suspicious at 40, Alert at 100",
            StealthRules.stateFor(39.9f) == DetectionState.CALM &&
                StealthRules.stateFor(40f) == DetectionState.SUSPICIOUS &&
                StealthRules.stateFor(99.9f) == DetectionState.SUSPICIOUS &&
                StealthRules.stateFor(100f) == DetectionState.ALERT,
            observed(StealthRules.stateFor(40f), DetectionState.SUSPICIOUS)
        )
        r.check(
            "Risk factors", "close, standing, moving and lit each add a step",
            StealthRules.riskFactors(true, true, true, true) == 4 &&
                StealthRules.riskFactors(false, false, false, false) == 0 &&
                approxEquals(StealthRules.addDetection(10f, 4, 5f), 30f) &&
                approxEquals(StealthRules.drainDetection(30f, 5f, 2f), 20f),
            observed(StealthRules.riskFactors(true, true, true, true), 4)
        )
        r.check(
            "Takedown", "1.5 m from behind, target not alert, silent weapon",
            approxEquals(StealthRules.TAKEDOWN_DISTANCE_METERS, 1.5f) &&
                StealthRules.canTakedown(1.5f, fromBehind = true, enemyAlert = false, silentWeapon = true) &&
                !StealthRules.canTakedown(1.6f, true, false, true) &&
                !StealthRules.canTakedown(1f, fromBehind = false, enemyAlert = false, silentWeapon = true) &&
                !StealthRules.canTakedown(1f, true, enemyAlert = true, silentWeapon = true),
            observed(StealthRules.canTakedown(1f, false, false, true), false)
        )
        val alarm = AlarmState()
        r.check(
            "Alarm once", "the first pull succeeds and every later pull is refused",
            alarm.trigger() && !alarm.trigger() && alarm.triggered,
            observed(alarm.trigger(), false)
        )
    }
}
