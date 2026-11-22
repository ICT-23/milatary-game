package com.example.game.vanguard.progression

import com.example.game.vanguard.core.GameErrorCategory
import com.example.game.vanguard.core.GameResult

/**
 * Section 2 Progression: SkillTreeService.
 *
 * Skill points are earned with rank and spent on branches. Prerequisites are
 * checked before points are deducted, so a failed purchase never costs
 * anything - the same "no side effect on failure" rule the combat gates use.
 */
enum class SkillBranch(val displayName: String) {
    MARKSMANSHIP("Marksmanship"),
    FIELD_CRAFT("Field Craft"),
    COMMAND("Command")
}

data class SkillNode(
    val id: String,
    val name: String,
    val branch: SkillBranch,
    val costPerRank: Int,
    val maxRank: Int,
    val requires: List<String> = emptyList()
)

data class SkillState(
    val points: Int,
    val ranks: Map<String, Int> = emptyMap()
) {
    fun rankOf(nodeId: String): Int = ranks[nodeId] ?: 0

    fun spendOn(nodeId: String): SkillState =
        copy(ranks = ranks + (nodeId to rankOf(nodeId) + 1))

    val totalRanks: Int get() = ranks.values.sum()
}

object SkillTreeService {

    const val BONUS_PER_RANK = 0.05f

    val TREE: List<SkillNode> = listOf(
        SkillNode("SKILL_STEADY_AIM", "Steady Aim", SkillBranch.MARKSMANSHIP, 1, 3),
        SkillNode(
            "SKILL_TRIGGER_DISCIPLINE",
            "Trigger Discipline",
            SkillBranch.MARKSMANSHIP,
            costPerRank = 2,
            maxRank = 1,
            requires = listOf("SKILL_STEADY_AIM")
        ),
        SkillNode("SKILL_FIELD_CRAFT", "Field Craft", SkillBranch.FIELD_CRAFT, 1, 2),
        SkillNode("SKILL_SQUAD_COMMS", "Squad Comms", SkillBranch.COMMAND, 2, 2),
        SkillNode(
            "SKILL_MISSION_COMMAND",
            "Mission Command",
            SkillBranch.COMMAND,
            costPerRank = 3,
            maxRank = 1,
            requires = listOf("SKILL_SQUAD_COMMS")
        )
    )

    fun node(id: String): SkillNode? = TREE.firstOrNull { it.id == id }

    fun canUnlock(state: SkillState, nodeId: String): GameResult<SkillNode> {
        val skill = node(nodeId) ?: return GameResult.fail(
            GameErrorCategory.PROGRESSION,
            "UNKNOWN_SKILL",
            "No skill node with id $nodeId"
        )
        val missing = skill.requires.filter { state.rankOf(it) <= 0 }
        if (missing.isNotEmpty()) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "PREREQUISITE_MISSING",
                "${skill.name} requires ${missing.joinToString()}"
            )
        }
        if (state.rankOf(nodeId) >= skill.maxRank) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "MAX_RANK_REACHED",
                "${skill.name} is already at rank ${skill.maxRank}"
            )
        }
        if (state.points < skill.costPerRank) {
            return GameResult.fail(
                GameErrorCategory.PROGRESSION,
                "NO_SKILL_POINTS",
                "${skill.name} costs ${skill.costPerRank}, ${state.points} available"
            )
        }
        return GameResult.Ok(skill)
    }

    fun unlock(state: SkillState, nodeId: String): GameResult<SkillState> {
        val gate = canUnlock(state, nodeId)
        if (gate is GameResult.Err) return gate
        return GameResult.Ok(
            state.copy(points = state.points - gate.value.costPerRank).spendOn(nodeId)
        )
    }

    /** Branch bonuses are linear in ranks so a UI can state them exactly. */
    fun branchBonus(state: SkillState, branch: SkillBranch): Float {
        val ranks = TREE.filter { it.branch == branch }.sumOf { state.rankOf(it.id) }
        return ranks * BONUS_PER_RANK
    }
}
