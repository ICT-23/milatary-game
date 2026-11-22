package com.example.game.mission

/**
 * Objective states from section 11.2. Order matters only for readability; the
 * transition table below is the authority.
 */
enum class ObjectiveState {
    LOCKED,
    AVAILABLE,
    ACTIVE,
    COMPLETED,
    FAILED,
    SKIPPED,
    OPTIONAL_EXPIRED;

    /** Terminal states can never pay a reward twice. */
    val isTerminal: Boolean
        get() = this == COMPLETED || this == SKIPPED || this == OPTIONAL_EXPIRED

    val isRewardable: Boolean get() = this == COMPLETED
}

/** One objective in the mission graph, with explicit prerequisites. */
data class ObjectiveNode(
    val id: String,
    val title: String,
    val isOptional: Boolean = false,
    val prerequisites: List<String> = emptyList(),
    val rewardXp: Int = 0
)

/** Immutable checkpoint payload so a retry restores the same objective states. */
data class ObjectiveSnapshot(val states: Map<String, ObjectiveState>)

/**
 * Explicit objective state machine.
 *
 * - Only the transitions in [ObjectiveStateMachine.canTransition] are allowed.
 * - An objective cannot complete twice, and its reward is claimed once.
 * - Dependent objectives unlock only from their declared prerequisites.
 */
class ObjectiveTracker(val nodes: List<ObjectiveNode>) {

    private val nodeById: Map<String, ObjectiveNode> = nodes.associateBy { it.id }
    private val states = LinkedHashMap<String, ObjectiveState>()
    private val claimedRewards = mutableSetOf<String>()

    /** Section 11.3: completion also needs the extraction / debrief trigger. */
    var extractionReached: Boolean = false
        private set

    init {
        nodes.forEach { states[it.id] = ObjectiveState.LOCKED }
        unlockReadyObjectives()
    }

    fun stateOf(id: String): ObjectiveState = states[id] ?: ObjectiveState.LOCKED

    fun node(id: String): ObjectiveNode? = nodeById[id]

    fun canTransition(from: ObjectiveState, to: ObjectiveState): Boolean =
        ObjectiveStateMachine.canTransition(from, to)

    fun transition(id: String, to: ObjectiveState): Boolean {
        val from = states[id] ?: return false
        if (!canTransition(from, to)) return false
        states[id] = to
        if (to == ObjectiveState.COMPLETED) unlockReadyObjectives()
        return true
    }

    fun activate(id: String): Boolean = transition(id, ObjectiveState.ACTIVE)

    fun complete(id: String): Boolean = transition(id, ObjectiveState.COMPLETED)

    fun fail(id: String): Boolean = transition(id, ObjectiveState.FAILED)

    fun skip(id: String): Boolean = transition(id, ObjectiveState.SKIPPED)

    fun expireOptional(id: String): Boolean {
        val node = nodeById[id] ?: return false
        if (!node.isOptional) return false
        return transition(id, ObjectiveState.OPTIONAL_EXPIRED)
    }

    fun markExtractionReached() {
        extractionReached = true
    }

    /**
     * Pays an objective's reward exactly once. Returns 0 for a repeat claim, so a
     * retry or a duplicated callback can never farm experience.
     */
    fun claimReward(id: String): Int {
        val node = nodeById[id] ?: return 0
        if (stateOf(id) != ObjectiveState.COMPLETED) return 0
        if (!claimedRewards.add(id)) return 0
        return node.rewardXp
    }

    fun objectivesIn(state: ObjectiveState): List<ObjectiveNode> =
        nodes.filter { stateOf(it.id) == state }

    /** Optional objectives must never block completion. */
    fun requiredObjectives(): List<ObjectiveNode> = nodes.filter { !it.isOptional }

    fun allRequiredCompleted(): Boolean =
        requiredObjectives().all { stateOf(it.id) == ObjectiveState.COMPLETED }

    /** Section 11.3 mission completion rule. */
    fun isMissionComplete(): Boolean = allRequiredCompleted() && extractionReached

    fun activeObjective(): ObjectiveNode? =
        nodes.firstOrNull { stateOf(it.id) == ObjectiveState.ACTIVE }

    fun snapshot(): ObjectiveSnapshot = ObjectiveSnapshot(LinkedHashMap(states))

    /** Section 11.4: retry restores the checkpoint's objective states verbatim. */
    fun restore(snapshot: ObjectiveSnapshot) {
        states.clear()
        nodes.forEach { states[it.id] = snapshot.states[it.id] ?: ObjectiveState.LOCKED }
        claimedRewards.clear()
        extractionReached = false
    }

    private fun unlockReadyObjectives() {
        var changed = true
        while (changed) {
            changed = false
            for (node in nodes) {
                if (stateOf(node.id) != ObjectiveState.LOCKED) continue
                val prerequisitesMet = node.prerequisites.all {
                    stateOf(it) == ObjectiveState.COMPLETED
                }
                if (prerequisitesMet) {
                    states[node.id] = ObjectiveState.AVAILABLE
                    changed = true
                }
            }
        }
    }
}

/** The transition table itself, kept in one place and testable in isolation. */
object ObjectiveStateMachine {

    private val TRANSITIONS: Map<ObjectiveState, Set<ObjectiveState>> = mapOf(
        ObjectiveState.LOCKED to setOf(ObjectiveState.AVAILABLE),
        ObjectiveState.AVAILABLE to setOf(ObjectiveState.ACTIVE, ObjectiveState.OPTIONAL_EXPIRED),
        ObjectiveState.ACTIVE to setOf(
            ObjectiveState.COMPLETED,
            ObjectiveState.FAILED,
            ObjectiveState.SKIPPED,
            ObjectiveState.OPTIONAL_EXPIRED
        ),
        // A failed objective may be retried from a checkpoint, nothing else.
        ObjectiveState.FAILED to setOf(ObjectiveState.ACTIVE),
        ObjectiveState.COMPLETED to emptySet(),
        ObjectiveState.SKIPPED to emptySet(),
        ObjectiveState.OPTIONAL_EXPIRED to emptySet()
    )

    fun canTransition(from: ObjectiveState, to: ObjectiveState): Boolean =
        TRANSITIONS[from]?.contains(to) == true

    fun legalNext(from: ObjectiveState): Set<ObjectiveState> =
        TRANSITIONS[from] ?: emptySet()
}
