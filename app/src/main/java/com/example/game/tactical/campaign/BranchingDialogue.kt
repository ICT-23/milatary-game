package com.example.game.tactical.campaign

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Phase 6 deliverable: branching dialogue.
 *
 * Every choice carries a squad-morale delta and an optional story flag. Section 2
 * says dialogue choices "directly affect squad morale and story branches", so the
 * Act IV decision is literal: taking the tactical shortcut costs morale and
 * abandons the squad, diverting costs tempo and sets the branch that the Medal
 * of Honor evaluation reads.
 */
const val MIN_MORALE = 0
const val MAX_MORALE = 100

data class DialogueChoice(
    val id: String,
    val prompt: String,
    val moraleDelta: Int = 0,
    val reputationDelta: Int = 0,
    val advantageDelta: Int = 0,
    val setsFlag: String? = null
)

data class DialogueNode(
    val id: String,
    val speaker: String,
    val line: String,
    val choices: List<DialogueChoice> = emptyList(),
    val nextNodeId: String? = null,
    val minimumRankIndex: Int = 0
) {
    val isDecision: Boolean get() = choices.isNotEmpty()
}

data class DialogueState(
    val sceneId: String,
    val nodeId: String,
    val morale: Int = 50,
    val reputation: Int = 50,
    val advantage: Int = 0,
    val flags: Set<String> = emptySet()
) {
    val squadSaved: Boolean get() = FLAG_SQUAD_SAVED in flags
    val abandonedSquad: Boolean get() = FLAG_ABANDONED_SQUAD in flags
}

data class DialogueResult(val state: DialogueState, val node: DialogueNode?, val message: String)

object BranchingDialogue {

    const val FLAG_SUSPECTED_ROSTER = "suspected_roster"
    const val FLAG_SHARED_INTEL = "shared_intel"
    const val FLAG_SQUAD_SAVED = "squad_saved"
    const val FLAG_ABANDONED_SQUAD = "abandoned_squad"

    const val ACADEMY_ENTRY_RANK_INDEX = 0

    fun moraleLabel(morale: Int): String = when {
        morale >= 80 -> "HIGH - the squad would follow you into the citadel"
        morale >= 55 -> "STEADY - the squad trusts the plan"
        morale >= 35 -> "SHAKEN - they are watching you"
        else -> "CRITICAL - the squad is second-guessing every order"
    }

    private val ACT_I_INFORMANT: List<DialogueNode> = listOf(
        DialogueNode(
            id = "vance_intro",
            speaker = "Drill Sergeant Vance",
            line = "Candidate. Someone on this roster should not be here. " +
                "You have one question before the killhouse. Make it count.",
            choices = listOf(
                DialogueChoice(
                    id = "volunteer",
                    prompt = "\"Put me on the roster for the course again, Sergeant.\"",
                    moraleDelta = 5,
                    reputationDelta = 6
                ),
                DialogueChoice(
                    id = "question_roster",
                    prompt = "\"Who signed off on the intake roster, Sergeant?\"",
                    reputationDelta = 8,
                    setsFlag = FLAG_SUSPECTED_ROSTER
                )
            )
        ),
        DialogueNode(
            id = "vance_acknowledges",
            speaker = "Drill Sergeant Vance",
            line = "Good. Effort buys you nothing here, but it buys the squad something.",
            nextNodeId = "squad_bond"
        ),
        DialogueNode(
            id = "vance_warns",
            speaker = "Drill Sergeant Vance",
            line = "That roster came from the armory office. Keep that question to yourself " +
                "until you can prove it with a muzzle in the killhouse.",
            nextNodeId = "squad_bond"
        ),
        DialogueNode(
            id = "squad_bond",
            speaker = "Rookie Squad",
            line = "The four of them are waiting by the killhouse door, watching you.",
            choices = listOf(
                DialogueChoice(
                    id = "share_intel",
                    prompt = "Brief them on everything you noticed.",
                    moraleDelta = 8,
                    reputationDelta = 3,
                    setsFlag = FLAG_SHARED_INTEL
                ),
                DialogueChoice(
                    id = "stay_quiet",
                    prompt = "Keep the suspicion to yourself for now.",
                    moraleDelta = -4,
                    reputationDelta = 4
                )
            ),
            minimumRankIndex = ACADEMY_ENTRY_RANK_INDEX
        )
    )

    private val ACT_IV_COMRADES: List<DialogueNode> = listOf(
        DialogueNode(
            id = "briefing",
            speaker = "Battalion Command",
            line = "The command spire is exposed for six minutes. Third Squad is trapped in the " +
                "collapse zone. You cannot do both. Give me your call.",
            choices = listOf(
                DialogueChoice(
                    id = "push_spire",
                    prompt = "Push the spire. The war ends today.",
                    advantageDelta = 25,
                    moraleDelta = -15,
                    reputationDelta = 5,
                    setsFlag = FLAG_ABANDONED_SQUAD
                ),
                DialogueChoice(
                    id = "divert_rescue",
                    prompt = "Divert the assault. No one left behind.",
                    advantageDelta = -20,
                    moraleDelta = 20,
                    reputationDelta = 15,
                    setsFlag = FLAG_SQUAD_SAVED
                )
            ),
            minimumRankIndex = 4
        ),
        DialogueNode(
            id = "spire_alone",
            speaker = "Battalion Command",
            line = "Understood. The spire falls either way, but Third Squad is on its own. " +
                "Command will remember the call you made.",
            nextNodeId = null,
            minimumRankIndex = 4
        ),
        DialogueNode(
            id = "rescue_success",
            speaker = "Third Squad",
            line = "We heard the guns turn toward us. Nobody expected the battalion to follow. " +
                "All of them are walking out of the collapse zone.",
            nextNodeId = null,
            minimumRankIndex = 4
        )
    )

    private val SCENES: Map<String, List<DialogueNode>> = mapOf(
        "act1_informant" to ACT_I_INFORMANT,
        "act4_comrades" to ACT_IV_COMRADES
    )

    fun sceneIds(): List<String> = SCENES.keys.toList()

    fun nodesFor(sceneId: String): List<DialogueNode> = SCENES[sceneId].orEmpty()

    fun nodeAt(sceneId: String, nodeId: String): DialogueNode? =
        nodesFor(sceneId).firstOrNull { it.id == nodeId }

    /** Opens a scene on its first node, refusing scenes whose rank gate is not met. */
    fun start(
        sceneId: String,
        rankIndex: Int = 0,
        morale: Int = 50,
        reputation: Int = 50
    ): GameResult<DialogueState> {
        val nodes = nodesFor(sceneId)
        val first = nodes.firstOrNull()
            ?: return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "SCENE_NOT_FOUND",
                "No dialogue scene '$sceneId'"
            )
        if (rankIndex < first.minimumRankIndex) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "DIALOGUE_RANK_LOCKED",
                "This scene opens at rank index ${first.minimumRankIndex}"
            )
        }
        return GameResult.Ok(
            DialogueState(
                sceneId = sceneId,
                nodeId = first.id,
                morale = morale.coerceIn(MIN_MORALE, MAX_MORALE),
                reputation = reputation.coerceIn(MIN_MORALE, MAX_MORALE)
            )
        )
    }

    /** Applies one choice and advances. A node with no choices only needs [advance]. */
    fun choose(state: DialogueState, choiceId: String): GameResult<DialogueResult> {
        val node = nodeAt(state.sceneId, state.nodeId)
            ?: return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "NODE_NOT_FOUND",
                "Dialogue node '${state.nodeId}' is not in ${state.sceneId}"
            )
        if (!node.isDecision) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "NOT_A_DECISION",
                "'${node.id}' has no choices; advance instead"
            )
        }
        val choice = node.choices.firstOrNull { it.id == choiceId }
            ?: return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "CHOICE_NOT_FOUND",
                "'$choiceId' is not offered at '${node.id}'"
            )

        val nextState = state.copy(
            morale = (state.morale + choice.moraleDelta).coerceIn(MIN_MORALE, MAX_MORALE),
            reputation = (state.reputation + choice.reputationDelta).coerceIn(MIN_MORALE, MAX_MORALE),
            advantage = (state.advantage + choice.advantageDelta).coerceAtLeast(0),
            flags = choice.setsFlag?.let { state.flags + it } ?: state.flags
        )
        val nextNode = nextNodeFor(node, nextState)
        return GameResult.Ok(
            DialogueResult(
                state = if (nextNode != null) nextState.copy(nodeId = nextNode.id) else nextState,
                node = nextNode,
                message = if (nextNode == null) {
                    "Scene complete - morale ${nextState.morale}: ${moraleLabel(nextState.morale)}"
                } else {
                    nextNode.line
                }
            )
        )
    }

    /** Continues a linear node that carries no choices. */
    fun advance(state: DialogueState): GameResult<DialogueResult> {
        val node = nodeAt(state.sceneId, state.nodeId)
            ?: return GameResult.fail(
                GameErrorCategory.NARRATIVE,
                "NODE_NOT_FOUND",
                "Dialogue node '${state.nodeId}' is not in ${state.sceneId}"
            )
        if (node.isDecision) {
            return GameResult.fail(
                GameErrorCategory.INVALID_STATE,
                "DECISION_REQUIRED",
                "'${node.id}' requires a choice"
            )
        }
        val nextNode = node.nextNodeId?.let { nodeAt(state.sceneId, it) }
        return GameResult.Ok(
            DialogueResult(
                state = if (nextNode != null) state.copy(nodeId = nextNode.id) else state,
                node = nextNode,
                message = nextNode?.line ?: "Scene complete"
            )
        )
    }

    private fun nextNodeFor(node: DialogueNode, state: DialogueState): DialogueNode? {
        val targetId = node.nextNodeId ?: when (node.id) {
            "vance_intro" -> if (FLAG_SUSPECTED_ROSTER in state.flags) "vance_warns" else "vance_acknowledges"
            "briefing" -> if (FLAG_SQUAD_SAVED in state.flags) "rescue_success" else "spire_alone"
            else -> null
        }
        return targetId?.let { nodeAt(state.sceneId, it) }
    }
}
