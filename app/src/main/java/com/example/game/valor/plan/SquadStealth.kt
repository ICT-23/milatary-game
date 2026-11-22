package com.example.game.valor.plan

import com.example.game.valor.gdd.RankCurve

/** Section 5.11: the five squad commands the player can issue. */
enum class SquadCommand(val displayName: String) {
    FOLLOW("Follow"),
    HOLD("Hold"),
    ATTACK_TARGET("Attack target"),
    COVER_ME("Cover me"),
    REGROUP("Regroup")
}

/** Section 5.12: what the detection meter currently means. */
enum class DetectionState(val displayName: String) {
    CALM("Calm"),
    SUSPICIOUS("Suspicious"),
    ALERT("Alert")
}

/** Section 5.11: squad size comes from the rank table, not from a second list. */
object SquadRules {

    const val DOWNED_SECONDS = 20f
    const val BLEED_OUT_IS_PERMANENT = true

    val COMMANDS: List<SquadCommand> = SquadCommand.entries

    /** Design doc Section 4: 1 mate at 5, 2 at 9, 3 at 14. */
    fun maxSize(rank: Int): Int = when {
        RankCurve.isUnlocked(rank, "Squad of 3 AI") -> 3
        RankCurve.isUnlocked(rank, "Squad of 2 AI") -> 2
        RankCurve.isUnlocked(rank, "Squad command (1 AI teammate)") -> 1
        else -> 0
    }

    fun goesDown(health: Float): Boolean = health <= 0f

    fun isDowned(secondsDown: Float): Boolean = secondsDown in 0f..DOWNED_SECONDS

    fun canBeRevived(secondsDown: Float, storyCritical: Boolean, scriptedMoment: Boolean): Boolean =
        secondsDown <= DOWNED_SECONDS && (!storyCritical || scriptedMoment)

    /** "Bleed-out → dead (permanent for mission; story-critical allies cannot die)." */
    fun bleedsOut(secondsDown: Float, storyCritical: Boolean, scriptedMoment: Boolean): Boolean =
        secondsDown >= DOWNED_SECONDS && (!storyCritical || scriptedMoment)

    /** Allies use the same ammo rules and resupply from the same crates. */
    fun resupply(ammo: AmmoState, crateAmount: Int): Pair<AmmoState, Int> =
        AmmoRules.pickup(ammo, crateAmount)

    /** "Allies never block the player's shots unless physically in the line." */
    fun blocksShotOnlyWhenInLine(): Boolean = true

    fun sameWeaponAndDamageSystems(): Boolean = true
}

/** Section 5.12: stealth detection, takedowns and the once-per-mission alarm. */
object StealthRules {

    const val METER_MIN = 0
    const val METER_MAX = 100
    const val SUSPICIOUS_THRESHOLD = 40
    const val ALERT_THRESHOLD = 100
    const val TAKEDOWN_DISTANCE_METERS = 1.5f
    const val NO_ALARM_BONUS = "no alarm"

    fun clamp(meter: Float): Float = meter.coerceIn(METER_MIN.toFloat(), METER_MAX.toFloat())

    /**
     * The four risk factors the document names. Each one adds a step; the step
     * size is tuning data held by the caller.
     */
    fun riskFactors(close: Boolean, standing: Boolean, moving: Boolean, lit: Boolean): Int =
        listOf(close, standing, moving, lit).count { it }

    fun addDetection(meter: Float, riskFactors: Int, stepPerFactor: Float): Float =
        clamp(meter + riskFactors.coerceAtLeast(0) * stepPerFactor)

    fun drainDetection(meter: Float, stepPerSecond: Float, deltaSeconds: Float): Float =
        clamp(meter - stepPerSecond.coerceAtLeast(0f) * deltaSeconds)

    fun stateFor(meter: Float): DetectionState = when {
        clamp(meter) >= ALERT_THRESHOLD -> DetectionState.ALERT
        clamp(meter) >= SUSPICIOUS_THRESHOLD -> DetectionState.SUSPICIOUS
        else -> DetectionState.CALM
    }

    /** "From behind within 1.5 m, enemy not Alert, melee or suppressed weapon." */
    fun canTakedown(
        distanceMeters: Float,
        fromBehind: Boolean,
        enemyAlert: Boolean,
        silentWeapon: Boolean
    ): Boolean =
        distanceMeters <= TAKEDOWN_DISTANCE_METERS &&
            fromBehind &&
            !enemyAlert &&
            silentWeapon

    /** Bodies found by enemies raise alert, so hiding them matters. */
    fun bodyFound(alertMeter: Float, step: Float): Float = addDetection(alertMeter, 1, step)

    fun noAlarmBonus(): String = NO_ALARM_BONUS
}

/** Section 5.12 "Alarm: only once per mission." */
class AlarmState {

    var triggered: Boolean = false
        private set

    /** Returns true only for the first pull of the mission. */
    fun trigger(): Boolean {
        if (triggered) return false
        triggered = true
        return true
    }

    fun resetForNewMission() {
        triggered = false
    }
}
