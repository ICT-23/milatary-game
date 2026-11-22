package com.example.game.valor.plan

import com.example.game.valor.gdd.MissionMastery

/** Section 5.14: the ten mission states, in document order. */
enum class MissionState(val displayName: String) {
    LOADING("Loading"),
    BRIEFING("Briefing"),
    INTRO("Intro/Cutscene"),
    PLAYING("Playing"),
    PAUSED("Paused"),
    COMPLETING("Completing"),
    SUCCESS("Success"),
    FAILED("Failed"),
    RESULTS("Results"),
    EXIT("Exit")
}

/** Section 5.14: the ten objective types. */
enum class ObjectiveType(val displayName: String) {
    KILL_ALL("KillAll"),
    KILL_COUNT("KillCount"),
    REACH_ZONE("ReachZone"),
    DEFEND_TIMER("DefendTimer"),
    ESCORT("Escort"),
    PLANT_DEFUSE("Plant/Defuse"),
    COLLECT("Collect"),
    SURVIVE_WAVES("SurviveWaves"),
    DRONE_STRIKE("DroneStrike"),
    STEALTH("Stealth")
}

/** Section 5.14: each objective owns its own state. */
enum class ObjectiveStatus { LOCKED, ACTIVE, COMPLETED, FAILED }

/** Section 5.14: the four ways a mission can fail. */
enum class FailReason(val displayName: String) {
    PLAYER_DEAD("Player dead and no revive"),
    PROTECTED_TARGET_DIED("Protected NPC or asset died"),
    TIMER_ENDED("Timed objective ran out"),
    ALARM_ON_STEALTH("Alarm on a stealth-required objective")
}

/** Section 5.14: one objective, transitioned only through its own methods. */
class Objective(
    val id: String,
    val type: ObjectiveType,
    val required: Boolean = true
) {
    var status: ObjectiveStatus = ObjectiveStatus.LOCKED
        private set

    val isComplete: Boolean get() = status == ObjectiveStatus.COMPLETED

    fun activate(): Boolean {
        if (status != ObjectiveStatus.LOCKED) return false
        status = ObjectiveStatus.ACTIVE
        return true
    }

    fun complete(): Boolean {
        if (status != ObjectiveStatus.ACTIVE) return false
        status = ObjectiveStatus.COMPLETED
        return true
    }

    fun fail(): Boolean {
        if (status == ObjectiveStatus.COMPLETED || status == ObjectiveStatus.FAILED) return false
        status = ObjectiveStatus.FAILED
        return true
    }
}

/**
 * Section 5.14: the state machine. Only the transitions below are legal, so a
 * mission can never jump from Loading straight to Results.
 */
class MissionRun(val objectives: List<Objective>) {

    var state: MissionState = MissionState.LOADING
        private set

    var failReason: FailReason? = null
        private set

    val requiredObjectives: List<Objective> get() = objectives.filter { it.required }

    val allRequiredComplete: Boolean
        get() = requiredObjectives.isNotEmpty() && requiredObjectives.all { it.isComplete }

    val anyRequiredFailed: Boolean get() = requiredObjectives.any { it.status == ObjectiveStatus.FAILED }

    fun canTransition(to: MissionState): Boolean =
        to in MissionFsm.TRANSITIONS[state].orEmpty()

    fun transition(to: MissionState, log: DebugLog? = null): Boolean {
        if (!canTransition(to)) {
            log?.record(LogCategory.MISSION, "Invalid transition $state -> $to")
            return false
        }
        state = to
        return true
    }

    /** Called after every objective update; decides Completing or Failed. */
    fun evaluate(): MissionState = when {
        anyRequiredFailed -> MissionState.FAILED
        allRequiredComplete -> MissionState.COMPLETING
        else -> MissionState.PLAYING
    }
}

object MissionFsm {

    const val VALID_TRANSITIONS_ONLY = true
    const val WATCHDOG_HINT_SECONDS = 60f
    const val WATCHDOG_AUTOCOMPLETE_SECONDS = 120f
    const val KILL_PLANE_NO_XP = true

    /** The legal edges; every other pair is rejected and logged. */
    val TRANSITIONS: Map<MissionState, Set<MissionState>> = mapOf(
        MissionState.LOADING to setOf(MissionState.BRIEFING),
        MissionState.BRIEFING to setOf(MissionState.INTRO),
        MissionState.INTRO to setOf(MissionState.PLAYING),
        MissionState.PLAYING to setOf(MissionState.PAUSED, MissionState.COMPLETING, MissionState.FAILED),
        MissionState.PAUSED to setOf(MissionState.PLAYING),
        MissionState.COMPLETING to setOf(MissionState.SUCCESS, MissionState.FAILED),
        MissionState.SUCCESS to setOf(MissionState.RESULTS),
        MissionState.FAILED to setOf(MissionState.RESULTS),
        MissionState.RESULTS to setOf(MissionState.EXIT),
        MissionState.EXIT to emptySet()
    )

    fun transitionCount(): Int = TRANSITIONS.values.sumOf { it.size }
}

/**
 * Section 5.14: living enemies are counted by registry, never by scene objects,
 * so a body that falls off the map cannot block the objective.
 */
class EnemyRegistry {

    private val living = linkedSetOf<String>()

    fun spawn(id: String) {
        living += id
    }

    fun kill(id: String): Boolean = living.remove(id)

    /** Safe cleanup (despawn) counts as removal for progress purposes. */
    fun despawn(id: String): Boolean = living.remove(id)

    fun fallOffMap(id: String): Boolean = kill(id)

    fun livingCount(): Int = living.size

    fun isEmpty(): Boolean = living.isEmpty()

    fun livingIds(): List<String> = living.toList()

    fun killPlaneCrossed(y: Float, killPlaneY: Float): Boolean = y < killPlaneY
}

/** Section 5.14: "60 s hint, 120 s auto-complete the stuck objective". */
data class Watchdog(val noProgressSeconds: Float, val activeTargets: Int) {

    fun shouldHint(): Boolean = noProgressSeconds >= MissionFsm.WATCHDOG_HINT_SECONDS

    fun shouldAutoComplete(): Boolean =
        noProgressSeconds >= MissionFsm.WATCHDOG_AUTOCOMPLETE_SECONDS && activeTargets <= 0
}

/** Section 5.14: a checkpoint restores exactly these facts on Retry. */
data class Checkpoint(
    val objectiveId: String,
    val playerHealth: Float,
    val playerAmmoTotal: Int,
    val completedObjectiveIds: List<String>
)

class CheckpointStore {

    var last: Checkpoint? = null
        private set

    fun save(checkpoint: Checkpoint) {
        last = checkpoint
    }

    /** "Saved at objective completion; cleared on mission end." */
    fun clear() {
        last = null
    }
}

/**
 * Section 5.14: stars from data, then XP and cash granted exactly once behind a
 * guard flag, then saved.
 */
class RewardGrant {

    var granted: Boolean = false
        private set

    fun grant(xp: Int, cash: Int, apply: (Int, Int) -> Unit): Boolean {
        if (granted) return false
        granted = true
        apply(xp, cash)
        return true
    }
}

object MissionResults {

    /** Time, accuracy and no-damage, exactly the three criteria in the design table. */
    fun stars(
        elapsedSeconds: Float,
        timeTargetSeconds: Float,
        accuracyPercent: Int,
        accuracyTargetPercent: Int,
        tookDamage: Boolean
    ): Int = MissionMastery.stars(
        elapsedSeconds, timeTargetSeconds, accuracyPercent, accuracyTargetPercent, tookDamage
    )

    fun accuracyPercent(hits: Int, shots: Int): Int =
        if (shots <= 0) 0 else (hits.toFloat() / shots * 100f).toInt().coerceIn(0, 100)
}
