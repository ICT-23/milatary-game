package com.example.game.valor.gdd

/** Section 7.4: one row of the enemy table. */
data class EnemyBehaviorRow(val enemy: String, val behavior: String)

/** Section 7.4: the six AI states of the state machine. */
enum class AiState(val displayName: String) {
    PATROL("Patrol"),
    ALERT("Alert"),
    COMBAT("Combat"),
    FLANK("Flank"),
    RETREAT("Retreat/Heal"),
    DEAD("Dead")
}

/** Section 7.4: the stimuli the machine reacts to. */
enum class AiEvent(val displayName: String) {
    HEARD_GUNSHOT("Heard a gunshot"),
    SAW_TARGET("Target inside the vision cone"),
    LOST_TARGET("Target lost"),
    FLANK_TIMER("Flank window reached"),
    TOOK_HEAVY_DAMAGE("Took heavy damage"),
    HEALTH_RESTORED("Health restored"),
    KILLED("Killed")
}

/** One arrow of the documented state machine. */
data class AiTransition(val from: AiState, val event: AiEvent, val to: AiState)

object EnemyRoster {

    /** Section 7.4 enemy table, transcribed row for row. */
    val ENEMIES: List<EnemyBehaviorRow> = listOf(
        EnemyBehaviorRow("Rifleman", "Standard, takes cover"),
        EnemyBehaviorRow("Rusher", "Charges, weak"),
        EnemyBehaviorRow("Sniper", "Laser/glint warning, one-shot"),
        EnemyBehaviorRow("Heavy gunner", "Armored, slow, suppressing fire"),
        EnemyBehaviorRow("Grenadier", "Flushes you out of cover"),
        EnemyBehaviorRow("Officer", "Buffs nearby enemies (kill first)"),
        EnemyBehaviorRow("Medic", "Heals others"),
        EnemyBehaviorRow("Vehicle/tank", "Needs launcher or flanking"),
        EnemyBehaviorRow("Drone (late)", "Flying, needs anti-air"),
        EnemyBehaviorRow("Boss", "Unique phases")
    )

    fun byName(name: String): EnemyBehaviorRow? =
        ENEMIES.firstOrNull { it.enemy.equals(name.trim(), ignoreCase = true) }

    fun count(): Int = ENEMIES.size
}

/**
 * Section 7.4 AI: "Patrol -> Alert -> Combat -> Flank -> Retreat/Heal -> Dead",
 * driven by cover points, gunshots the enemy hears and a vision cone it sees
 * through.
 */
object EnemyAi {

    /** Enemies use cover points rather than standing in the open. */
    const val USES_COVER_POINTS = true

    /** Gunshots are heard, not only seen. */
    const val HEARS_GUNSHOTS = true

    /** Sight checks run through a vision cone. */
    const val USES_VISION_CONE = true

    val TRANSITIONS: List<AiTransition> = listOf(
        AiTransition(AiState.PATROL, AiEvent.HEARD_GUNSHOT, AiState.ALERT),
        AiTransition(AiState.PATROL, AiEvent.SAW_TARGET, AiState.COMBAT),
        AiTransition(AiState.ALERT, AiEvent.SAW_TARGET, AiState.COMBAT),
        AiTransition(AiState.ALERT, AiEvent.LOST_TARGET, AiState.PATROL),
        AiTransition(AiState.COMBAT, AiEvent.FLANK_TIMER, AiState.FLANK),
        AiTransition(AiState.COMBAT, AiEvent.TOOK_HEAVY_DAMAGE, AiState.RETREAT),
        AiTransition(AiState.FLANK, AiEvent.SAW_TARGET, AiState.COMBAT),
        AiTransition(AiState.RETREAT, AiEvent.HEALTH_RESTORED, AiState.COMBAT)
    )

    /** Once an enemy is dead the machine stops. */
    fun next(current: AiState, event: AiEvent): AiState = when {
        event == AiEvent.KILLED -> AiState.DEAD
        current == AiState.DEAD -> AiState.DEAD
        else -> TRANSITIONS.firstOrNull { it.from == current && it.event == event }?.to ?: current
    }

    fun canTransition(from: AiState, to: AiState): Boolean =
        to == AiState.DEAD || TRANSITIONS.any { it.from == from && it.to == to }

    fun eventsFor(state: AiState): List<AiEvent> =
        TRANSITIONS.filter { it.from == state }.map { it.event }

    /** KILLED lands in Dead from any state, so Dead is always a target of every state. */
    fun targetsOf(state: AiState): List<AiState> =
        (TRANSITIONS.filter { it.from == state }.map { it.to } + AiState.DEAD).distinct()

    /** The six-state loop must be reachable from Patrol without a dead end. */
    fun isReachable(target: AiState): Boolean {
        val seen = mutableSetOf(AiState.PATROL)
        val queue = ArrayDeque(listOf(AiState.PATROL))
        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            targetsOf(state).forEach { next ->
                if (seen.add(next)) queue.addLast(next)
            }
        }
        return target in seen
    }
}
